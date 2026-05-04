from __future__ import annotations

import random
from enum import StrEnum
from typing import Sequence

from app.models.item import Item

# ── Color vocabulary ─────────────────────────────────────────────────────────

NEUTRALS: frozenset[str] = frozenset({
    "black", "white", "gray", "beige", "navy", "brown", "olive",
})

# Colors that pair like a single hue family for monochrome detection
_TONAL_GROUPS: list[frozenset[str]] = [
    frozenset({"red", "burgundy", "pink"}),
    frozenset({"yellow", "orange"}),
    frozenset({"green", "dark green"}),
    frozenset({"blue", "cyan"}),
    frozenset({"purple"}),
]

# Wheel positions for analogous detection (each entry = one hue band)
_WHEEL: list[frozenset[str]] = [
    frozenset({"red", "burgundy", "pink"}),
    frozenset({"orange"}),
    frozenset({"yellow"}),
    frozenset({"green", "dark green"}),
    frozenset({"cyan"}),
    frozenset({"blue"}),
    frozenset({"purple"}),
]

# Hardcoded complementary pairs (A ↔ B)
_COMPLEMENTARY_PAIRS: list[tuple[frozenset[str], frozenset[str]]] = [
    (frozenset({"red", "burgundy", "pink"}), frozenset({"green", "dark green"})),
    (frozenset({"orange"}), frozenset({"blue"})),
    (frozenset({"yellow"}), frozenset({"purple"})),
    (frozenset({"cyan"}), frozenset({"red", "burgundy", "pink"})),
]

# Layering penalty triggers
_HEAVY_OUTERS: frozenset[str] = frozenset({"coat", "jacket", "puffer jacket"})
_BULKY_TOPS: frozenset[str] = frozenset({"hoodie", "sweater"})


class HarmonyMode(StrEnum):
    NEUTRAL_ACCENT = "neutral_accent"
    MONOCHROME = "monochrome"
    ANALOGOUS = "analogous"
    COMPLEMENTARY = "complementary"


# ── Internal helpers ─────────────────────────────────────────────────────────

def _wheel_pos(color: str) -> int | None:
    for i, band in enumerate(_WHEEL):
        if color in band:
            return i
    return None


def _tonal_group(color: str) -> int | None:
    for i, group in enumerate(_TONAL_GROUPS):
        if color in group:
            return i
    return None


def _are_analogous(colors: set[str]) -> bool:
    if not colors:
        return True
    positions = [_wheel_pos(c) for c in colors]
    positions = [p for p in positions if p is not None]
    if not positions:
        return True
    n = len(_WHEEL)
    for i, a in enumerate(positions):
        for b in positions[i + 1:]:
            dist = min(abs(a - b), n - abs(a - b))
            if dist > 1:
                return False
    return True


def _are_complementary(colors: set[str]) -> bool:
    if len(colors) < 2:
        return False
    for a_group, b_group in _COMPLEMENTARY_PAIRS:
        has_a = bool(colors & a_group)
        has_b = bool(colors & b_group)
        if has_a and has_b:
            # No third color band outside these two groups
            remaining = colors - a_group - b_group
            if not remaining:
                return True
    return False


def _all_same_tonal_group(colors: set[str]) -> bool:
    if not colors:
        return True
    groups = {_tonal_group(c) for c in colors}
    groups.discard(None)
    return len(groups) <= 1


# ── Public API ───────────────────────────────────────────────────────────────

def score_outfit(items_by_slot: dict[str, Item | None], mode: HarmonyMode) -> float:
    """Score how well a slot→item mapping matches *mode* and general harmony rules.

    Higher is better; scores can be negative for poor combos.
    """
    score = 0.0

    all_items = [item for item in items_by_slot.values() if item is not None]
    dominants = [(item.dominant_color or "").strip().lower() for item in all_items]
    dominants = [c for c in dominants if c and c != "unknown"]

    non_neutrals = [c for c in dominants if c not in NEUTRALS]
    unique_non_neutrals: set[str] = set(non_neutrals)

    # Neutral anchor
    if any(c in NEUTRALS for c in dominants):
        score += 2.0

    # Bold cap
    if len(unique_non_neutrals) > 2:
        score -= float(len(unique_non_neutrals) - 2)

    # Mode bonus
    if mode == HarmonyMode.NEUTRAL_ACCENT:
        if len(unique_non_neutrals) <= 1:
            score += 2.0
    elif mode == HarmonyMode.MONOCHROME:
        if _all_same_tonal_group(unique_non_neutrals):
            score += 2.0
    elif mode == HarmonyMode.ANALOGOUS:
        if _are_analogous(unique_non_neutrals):
            score += 2.0
    elif mode == HarmonyMode.COMPLEMENTARY:
        if _are_complementary(unique_non_neutrals):
            score += 2.0

    # Shoes tie-in
    shoes = items_by_slot.get("shoes")
    if shoes:
        shoe_dom = (shoes.dominant_color or "").strip().lower()
        if shoe_dom in NEUTRALS:
            score += 1.0
        elif shoe_dom and any(
            item is not shoes and (item.dominant_color or "").strip().lower() == shoe_dom
            for item in all_items
        ):
            score += 1.0

    # Outer-top harmony
    outer = items_by_slot.get("outer")
    top = items_by_slot.get("top")
    if outer and top:
        outer_dom = (outer.dominant_color or "").strip().lower()
        top_dom = (top.dominant_color or "").strip().lower()
        both_neutral = outer_dom in NEUTRALS and top_dom in NEUTRALS
        same_color = outer_dom and outer_dom == top_dom
        if both_neutral or same_color:
            score += 1.0
        elif outer_dom in NEUTRALS or top_dom in NEUTRALS:
            score += 0.5

        # Layering penalty
        if (outer.category or "") in _HEAVY_OUTERS and (top.category or "") in _BULKY_TOPS:
            score -= 2.0

    # Style consistency — penalise outfits that mix explicit style tags.
    # Items with an empty style list are "any-style" and don't contribute.
    explicit_styles: set[str] = set()
    for item in all_items:
        for s in (item.style or []):
            explicit_styles.add(s)
    if len(explicit_styles) == 1:
        score += 3.0   # all styled items agree on one style
    elif len(explicit_styles) >= 2:
        score -= 3.0   # conflicting explicit styles

    return score


def suggest_outfit(
    candidates_by_slot: dict[str, Sequence[Item]],
    allowed_modes: list[HarmonyMode] | None = None,
    samples: int = 30,
    rng: random.Random | None = None,
) -> dict[str, Item] | None:
    """Return the best-scoring outfit from *samples* random combinations.

    Returns ``None`` if every required slot has no candidates.
    Falls back gracefully when some slots are empty (they're omitted from scoring).
    """
    if rng is None:
        rng = random.Random()
    if not allowed_modes:
        allowed_modes = list(HarmonyMode)

    non_empty = {slot: list(items) for slot, items in candidates_by_slot.items() if items}
    if not non_empty:
        return None

    mode = rng.choice(allowed_modes)

    best_combo: dict[str, Item] | None = None
    best_score = float("-inf")

    for _ in range(samples):
        combo: dict[str, Item] = {slot: rng.choice(items) for slot, items in non_empty.items()}
        s = score_outfit(combo, mode)
        if s > best_score:
            best_score = s
            best_combo = combo

    return best_combo
