# app/services/clip_attribute_classifier.py
from __future__ import annotations
from dataclasses import dataclass
from typing import List, Tuple

import torch
from PIL import Image
from transformers import CLIPProcessor, CLIPModel


@dataclass
class AttributePrediction:
    label: str
    confidence: float
    topk: List[Tuple[str, float]]


class ClipAttributeClassifier:
    """
    Generic CLIP-based zero-shot classifier for semantic attributes
    (material, occasion, etc.), given a list of labels and templates.
    """

    def __init__(self, model_id: str = "openai/clip-vit-base-patch32"):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        self.model = CLIPModel.from_pretrained(model_id).to(self.device)
        self.processor = CLIPProcessor.from_pretrained(model_id)
        self.model.eval()

    def _build_prompts(self, labels: List[str], templates: List[str]) -> tuple[list[str], list[int]]:
        all_prompts: list[str] = []
        prompt_counts: list[int] = []
        for label in labels:
            ts = [t.format(label) for t in templates]
            all_prompts.extend(ts)
            prompt_counts.append(len(ts))
        return all_prompts, prompt_counts

    @torch.no_grad()
    def predict(
        self,
        rgb_img: Image.Image,
        labels: List[str],
        templates: List[str],
        top_k: int = 3,
    ) -> AttributePrediction:
        all_prompts, prompt_counts = self._build_prompts(labels, templates)

        inputs = self.processor(
            text=all_prompts,
            images=rgb_img,
            return_tensors="pt",
            padding=True,
        ).to(self.device)

        outputs = self.model(**inputs)
        probs = outputs.logits_per_image.softmax(dim=-1).squeeze(0)

        label_scores: list[Tuple[str, float]] = []
        idx = 0
        for label, count in zip(labels, prompt_counts):
            score = probs[idx : idx + count].mean().item()
            label_scores.append((label, score))
            idx += count

        label_scores.sort(key=lambda x: x[1], reverse=True)
        topk = label_scores[:top_k]
        best_label, best_score = topk[0]

        return AttributePrediction(
            label=best_label,
            confidence=best_score,
            topk=topk,
        )