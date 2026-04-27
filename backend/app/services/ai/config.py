from typing import Dict

CATEGORIES_EN = [
    "t-shirt", "shirt", "hoodie", "sweater",
    "jacket", "coat", "jeans", "pants", "shorts",
    "skirt", "dress", "blazer",
    "sneakers", "shoes", "boots",
    "bag", "hat"
]

MATERIAL_LABELS = [
    "cotton",
    "denim",
    "leather",
    "knit",
    "nylon"
]

STYLE_LABELS = [
    "casual",
    "formal",
    "sporty",
]

CLIP_MODEL_ID = "Marqo/marqo-fashionSigLIP"

CONFIDENCE_THRESHOLDS: Dict[str, float] = {
    "category": 0.30,
    "material": 0.35,
    "style": 0.30,
}
