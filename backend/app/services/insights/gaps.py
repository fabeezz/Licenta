from __future__ import annotations

from dataclasses import dataclass
from typing import Literal, Sequence

from app.models.item import Item
from app.services.outfits.slots import OutfitSlot, category_to_slot

_WEATHER_TAGS: frozenset[str] = frozenset({"warm", "cold", "rainy"})
_STYLE_TAGS: frozenset[str] = frozenset({"casual", "formal", "sporty"})

# Color group buckets (mirrors _COLOR_GROUPS in item_repository.py)
_COLOR_LIGHT = frozenset({"white", "beige", "pink", "yellow", "cyan"})
_COLOR_DARK = frozenset({"black", "gray", "burgundy", "brown", "olive", "dark green", "navy"})
_COLOR_COLORFUL = frozenset({"red", "orange", "green", "blue", "purple"})

_SLOT_MISSING_THRESHOLD = 1   # 0 items → missing
_SLOT_LOW_THRESHOLD = 2       # < 2 items → low
_WEATHER_MISSING_THRESHOLD = 1
_WEATHER_LOW_THRESHOLD = 3
_STYLE_MISSING_THRESHOLD = 1
_STYLE_LOW_THRESHOLD = 3
_COLOR_LOW_PCT = 0.15         # group share below 15 % → low

_SUGGESTIONS: dict[tuple[str, str], str] = {
    ("slot", "top"): "Add some tops — shirts, t-shirts, or sweaters — to round out your wardrobe.",
    ("slot", "bottom"): "Add some bottoms like jeans, pants, or shorts to complete your outfits.",
    ("slot", "outer"): "A jacket, coat, or hoodie gives you layering options for cooler days.",
    ("slot", "shoes"): "Add a pair of shoes — your wardrobe can't form complete outfits without one.",
    ("weather", "warm"): "Pick up some warm-weather pieces like t-shirts or shorts for summer days.",
    ("weather", "cold"): "Add a coat, sweater, or knitwear to stay covered on cold days.",
    ("weather", "rainy"): "A waterproof jacket or raincoat will prepare you for rainy days.",
    ("style", "casual"): "Add some everyday casual pieces — a relaxed staple for off-duty days.",
    ("style", "formal"): "Consider a blazer, dress shirt, or dress shoes for formal occasions.",
    ("style", "sporty"): "Add some athletic or activewear for gym sessions and active outings.",
    ("color", "light"): "Add a light-toned piece (white, beige) — neutrals anchor most outfits.",
    ("color", "dark"): "Add a dark neutral (black, navy, gray) to ground your color palette.",
    ("color", "colorful"): "Introduce some color — a bold piece lifts an otherwise muted wardrobe.",
    ("color", "neutrals"): "You're missing neutral tones entirely. A white or black basic is endlessly versatile.",
}


@dataclass(frozen=True)
class Gap:
    dimension: Literal["slot", "weather", "style", "color"]
    key: str
    severity: Literal["missing", "low"]
    suggestion: str


def _dominant(item: Item) -> str | None:
    if not item.color_tags:
        return None
    dominant = item.color_tags.get("dominant")
    if dominant and isinstance(dominant, list) and dominant:
        return dominant[0]
    return None


def analyze(items: Sequence[Item]) -> list[Gap]:
    """Return a list of wardrobe gaps across slot, weather, style, and color dimensions."""
    gaps: list[Gap] = []
    gaps.extend(_slot_gaps(items))
    gaps.extend(_weather_gaps(items))
    gaps.extend(_style_gaps(items))
    gaps.extend(_color_gaps(items))
    return gaps


# ── Slot coverage ─────────────────────────────────────────────────────────────

def _slot_gaps(items: Sequence[Item]) -> list[Gap]:
    counts: dict[OutfitSlot, int] = {s: 0 for s in OutfitSlot}
    for item in items:
        slot = category_to_slot(item.category)
        if slot is not None:
            counts[slot] += 1

    result: list[Gap] = []
    for slot, count in counts.items():
        if count < _SLOT_MISSING_THRESHOLD:
            result.append(_gap("slot", slot.value, "missing"))
        elif count < _SLOT_LOW_THRESHOLD:
            result.append(_gap("slot", slot.value, "low"))
    return result


# ── Weather coverage ──────────────────────────────────────────────────────────

def _weather_gaps(items: Sequence[Item]) -> list[Gap]:
    counts: dict[str, int] = {tag: 0 for tag in _WEATHER_TAGS}
    for item in items:
        tags = set(item.weather or [])
        effective = tags - {"all-weather"}
        if "all-weather" in tags:
            effective = _WEATHER_TAGS  # credit every tag
        for tag in effective & _WEATHER_TAGS:
            counts[tag] += 1

    result: list[Gap] = []
    for tag, count in counts.items():
        if count < _WEATHER_MISSING_THRESHOLD:
            result.append(_gap("weather", tag, "missing"))
        elif count < _WEATHER_LOW_THRESHOLD:
            result.append(_gap("weather", tag, "low"))
    return result


# ── Style coverage ────────────────────────────────────────────────────────────

def _style_gaps(items: Sequence[Item]) -> list[Gap]:
    counts: dict[str, int] = {tag: 0 for tag in _STYLE_TAGS}
    for item in items:
        for tag in (item.style or []):
            if tag in counts:
                counts[tag] += 1

    result: list[Gap] = []
    for tag, count in counts.items():
        if count < _STYLE_MISSING_THRESHOLD:
            result.append(_gap("style", tag, "missing"))
        elif count < _STYLE_LOW_THRESHOLD:
            result.append(_gap("style", tag, "low"))
    return result


# ── Color balance ─────────────────────────────────────────────────────────────

def _color_gaps(items: Sequence[Item]) -> list[Gap]:
    total = len(items)
    if total == 0:
        return [_gap("color", "neutrals", "missing")]

    light_count = 0
    dark_count = 0
    colorful_count = 0

    for item in items:
        dc = _dominant(item)
        if dc is None:
            continue
        if dc in _COLOR_LIGHT:
            light_count += 1
        if dc in _COLOR_DARK:
            dark_count += 1
        if dc in _COLOR_COLORFUL:
            colorful_count += 1

    result: list[Gap] = []

    # No neutrals at all (dark group has the most neutral colors)
    neutral_count = dark_count + light_count
    if neutral_count == 0:
        result.append(_gap("color", "neutrals", "missing"))
    else:
        if light_count / total < _COLOR_LOW_PCT:
            result.append(_gap("color", "light", "low"))
        if dark_count / total < _COLOR_LOW_PCT:
            result.append(_gap("color", "dark", "low"))

    if colorful_count / total < _COLOR_LOW_PCT:
        result.append(_gap("color", "colorful", "low"))

    return result


def _gap(dimension: str, key: str, severity: Literal["missing", "low"]) -> Gap:
    suggestion = _SUGGESTIONS.get((dimension, key), f"Consider adding more {key} options to your wardrobe.")
    return Gap(dimension=dimension, key=key, severity=severity, suggestion=suggestion)  # type: ignore[arg-type]
