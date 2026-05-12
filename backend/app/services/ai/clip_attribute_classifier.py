from __future__ import annotations
from dataclasses import dataclass
from typing import List, Tuple

import torch
import torch.nn.functional as F
from PIL import Image

import open_clip


@dataclass
class AttributePrediction:
    label: str
    confidence: float
    topk: List[Tuple[str, float]]


class ClipAttributeClassifier:
    def __init__(self, model_id: str = "Marqo/marqo-fashionCLIP"):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        hf_id = f"hf-hub:{model_id}"
        self.model, _, self.preprocess = open_clip.create_model_and_transforms(hf_id)
        self.tokenizer = open_clip.get_tokenizer(hf_id)
        self.model = self.model.to(self.device)
        self.model.eval()

    @torch.no_grad()
    def encode_image(self, rgb_img: Image.Image) -> torch.Tensor:
        """Encode a single image and return an L2-normalized embedding (1, D)."""
        tensor = self.preprocess(rgb_img).unsqueeze(0).to(self.device)
        feats = self.model.encode_image(tensor)
        return F.normalize(feats, dim=-1)

    @torch.no_grad()
    def encode_text(self, text: str) -> torch.Tensor:
        """Encode a text query and return an L2-normalized embedding (1, D)."""
        tokens = self.tokenizer([text]).to(self.device)
        feats = self.model.encode_text(tokens)
        return F.normalize(feats, dim=-1)

    @torch.no_grad()
    def precompute_label_embeds(
        self,
        labels: List[str],
        label_templates: List[List[str]],
    ) -> torch.Tensor:
        """Tokenize + encode all label prompts; return L2-normalized matrix (N, D).

        label_templates[i] is the list of prompt strings for labels[i].
        All prompts are batched in one forward pass, then averaged per label.
        """
        all_prompts: list[str] = []
        prompt_counts: list[int] = []
        for templates in label_templates:
            all_prompts.extend(templates)
            prompt_counts.append(len(templates))

        tokens = self.tokenizer(all_prompts).to(self.device)
        all_text_feats = F.normalize(self.model.encode_text(tokens), dim=-1)

        label_embeds: list[torch.Tensor] = []
        idx = 0
        for count in prompt_counts:
            label_embeds.append(all_text_feats[idx : idx + count].mean(dim=0))
            idx += count

        return F.normalize(torch.stack(label_embeds), dim=-1)

    @torch.no_grad()
    def score_against_cached(
        self,
        image_embed: torch.Tensor,
        label_embeds_t: torch.Tensor,
        labels: List[str],
        top_k: int = 3,
    ) -> AttributePrediction:
        """Cosine + temperature softmax against a precomputed label embedding matrix."""
        temperature = self.model.logit_scale.exp()
        cosine_sims = (image_embed @ label_embeds_t.T).squeeze(0)
        probs = (cosine_sims * temperature).softmax(dim=-1)

        label_scores = [(label, probs[i].item()) for i, label in enumerate(labels)]
        label_scores.sort(key=lambda x: x[1], reverse=True)
        topk = label_scores[:top_k]
        return AttributePrediction(label=topk[0][0], confidence=topk[0][1], topk=topk)

    @torch.no_grad()
    def score_labels(
        self,
        image_embed: torch.Tensor,
        labels: List[str],
        templates: List[str],
        top_k: int = 3,
    ) -> AttributePrediction:
        """Score labels against a pre-computed image embedding.

        Averages template text embeddings per label in embedding space,
        then computes temperature-scaled cosine softmax across all labels.
        """
        label_templates = [[t.format(label) for t in templates] for label in labels]
        label_embeds_t = self.precompute_label_embeds(labels, label_templates)
        return self.score_against_cached(image_embed, label_embeds_t, labels, top_k)

    @torch.no_grad()
    def score_labels_multi(
        self,
        image_embed: torch.Tensor,
        labels: List[str],
        label_templates: List[List[str]],
        top_k: int = 3,
    ) -> AttributePrediction:
        """Like score_labels but accepts a separate template list per label."""
        label_embeds_t = self.precompute_label_embeds(labels, label_templates)
        return self.score_against_cached(image_embed, label_embeds_t, labels, top_k)

