CATEGORIES_EN = [
    "t-shirt", "shirt", "hoodie", "sweater",
    "jacket", "coat", "jeans", "pants", "shorts",
    "skirt", "dress", "blazer",
    "sneakers", "shoes", "boots",
    "bag", "hat"
]

# The absolute basics. If CLIP gets it wrong here, it's an edge case.
MATERIAL_LABELS = [
    "cotton",   # Tees, hoodies, casual pants
    "denim",    # Jeans, jean jackets
    "leather",  # Leather jackets, boots, bags
    "knit",     # Sweaters, beanies
    "nylon"     # Rain jackets, windbreakers, sporty stuff
]

OCCASION_LABELS = [
    "casual", 
    "formal", 
    "sportswear" 
]

CLIP_MODEL_ID = "openai/clip-vit-base-patch32"
