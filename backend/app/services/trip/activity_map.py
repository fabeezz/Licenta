from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class Activity:
    key: str
    label: str
    style: str  # casual | formal | sporty
    section: str


ACTIVITIES: list[Activity] = [
    # Physical
    Activity("walking_tours", "Walking tours", "casual", "Physical Activities"),
    Activity("jogging", "Jogging", "sporty", "Physical Activities"),
    Activity("cycling", "Cycling", "sporty", "Physical Activities"),
    Activity("yoga", "Yoga", "sporty", "Physical Activities"),
    Activity("gym", "Gym session", "sporty", "Physical Activities"),
    Activity("swimming", "Swimming", "sporty", "Physical Activities"),
    Activity("hiking", "Hiking", "sporty", "Physical Activities"),
    # Culture & Entertainment
    Activity("art_galleries", "Art galleries", "casual", "Culture & Entertainment"),
    Activity("museum_visits", "Museum visits", "casual", "Culture & Entertainment"),
    Activity("live_music", "Live music", "casual", "Culture & Entertainment"),
    Activity("theater", "Theater shows", "formal", "Culture & Entertainment"),
    Activity("cinema", "Cinema", "casual", "Culture & Entertainment"),
    Activity("opera", "Opera / Ballet", "formal", "Culture & Entertainment"),
    # Food & Drink
    Activity("cafe_hopping", "Cafe hopping", "casual", "Food & Drink"),
    Activity("wine_bars", "Wine bars", "formal", "Food & Drink"),
    Activity("fine_dining", "Fine dining", "formal", "Food & Drink"),
    Activity("food_markets", "Food markets", "casual", "Food & Drink"),
    # Outdoor & Nature
    Activity("park_visits", "Park visits", "casual", "Outdoor & Nature"),
    Activity("river_cruise", "River cruise", "casual", "Outdoor & Nature"),
    Activity("scenic_walks", "Scenic walks", "casual", "Outdoor & Nature"),
    Activity("beach", "Beach", "sporty", "Outdoor & Nature"),
]

ACTIVITY_BY_KEY: dict[str, Activity] = {a.key: a for a in ACTIVITIES}


def dominant_style(activity_keys: list[str]) -> str:
    """Return the dominant style for a list of activity keys.

    Priority: formal > sporty > casual.
    """
    styles = {ACTIVITY_BY_KEY[k].style for k in activity_keys if k in ACTIVITY_BY_KEY}
    if "formal" in styles:
        return "formal"
    if "sporty" in styles:
        return "sporty"
    return "casual"
