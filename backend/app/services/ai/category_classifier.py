from __future__ import annotations
from dataclasses import dataclass
from typing import List, Tuple

import torch
from PIL import Image

from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier, AttributePrediction


# Kept for backward compatibility with any code that imports ClipPrediction
@dataclass
class ClipPrediction:
    label: str
    confidence: float
    topk: List[Tuple[str, float]]


class ClipCategoryClassifier:
    def __init__(self, base_classifier: ClipAttributeClassifier):
        self._base = base_classifier
        self._templates: List[str] = [
            "a photo of a {}",
            "a product photo of a {}",
            "an e-commerce listing photo of a {}",
            "a flat lay photo of a {}",
            "a studio shot of a {} on a white background",
            "a {} isolated on a transparent background",
            "a clothing item: {}",
            "a wardrobe photo of a {}",
            "a fashion catalog image of a {}",
            "the garment shown is a {}",
        ]

    def score(
        self,
        image_embed: torch.Tensor,
        labels: List[str],
        top_k: int = 3,
    ) -> AttributePrediction:
        return self._base.score_labels(image_embed, labels, self._templates, top_k=top_k)

    def predict(
        self,
        rgb_img: Image.Image,
        labels: List[str],
        top_k: int = 3,
    ) -> ClipPrediction:
        pred = self._base.predict(rgb_img, labels, self._templates, top_k=top_k)
        return ClipPrediction(label=pred.label, confidence=pred.confidence, topk=pred.topk)
