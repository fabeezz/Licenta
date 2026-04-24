from __future__ import annotations
from typing import Optional

def infer_season(category: Optional[str], material: Optional[str]) -> str:
    category = (category or "").lower()
    material = (material or "").lower()

    # 1. FOOLPROOF CATEGORIES
    if category in {"shorts", "t-shirt"}:
        return "summer"
    
    if category in {"coat", "puffer"}:
        return "winter"

    if category in {"sweater", "hoodie"}:
        return "autumn"

    # 2. AMBIGUOUS CATEGORIES (Use material to decide)
    if category in {"jacket", "blazer", "pants", "dress", "skirt", "shirt"}:
        if material in {"knit"}:
            return "winter"
        if material in {"nylon", "denim"}:
            return "autumn"
        if material in {"cotton", "leather"}:
            return "spring" 

    # Default fallback
    return "all-season"