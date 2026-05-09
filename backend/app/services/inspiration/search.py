from __future__ import annotations

from dataclasses import dataclass, field
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
    category_to_slot,
)

_SLOT_CATEGORIES: dict[OutfitSlot, set[str]] = {
    OutfitSlot.TOP: TOP_CATEGORIES | {"dress"},
    OutfitSlot.BOTTOM: BOTTOM_CATEGORIES,
    OutfitSlot.OUTER: OUTER_CATEGORIES,
    OutfitSlot.SHOES: SHOES_CATEGORIES,
}

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


def _decode_embedding(item: Item) -> np.ndarray | None:
    if item.embedding is None:
        return None
    arr = np.frombuffer(item.embedding, dtype=np.float32).copy()
    return arr


def cosine_topk(
    query: np.ndarray,
    items: Sequence[Item],
    slot: OutfitSlot,
    k: int = 5,
) -> list[tuple[Item, float]]:
    """Return top-k items for *slot* ranked by cosine similarity to *query*.

    Items without an embedding or not belonging to *slot* are skipped.
    *query* must already be L2-normalised (as produced by encode_image).
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
    return [(candidates[i], float(scores[i])) for i in top_indices]


def match_inspiration(
    rgb_img: Image.Image,
    base_classifier: ClipAttributeClassifier,
    items: Sequence[Item],
    k: int = 5,
) -> InspirationResult:
    """Find the best wardrobe matches per slot for an inspiration image.

    Segments the image with SegFormer, encodes each garment crop with CLIP,
    then ranks the user's items per slot by cosine similarity.
    For any slot the segmenter misses, the full-image CLIP embedding is used
    as a fallback so the slot still gets ranked candidates.
    """
    global_embed = base_classifier.encode_image(rgb_img).cpu().numpy().squeeze(0)
    crops = _segmenter.segment(rgb_img)

    slot_embeds: dict[OutfitSlot, np.ndarray] = {}
    for slot, crop in crops.items():
        embed = base_classifier.encode_image(crop).cpu().numpy().squeeze(0)
        slot_embeds[slot] = embed

    def _match(slot: OutfitSlot) -> SlotMatch:
        query = slot_embeds.get(slot, global_embed)
        ranked = cosine_topk(query, items, slot, k=k)
        if not ranked:
            return SlotMatch(best=None, alternates=[], score=None)
        best_item, best_score = ranked[0]
        alternates = [item for item, _ in ranked[1:]]
        return SlotMatch(best=best_item, alternates=alternates, score=best_score)

    return InspirationResult(
        top=_match(OutfitSlot.TOP),
        bottom=_match(OutfitSlot.BOTTOM),
        outer=_match(OutfitSlot.OUTER),
        shoes=_match(OutfitSlot.SHOES),
    )
