from __future__ import annotations
from typing import List

import torch

from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier, AttributePrediction


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
        self._cached_labels: List[str] | None = None
        self._cached_embeds: torch.Tensor | None = None

    def prewarm(self, labels: List[str]) -> None:
        """Precompute and cache label embeddings for the given label set."""
        label_templates = [[t.format(lbl) for t in self._templates] for lbl in labels]
        self._cached_labels = labels
        self._cached_embeds = self._base.precompute_label_embeds(labels, label_templates)

    def score(
        self,
        image_embed: torch.Tensor,
        labels: List[str],
        top_k: int = 3,
    ) -> AttributePrediction:
        if self._cached_embeds is not None and self._cached_labels == labels:
            return self._base.score_against_cached(
                image_embed, self._cached_embeds, labels, top_k
            )
        return self._base.score_labels(image_embed, labels, self._templates, top_k=top_k)
