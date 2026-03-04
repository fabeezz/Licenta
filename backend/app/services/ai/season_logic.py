from __future__ import annotations
from typing import Optional

def infer_season(category: Optional[str], material: Optional[str]) -> str:
    category = (category or "").lower()
    material = (material or "").lower()

    # 1. FOOLPROOF CATEGORIES (Material doesn't matter)
    if category in {"shorts", "t-shirt"}:
        return "summer"
    
    if category in {"coat", "puffer", "sweater", "hoodie"}:
        return "winter" # Or "autumn/winter" depending on your preference

    # 2. AMBIGUOUS CATEGORIES (Use material to decide)
    if category in {"jacket", "blazer", "pants", "dress", "skirt", "shirt"}:
        if material in {"knit", "leather"}:
            return "winter"
        if material in {"nylon", "denim"}:
            return "spring/autumn"
        if material == "cotton":
            # A cotton shirt/dress is usually summer/spring
            return "summer" 

    # Default fallback
    return "all-season"