from __future__ import annotations

import io
from typing import Sequence

from PIL import Image

from app.core.utils import parse_csv_tags
from app.models.item import Item
from app.schemas.outfit import OutfitSuggestRequest
from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier
from app.services.inspiration.search import InspirationResult, match_inspiration
from app.services.outfits.filters import item_matches_style, item_matches_weather, prefer_explicit_style
from app.services.outfits.harmony import HarmonyMode, suggest_outfit
from app.services.outfits.slots import OUTER_CATEGORIES, SHOES_CATEGORIES, TOP_CATEGORIES
from app.services.storage import save_upload

_DRESS_CATS = {"dress"}
_TOP_CATS = TOP_CATEGORIES | _DRESS_CATS
_BOTTOM_CATS = {"jeans", "pants", "shorts", "skirt"}


def build_suggestion(
    items: Sequence[Item],
    payload: OutfitSuggestRequest,
    preferred_styles: list[str] | None,
) -> dict[str, Item] | None:
    """Categorize *items* into slots, apply filters, and return the best harmony-scored combo."""
    weather_tags = parse_csv_tags(payload.weather)
    style = payload.style.strip().lower() if payload.style else None
    effective_style = style or ((preferred_styles or [None])[0] if not style else None)

    def passes_filters(item: Item) -> bool:
        if weather_tags and not item_matches_weather(item, weather_tags):
            return False
        if style and not item_matches_style(item, style):
            return False
        return True

    candidates: dict[str, list[Item]] = {"top": [], "bottom": [], "outer": [], "shoes": []}
    for item in items:
        if not passes_filters(item):
            continue
        cat = (item.category or "").strip().lower()
        if cat in _TOP_CATS:
            candidates["top"].append(item)
        elif cat in _BOTTOM_CATS:
            candidates["bottom"].append(item)
        elif cat in OUTER_CATEGORIES:
            candidates["outer"].append(item)
        elif cat in SHOES_CATEGORIES:
            candidates["shoes"].append(item)

    if effective_style:
        for slot in ("top", "bottom", "shoes", "outer"):
            candidates[slot] = prefer_explicit_style(candidates[slot], effective_style)

    allowed_modes: list[HarmonyMode] | None = None
    if payload.modes:
        parsed = []
        for m in payload.modes:
            try:
                parsed.append(HarmonyMode(m.lower()))
            except ValueError:
                pass
        allowed_modes = parsed or None

    return suggest_outfit(candidates, allowed_modes=allowed_modes)


def run_inspiration(
    raw: bytes,
    ext: str,
    items: Sequence[Item],
    base_classifier: ClipAttributeClassifier,
) -> tuple[str, InspirationResult]:
    """Save the uploaded image and match wardrobe items per slot by CLIP similarity."""
    screenshot_name = save_upload(raw, ext)
    rgb_img = Image.open(io.BytesIO(raw)).convert("RGB")
    result = match_inspiration(rgb_img, base_classifier, list(items))
    return screenshot_name, result
