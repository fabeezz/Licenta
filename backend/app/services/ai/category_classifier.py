from __future__ import annotations
from dataclasses import dataclass
from typing import List, Tuple
from PIL import Image

from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier, AttributePrediction

@dataclass
class ClipPrediction:
    label: str
    confidence: float
    topk: List[Tuple[str, float]]

class ClipCategoryClassifier:
    """
    Thin wrapper around ClipAttributeClassifier specialized for wardrobe categories.
    """
    def __init__(self, base_classifier: ClipAttributeClassifier):
        # We now accept the pre-loaded base classifier via Dependency Injection
        self._base = base_classifier
        self._templates: List[str] = [
            "a photo of a {}",
            "a product photo of a {}",
            "a {} isolated on a white background",
            "a studio shot of a {}",
            "an item of clothing: {}",
        ]

    def predict(
        self,
        rgb_img: Image.Image,
        labels: List[str],
        top_k: int = 3,
    ) -> ClipPrediction:
        attr_pred: AttributePrediction = self._base.predict(
            rgb_img,
            labels,
            self._templates,
            top_k=top_k,
        )

        return ClipPrediction(
            label=attr_pred.label,
            confidence=attr_pred.confidence,
            topk=attr_pred.topk,
        )