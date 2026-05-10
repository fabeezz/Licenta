from __future__ import annotations

from dataclasses import dataclass
from typing import Sequence

import numpy as np
from PIL import Image

from app.models.item import Item
from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier
from app.services.inspiration.segmenter import GarmentSegmenter
from app.services.outfits.slots import (
    BOTTOM_CATEGORIES,
    OUTER_CATEGORIES,
    SHOES_CATEGORIES,
    TOP_CATEGORIES,
    OutfitSlot,
)
from app.services.storage import media_path

_SLOT_CATEGORIES: dict[OutfitSlot, set[str]] = {
    OutfitSlot.TOP: TOP_CATEGORIES | {"dress"},
    OutfitSlot.BOTTOM: BOTTOM_CATEGORIES,
    OutfitSlot.OUTER: OUTER_CATEGORIES,
    OutfitSlot.SHOES: SHOES_CATEGORIES,
}

# Labels used for zero-shot CLIP outer-presence detection.
_OUTER_LABELS = ["jacket or coat", "shirt or top"]
_OUTER_TEMPLATES = ["a photo of a person wearing a {}"]
# Threshold: if outer-label softmax prob >= this, treat Upper-clothes as an outer.
_OUTER_THRESHOLD = 0.5

_segmenter = GarmentSegmenter()


@dataclass
class SlotMatch:
    best: Item | None
    alternates: list[Item]
    score: float | None


@dataclass
class InspirationResult:
    top: SlotMatch
    bottom: SlotMatch
    outer: SlotMatch
    shoes: SlotMatch


def _rgb_histogram(img: Image.Image, bins: int = 32) -> np.ndarray:
    arr = np.array(img.convert("RGB"))
    hist = np.concatenate([
        np.histogram(arr[:, :, ch], bins=bins, range=(0, 256))[0].astype(np.float32)
        for ch in range(3)
    ])
    total = hist.sum()
    return hist / total if total > 0 else hist


def _histogram_intersection(a: np.ndarray, b: np.ndarray) -> float:
    return float(np.minimum(a, b).sum())


def cosine_topk(
    query: np.ndarray,
    items: Sequence[Item],
    slot: OutfitSlot,
    k: int = 5,
    ref_img: Image.Image | None = None,
) -> list[tuple[Item, float]]:
    """Return top-k items for *slot* ranked by cosine similarity to *query*.

    Items without an embedding or not belonging to *slot* are skipped.
    *query* must already be L2-normalised (as produced by encode_image).

    If *ref_img* is provided, scores are blended with an HSV histogram
    intersection (0.7 × cosine + 0.3 × color similarity) for better color
    matching. Falls back to cosine-only if an item's image cannot be loaded.
    """
    allowed = _SLOT_CATEGORIES[slot]
    candidates = [
        item for item in items
        if item.embedding is not None
        and (item.category or "").strip().lower() in allowed
    ]
    if not candidates:
        return []

    matrix = np.stack([
        np.frombuffer(item.embedding, dtype=np.float32)  # type: ignore[arg-type]
        for item in candidates
    ])
    scores = matrix @ query
    top_indices = np.argpartition(scores, -min(k, len(scores)))[-min(k, len(scores)):]
    top_indices = top_indices[np.argsort(scores[top_indices])[::-1]]
    top_results = [(candidates[int(i)], float(scores[i])) for i in top_indices]

    if ref_img is None:
        return top_results

    try:
        ref_hist = _rgb_histogram(ref_img)
    except Exception:
        return top_results

    reranked: list[tuple[Item, float]] = []
    for item, cosine_score in top_results:
        try:
            img_name = item.image_no_bg_name or item.image_original_name
            item_img = Image.open(media_path(img_name)).convert("RGB")
            item_hist = _rgb_histogram(item_img)
            color_sim = _histogram_intersection(ref_hist, item_hist)
            blended = 0.7 * cosine_score + 0.3 * color_sim
        except Exception:
            blended = cosine_score
        reranked.append((item, blended))
    reranked.sort(key=lambda x: x[1], reverse=True)
    return reranked


def match_inspiration(
    rgb_img: Image.Image,
    base_classifier: ClipAttributeClassifier,
    items: Sequence[Item],
    k: int = 5,
) -> InspirationResult:
    """Find the best wardrobe matches per slot for an inspiration image.

    Segments the image with SegFormer, encodes each garment crop with CLIP,
    then ranks the user's items per slot by cosine similarity with optional
    color re-ranking.

    Outer-presence detection: the 'Upper-clothes' SegFormer crop is classified
    with zero-shot CLIP to distinguish coats/jackets from base tops. If an outer
    is detected, that crop's embedding is routed to the OUTER slot and TOP falls
    back to the global-image embedding (suggesting a base layer to wear under).
    If no outer is detected, the OUTER slot is omitted from the result.

    Dress rule: when TOP resolves to a dress, BOTTOM is suppressed (a dress
    already covers both zones).

    Cross-slot de-duplication: the same item will not appear as the best pick
    in more than one slot.
    """
    global_embed = base_classifier.encode_image(rgb_img).cpu().numpy().squeeze(0)
    crops = _segmenter.segment(rgb_img)

    # --- A+B: detect whether the Upper-clothes crop is an outer (coat/jacket) ---
    has_outer = False
    top_embed_np: np.ndarray | None = None
    if OutfitSlot.TOP in crops:
        top_torch = base_classifier.encode_image(crops[OutfitSlot.TOP])
        pred = base_classifier.score_labels(
            top_torch,
            labels=_OUTER_LABELS,
            templates=_OUTER_TEMPLATES,
        )
        has_outer = pred.label == _OUTER_LABELS[0] and pred.confidence >= _OUTER_THRESHOLD
        top_embed_np = top_torch.cpu().numpy().squeeze(0)

    slot_embeds: dict[OutfitSlot, np.ndarray] = {}
    for slot, crop in crops.items():
        if slot == OutfitSlot.TOP:
            # top_embed_np is guaranteed non-None here: TOP is in crops iff the block above ran.
            assert top_embed_np is not None
            if has_outer:
                # Coat detected: route this crop to OUTER; TOP will fall back to global_embed.
                slot_embeds[OutfitSlot.OUTER] = top_embed_np
            else:
                slot_embeds[OutfitSlot.TOP] = top_embed_np
        else:
            embed = base_classifier.encode_image(crop).cpu().numpy().squeeze(0)
            slot_embeds[slot] = embed

    def _match(slot: OutfitSlot) -> SlotMatch:
        # Suppress OUTER entirely when reference has no outer garment.
        if slot == OutfitSlot.OUTER and not has_outer:
            return SlotMatch(best=None, alternates=[], score=None)
        query: np.ndarray = slot_embeds[slot] if slot in slot_embeds else global_embed
        # Use the segmented crop for color re-ranking; OUTER reuses TOP's crop when
        # it was rerouted (has_outer=True); slots falling back to global_embed skip
        # color re-ranking (ref_img=None) to avoid misleading whole-image color bias.
        if has_outer and slot == OutfitSlot.OUTER:
            ref_crop = crops.get(OutfitSlot.TOP)
        elif slot in slot_embeds and slot in crops:
            ref_crop = crops.get(slot)
        else:
            ref_crop = None
        ranked = cosine_topk(query, items, slot, k=k, ref_img=ref_crop)
        if not ranked:
            return SlotMatch(best=None, alternates=[], score=None)
        best_item, best_score = ranked[0]
        alternates = [item for item, _ in ranked[1:]]
        return SlotMatch(best=best_item, alternates=alternates, score=best_score)

    # --- Build slot matches in priority order for de-duplication ---
    seen_ids: set[int] = set()

    def _dedup(match: SlotMatch) -> SlotMatch:
        if match.best is None:
            return match
        if match.best.id in seen_ids:
            for alt in match.alternates:
                if alt.id not in seen_ids:
                    seen_ids.add(alt.id)
                    return SlotMatch(
                        best=alt,
                        alternates=[a for a in match.alternates if a is not alt],
                        score=match.score,
                    )
            return SlotMatch(best=None, alternates=[], score=None)
        seen_ids.add(match.best.id)
        return match

    top_m = _dedup(_match(OutfitSlot.TOP))
    outer_m = _dedup(_match(OutfitSlot.OUTER))

    # E: dress rule — a dress already covers both top and bottom zones.
    top_category = (top_m.best.category or "").strip().lower() if top_m.best else ""
    if top_category == "dress":
        bottom_m = SlotMatch(best=None, alternates=[], score=None)
    else:
        bottom_m = _dedup(_match(OutfitSlot.BOTTOM))

    shoes_m = _dedup(_match(OutfitSlot.SHOES))

    return InspirationResult(
        top=top_m,
        bottom=bottom_m,
        outer=outer_m,
        shoes=shoes_m,
    )
