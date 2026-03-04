from typing import List
from PIL import Image

from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier, AttributePrediction
from app.services.ai.config import OCCASION_LABELS

class ClipOccasionClassifier:
    def __init__(self, base_classifier: ClipAttributeClassifier) -> None:
        self._base = base_classifier
        self._labels: List[str] = OCCASION_LABELS
        
        # Better templates for isolated items instead of "outfits"
        self._templates = [
            "an item of {} clothing",
            "a piece of {} wear",
            "this garment is suitable for a {} occasion",
        ]

        # --- OCCASION OVERRIDES ---
        # Force the obvious items into their correct buckets
        self._overrides = {
            "t-shirt": "casual",
            "sweater": "casual",
            "jeans": "casual",
            "hoodie": "casual",
            "shorts": "casual",
            "sneakers": "casual",
            "blazer": "formal",
            "jacket": "casual",
        }

    def predict(self, rgb_img: Image.Image, category: str | None) -> AttributePrediction:
        cat = (category or "").lower().strip()

        # 1. Check if the category strictly dictates the occasion
        if cat in self._overrides:
            occ = self._overrides[cat]
            return AttributePrediction(
                label=occ,
                confidence=1.0,
                topk=[(occ, 1.0)]
            )

        # 2. If it's ambiguous (like a coat, dress, or shoes), ask CLIP
        return self._base.predict(
            rgb_img, 
            self._labels, 
            self._templates, 
            top_k=3
        )