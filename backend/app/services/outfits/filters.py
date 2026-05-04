from __future__ import annotations

from app.models.item import Item


def item_matches_weather(item: Item, required_tags: list[str]) -> bool:
    """Item matches if its weather list intersects required tags OR includes 'all-weather'."""
    item_tags = set(item.weather or [])
    if "all-weather" in item_tags:
        return True
    return bool(item_tags.intersection(required_tags))


def item_matches_style(item: Item, style: str) -> bool:
    return not item.style or style in item.style


def prefer_explicit_style(items: list[Item], style: str) -> list[Item]:
    """Return items that explicitly carry *style*; falls back to all items if none do.

    Items with an empty style list are treated as 'any style' by item_matches_style,
    but when composing a styled outfit we want explicit matches to dominate so every
    slot feels cohesive.  If the wardrobe has no explicitly-tagged items for a slot,
    we fall back to the full list so the outfit can still be built.
    """
    explicit = [i for i in items if style in (i.style or [])]
    return explicit if explicit else items
