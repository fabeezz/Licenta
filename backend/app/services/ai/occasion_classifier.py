from __future__ import annotations
from collections import defaultdict
from typing import Dict, List

import torch
from PIL import Image

from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier, AttributePrediction

# Internal fine-grained labels, collapsed to the canonical 3-occasion set
_INTERNAL_TO_CANONICAL: Dict[str, str] = {
    "casual": "casual",
    "smart casual": "casual",
    "loungewear": "casual",
    "formal": "formal",
    "business formal": "formal",
    "sportswear": "sportswear",
    "athleisure": "sportswear",
}

_INTERNAL_LABELS: List[str] = list(_INTERNAL_TO_CANONICAL.keys())

# Single-slot templates (occasion label fills {})
_TEMPLATES_SINGLE = [
    "an item of {} clothing",
    "a piece of {} wear",
    "clothing for {} occasions",
]

# Category-conditioned templates: first {} = category, second {} = occasion
_TEMPLATES_WITH_CAT = [
    "a {} outfit for {} occasions",
    "a {} suitable for {} settings",
    "a {} that is {} attire",
]


class ClipOccasionClassifier:
    def __init__(self, base_classifier: ClipAttributeClassifier) -> None:
        self._base = base_classifier

    def _build_templates(self, cat: str) -> List[str]:
        cat_conditioned = [t.format(cat, "{}") for t in _TEMPLATES_WITH_CAT]
        return list(_TEMPLATES_SINGLE) + cat_conditioned

    def score(self, image_embed: torch.Tensor, category: str | None) -> AttributePrediction:
        templates = self._build_templates((category or "clothing item").lower().strip())
        pred = self._base.score_labels(
            image_embed, _INTERNAL_LABELS, templates, top_k=len(_INTERNAL_LABELS)
        )
        return _collapse(pred)

    def predict(self, rgb_img: Image.Image, category: str | None) -> AttributePrediction:
        templates = self._build_templates((category or "clothing item").lower().strip())
        pred = self._base.predict(
            rgb_img, _INTERNAL_LABELS, templates, top_k=len(_INTERNAL_LABELS)
        )
        return _collapse(pred)


def _collapse(pred: AttributePrediction) -> AttributePrediction:
    scores: Dict[str, float] = defaultdict(float)
    for label, score in pred.topk:
        scores[_INTERNAL_TO_CANONICAL[label]] += score
    ranked = sorted(scores.items(), key=lambda x: x[1], reverse=True)
    return AttributePrediction(label=ranked[0][0], confidence=ranked[0][1], topk=ranked[:3])
