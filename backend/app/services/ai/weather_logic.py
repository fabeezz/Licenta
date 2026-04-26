from __future__ import annotations
from typing import Optional


def infer_weather(category: Optional[str], material: Optional[str]) -> list[str]:
    category = (category or "").lower()
    material = (material or "").lower()

    if category in {"shorts", "t-shirt"}:
        return ["warm"]

    if category in {"coat", "puffer"}:
        return ["cold"]

    if category in {"sweater", "hoodie"}:
        return ["cold"]

    if category in {"jacket", "blazer", "pants", "dress", "skirt", "shirt"}:
        if material == "knit":
            return ["cold"]
        if material == "nylon":
            return ["rainy", "cold"]
        if material in {"denim", "cotton"}:
            return ["all-weather"]
        if material == "leather":
            return ["cold"]

    return ["all-weather"]
