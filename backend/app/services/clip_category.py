from __future__ import annotations

from dataclasses import dataclass
from typing import List, Tuple, Dict, Optional
import torch
from PIL import Image
from transformers import CLIPProcessor, CLIPModel


@dataclass
class Prediction:
    label: str
    confidence: float
    topk: List[Tuple[str, float]]


class ClipCategoryClassifier:
    """
    CLIP zero-shot classifier for wardrobe item categories.

    - Loads CLIP model once.
    - Precomputes text embeddings for your label set + prompt templates.
    - For each image: compute image embedding once, then cosine similarity with cached text embeddings.
    """

    def __init__(
        self,
        labels: List[str],
        model_id: str = "openai/clip-vit-base-patch32",
        device: Optional[str] = None,
        templates: Optional[List[str]] = None,
    ):
        self.labels = labels
        self.device = device or ("cuda" if torch.cuda.is_available() else "cpu")

        self.model = CLIPModel.from_pretrained(model_id).to(self.device)
        self.processor = CLIPProcessor.from_pretrained(model_id)
        self.model.eval()

        self.templates = templates or [
            "a product photo of a {}",
            "a studio photo of a {}",
            "an isolated {}",
            "a clothing item: {}",
        ]

        # cache: label -> (num_templates, embed_dim)
        self._text_embeds: Dict[str, torch.Tensor] = {}
        self._build_text_cache()

    @torch.no_grad()
    def _build_text_cache(self) -> None:
        """
        Build normalized text embeddings for each label and each template.
        Store as tensor [T, D] per label.
        """
        for label in self.labels:
            prompts = [t.format(label) for t in self.templates]
            inputs = self.processor(text=prompts, return_tensors="pt", padding=True).to(self.device)

            text_features = self.model.get_text_features(**inputs)
            text_features = text_features / text_features.norm(dim=-1, keepdim=True)

            self._text_embeds[label] = text_features  # shape [T, D]

    @staticmethod
    def _softmax(x: torch.Tensor) -> torch.Tensor:
        # stable softmax
        x = x - x.max()
        return torch.exp(x) / torch.exp(x).sum()

    @torch.no_grad()
    def predict(self, image_path: str, top_k: int = 3) -> Prediction:
        """
        Returns best label + confidence + topk.

        Confidence here is softmax over aggregated label similarities.
        (Good enough for gating; not a calibrated probability.)
        """
        img = Image.open(image_path).convert("RGB")
        inputs = self.processor(images=img, return_tensors="pt").to(self.device)

        image_features = self.model.get_image_features(**inputs)
        image_features = image_features / image_features.norm(dim=-1, keepdim=True)  # [1, D]

        # aggregate score per label by averaging template similarities
        scores = []
        for label in self.labels:
            text_feats = self._text_embeds[label]  # [T, D]
            # cosine similarity: (1,D) dot (T,D)^T -> (T,)
            sim = (image_features @ text_feats.T).squeeze(0)  # [T]
            score = sim.mean()  # average over templates
            scores.append((label, score))

        # convert to tensor for softmax confidence
        score_tensor = torch.stack([s for _, s in scores])
        probs = self._softmax(score_tensor)

        # pick top-k
        idx_sorted = torch.argsort(probs, descending=True)
        topk = []
        for i in idx_sorted[:top_k]:
            label = scores[int(i)][0]
            topk.append((label, float(probs[int(i)].item())))

        best_label, best_prob = topk[0]
        return Prediction(label=best_label, confidence=best_prob, topk=topk)
