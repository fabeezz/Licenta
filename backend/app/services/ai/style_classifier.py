from __future__ import annotations
from typing import Dict, List, Set, Tuple

import torch

from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier

_ALLOWED_BY_CATEGORY: Dict[str, Set[str]] = {
    "t-shirt":  {"casual", "sporty"},
    "shirt":    {"casual", "formal"},
    "hoodie":   {"casual", "sporty"},
    "sweater":  {"casual", "formal"},
    "jacket":   {"casual", "formal", "sporty"},
    "coat":     {"casual", "formal"},
    "jeans":    {"casual"},
    "pants":    {"casual", "formal", "sporty"},
    "shorts":   {"casual", "sporty"},
    "skirt":    {"casual", "formal"},
    "dress":    {"casual", "formal"},
    "blazer":   {"casual", "formal"},
    "sneakers": {"casual", "sporty"},
    "shoes":    {"casual", "formal"},
    "boots":    {"casual", "formal"},
    "bag":      {"casual", "formal", "sporty"},
    "hat":      {"casual", "formal", "sporty"},
}

_ALL_STYLES: Set[str] = {"casual", "formal", "sporty"}

# Per-style prompt lists — each {} is filled with the category name.
# Generic prompts (no {}) give signal when category is unknown.
_STYLE_TEMPLATES: Dict[str, List[str]] = {
    "casual": [
        "a casual clothing item for everyday wear",
        "a relaxed {} for daily casual use",
        "a {} worn in casual everyday settings",
        "a laid-back {} for leisure or weekend wear",
    ],
    "formal": [
        "a formal clothing item for business or office",
        "a tailored {} for professional or dressy occasions",
        "a {} suitable for formal events, office, or business settings",
        "an elegant {} worn at formal or smart occasions",
    ],
    "sporty": [
        "athletic sportswear for gym or exercise",
        "a {} designed for sports, running, or gym workouts",
        "a {} worn during athletic or fitness activities",
        "performance {} for active and sporty use",
    ],
}


class ClipStyleClassifier:
    def __init__(self, base_classifier: ClipAttributeClassifier) -> None:
        self._base = base_classifier

    def score_multi(
        self,
        image_embed: torch.Tensor,
        category: str | None,
        threshold: float = 0.30,
    ) -> List[Tuple[str, float]]:
        """Return all allowed styles above *threshold*, falling back to the best allowed."""
        cat = (category or "").lower().strip()
        allowed = _ALLOWED_BY_CATEGORY.get(cat, _ALL_STYLES)

        labels = sorted(allowed)  # stable order for reproducibility
        label_templates = [
            [t.format(cat) if "{}" in t else t for t in _STYLE_TEMPLATES[lbl]]
            for lbl in labels
        ]

        pred = self._base.score_labels_multi(
            image_embed, labels, label_templates, top_k=len(labels)
        )

        # pred.topk is already sorted desc; re-normalize within the allowed subset
        # (softmax was over all labels, but we restricted labels to allowed, so it's fine)
        above = [(lbl, conf) for lbl, conf in pred.topk if conf >= threshold]
        if not above:
            above = [pred.topk[0]]  # guaranteed: at least top-scoring allowed label

        return above
