from __future__ import annotations

from enum import StrEnum


class OutfitSlot(StrEnum):
    TOP = "top"
    BOTTOM = "bottom"
    OUTER = "outer"
    SHOES = "shoes"


TOP_CATEGORIES = {"t-shirt", "shirt", "sweater"}
THIN_TOP_CATEGORIES = {"t-shirt", "shirt"}
BOTTOM_CATEGORIES = {"jeans", "pants", "shorts", "skirt"}
OUTER_CATEGORIES = {"hoodie", "jacket", "coat", "blazer"}
SHOES_CATEGORIES = {"sneakers", "shoes", "boots"}
DUAL_SLOT_CATEGORIES = {"dress"}


def category_to_slot(category: str | None) -> OutfitSlot | None:
    if not category:
        return None
    c = category.strip().lower()
    if c in TOP_CATEGORIES:
        return OutfitSlot.TOP
    if c in BOTTOM_CATEGORIES:
        return OutfitSlot.BOTTOM
    if c in OUTER_CATEGORIES:
        return OutfitSlot.OUTER
    if c in SHOES_CATEGORIES:
        return OutfitSlot.SHOES
    return None


def category_to_slots(category: str | None) -> set[OutfitSlot]:
    """Return the set of slots a category fills. Dresses fill both TOP and BOTTOM."""
    if not category:
        return set()
    if category.strip().lower() in DUAL_SLOT_CATEGORIES:
        return {OutfitSlot.TOP, OutfitSlot.BOTTOM}
    slot = category_to_slot(category)
    return {slot} if slot is not None else set()
