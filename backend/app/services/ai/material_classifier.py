from typing import List
from PIL import Image

from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier, AttributePrediction
from app.services.ai.config import MATERIAL_LABELS

class ClipMaterialClassifier:
    def __init__(self, base_classifier: ClipAttributeClassifier) -> None:
        self._base = base_classifier
        self._labels: List[str] = MATERIAL_LABELS

        self._templates = [
            "a photo of a {} made of {} material",
            "an isolated product shot of a {} made of {}",
            "the fabric texture of this {} is {}",
        ]

        # --- THE MVP OVERRIDE DICTIONARY ---
        self._overrides = {
            "jeans": "denim",
            "sweater": "knit",
            "hoodie": "cotton",
        }

    def predict(self, rgb_img: Image.Image, category: str | None) -> AttributePrediction:
        cat = (category or "clothing item").lower().strip()

        # Check if we have a hardcoded rule for this category
        if cat in self._overrides:
            mat = self._overrides[cat]
            return AttributePrediction(
                label=mat, 
                confidence=1.0, 
                topk=[(mat, 1.0)]
            )

        # If no override, let CLIP guess
        templates_with_one_slot = [t.format(cat, "{}") for t in self._templates]

        return self._base.predict(
            rgb_img,
            self._labels,
            templates_with_one_slot,
            top_k=3,
        )