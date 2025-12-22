# app/services/category_classifier_clip.py
from __future__ import annotations
from dataclasses import dataclass
from typing import List, Tuple

import torch
from PIL import Image
from transformers import CLIPProcessor, CLIPModel

@dataclass
class ClipPrediction:
    label: str
    confidence: float
    topk: List[Tuple[str, float]]

class ClipCategoryClassifier:
    def __init__(self, model_id: str = "openai/clip-vit-base-patch32"):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        self.model = CLIPModel.from_pretrained(model_id).to(self.device)
        self.processor = CLIPProcessor.from_pretrained(model_id)
        self.model.eval()

    def _templates(self, label: str) -> List[str]:
        # English prompts, good for product/no-bg shots
        return [
            f"a photo of a {label}",
            f"a product photo of a {label}",
            f"a {label} isolated on a white background",
            f"a studio shot of a {label}",
            f"an item of clothing: {label}",
        ]

    @torch.no_grad()
    def predict(self, rgb_img: Image.Image, labels: List[str], top_k: int = 3) -> ClipPrediction:
        # 1) build prompt list
        all_prompts: List[str] = []
        prompt_counts: List[int] = []
        for l in labels:
            ts = self._templates(l)
            all_prompts.extend(ts)
            prompt_counts.append(len(ts))

        # 2) single forward pass
        inputs = self.processor(
            text=all_prompts,
            images=rgb_img,
            return_tensors="pt",
            padding=True
        ).to(self.device)

        outputs = self.model(**inputs)

        # 3) probabilities over all prompts
        probs = outputs.logits_per_image.softmax(dim=-1).squeeze(0)

        # 4) aggregate per label
        label_scores: List[Tuple[str, float]] = []
        idx = 0
        for label, count in zip(labels, prompt_counts):
            score = probs[idx: idx + count].mean().item()
            label_scores.append((label, score))
            idx += count

        label_scores.sort(key=lambda x: x[1], reverse=True)

        return ClipPrediction(
            label=label_scores[0][0],
            confidence=label_scores[0][1],
            topk=label_scores[:top_k]
        )
