from __future__ import annotations
from collections import defaultdict
from typing import Dict, FrozenSet, List, Optional

import torch
from PIL import Image

from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier, AttributePrediction

# Fine-grained internal labels → canonical 5
_INTERNAL_TO_CANONICAL: Dict[str, str] = {
    "cotton": "cotton",
    "jersey cotton": "cotton",
    "cotton blend": "cotton",
    "denim": "denim",
    "raw denim": "denim",
    "washed denim": "denim",
    "leather": "leather",
    "faux leather": "leather",
    "suede": "leather",
    "knit": "knit",
    "wool knit": "knit",
    "ribbed knit": "knit",
    "fleece": "knit",
    "nylon": "nylon",
    "polyester": "nylon",
    "technical fabric": "nylon",
}

_INTERNAL_LABELS: List[str] = list(_INTERNAL_TO_CANONICAL.keys())

# Per-category allowed canonical materials.
# Restricts the candidate set so color-driven errors (e.g. brown hoodie → leather) are ruled out.
# Fallback (unknown/None category): all 5 canonical materials are allowed.
_CATEGORY_TO_ALLOWED: Dict[str, FrozenSet[str]] = {
    "t-shirt":   frozenset({"cotton"}),
    "shirt":     frozenset({"cotton"}),
    "hoodie":    frozenset({"cotton", "knit"}),
    "sweater":   frozenset({"cotton", "knit"}),
    "jeans":     frozenset({"denim"}),
    "pants":     frozenset({"cotton", "denim", "knit", "nylon"}),
    "shorts":    frozenset({"cotton", "denim", "knit", "nylon"}),
    "skirt":     frozenset({"cotton", "denim", "knit", "nylon"}),
    "dress":     frozenset({"cotton", "knit", "nylon"}),
    "jacket":    frozenset({"cotton", "denim", "leather", "knit", "nylon"}),
    "coat":      frozenset({"cotton", "denim", "leather", "knit", "nylon"}),
    "blazer":    frozenset({"cotton", "knit"}),
    "sneakers":  frozenset({"leather", "nylon"}),
    "shoes":     frozenset({"leather", "nylon"}),
    "boots":     frozenset({"leather", "nylon"}),
    "bag":       frozenset({"leather", "nylon", "cotton"}),
    "hat":       frozenset({"cotton", "knit", "nylon"}),
}

_ALL_CANONICALS: FrozenSet[str] = frozenset(_INTERNAL_TO_CANONICAL.values())

# Texture-anchored prompts per canonical bucket.
# One `{}` slot = category. Emphasises weave/surface over material name to reduce colour leakage.
_CANONICAL_TEMPLATES: Dict[str, List[str]] = {
    "cotton": [
        "a {} in soft woven cotton fabric",
        "a {} made of matte cotton with a natural weave",
        "a product photo of a cotton {}",
    ],
    "knit": [
        "a {} with visible knit stitches and yarn loops",
        "a {} in chunky wool knit texture",
        "a product photo of a knitted {}",
    ],
    "denim": [
        "a {} in woven denim with twill diagonal weave",
        "a {} made of rigid cotton denim fabric",
        "a product photo of a denim {}",
    ],
    "leather": [
        "a {} with a smooth shiny leather surface",
        "a {} made of grained leather hide",
        "a product photo of a leather {}",
    ],
    "nylon": [
        "a {} in synthetic technical fabric with smooth sheen",
        "a {} made of lightweight nylon with a slick finish",
        "a product photo of a nylon {}",
    ],
}


def _allowed_labels(category: Optional[str]) -> List[str]:
    """Return internal labels whose canonical bucket is allowed for this category."""
    canonical_set = _CATEGORY_TO_ALLOWED.get((category or "").lower().strip(), _ALL_CANONICALS)
    return [lbl for lbl in _INTERNAL_LABELS if _INTERNAL_TO_CANONICAL[lbl] in canonical_set]


def _build_label_templates(labels: List[str], cat: str) -> List[List[str]]:
    """Return a list-of-template-lists parallel to `labels`, with category pre-filled."""
    result = []
    for lbl in labels:
        canonical = _INTERNAL_TO_CANONICAL[lbl]
        templates = [t.format(cat) for t in _CANONICAL_TEMPLATES[canonical]]
        result.append(templates)
    return result


def _collapse(pred: AttributePrediction) -> AttributePrediction:
    scores: Dict[str, float] = defaultdict(float)
    for label, score in pred.topk:
        canonical = _INTERNAL_TO_CANONICAL.get(label, label)
        scores[canonical] += score
    ranked = sorted(scores.items(), key=lambda x: x[1], reverse=True)
    return AttributePrediction(label=ranked[0][0], confidence=ranked[0][1], topk=ranked[:3])


class ClipMaterialClassifier:
    def __init__(self, base_classifier: ClipAttributeClassifier) -> None:
        self._base = base_classifier

    def score(self, image_embed: torch.Tensor, category: Optional[str]) -> AttributePrediction:
        cat = (category or "clothing item").lower().strip()
        allowed = _allowed_labels(category)

        # Short-circuit: single canonical bucket allowed → no model call needed
        canonicals = {_INTERNAL_TO_CANONICAL[l] for l in allowed}
        if len(canonicals) == 1:
            label = next(iter(canonicals))
            return AttributePrediction(label=label, confidence=1.0, topk=[(label, 1.0)])

        label_templates = _build_label_templates(allowed, cat)
        pred = self._base.score_labels_multi(
            image_embed, allowed, label_templates, top_k=len(allowed)
        )
        return _collapse(pred)

    def predict(self, rgb_img: Image.Image, category: Optional[str]) -> AttributePrediction:
        return self.score(self._base.encode_image(rgb_img), category)
