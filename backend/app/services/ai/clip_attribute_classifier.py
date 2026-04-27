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
        # open_clip loads Marqo models via the hf-hub: prefix
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
        all_prompts: list[str] = []
        prompt_counts: list[int] = []
        for label in labels:
            prompts = [t.format(label) for t in templates]
            all_prompts.extend(prompts)
            prompt_counts.append(len(prompts))

        tokens = self.tokenizer(all_prompts).to(self.device)
        all_text_feats = F.normalize(self.model.encode_text(tokens), dim=-1)

        label_embeds: list[torch.Tensor] = []
        idx = 0
        for count in prompt_counts:
            label_embeds.append(all_text_feats[idx : idx + count].mean(dim=0))
            idx += count

        label_embeds_t = F.normalize(torch.stack(label_embeds), dim=-1)
        temperature = self.model.logit_scale.exp()
        cosine_sims = (image_embed @ label_embeds_t.T).squeeze(0)
        probs = (cosine_sims * temperature).softmax(dim=-1)

        label_scores = [(label, probs[i].item()) for i, label in enumerate(labels)]
        label_scores.sort(key=lambda x: x[1], reverse=True)
        topk = label_scores[:top_k]

        return AttributePrediction(label=topk[0][0], confidence=topk[0][1], topk=topk)

    @torch.no_grad()
    def score_labels_multi(
        self,
        image_embed: torch.Tensor,
        labels: List[str],
        label_templates: List[List[str]],
        top_k: int = 3,
    ) -> AttributePrediction:
        """Like score_labels but accepts a separate template list per label."""
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

        label_embeds_t = F.normalize(torch.stack(label_embeds), dim=-1)
        temperature = self.model.logit_scale.exp()
        cosine_sims = (image_embed @ label_embeds_t.T).squeeze(0)
        probs = (cosine_sims * temperature).softmax(dim=-1)

        label_scores = [(label, probs[i].item()) for i, label in enumerate(labels)]
        label_scores.sort(key=lambda x: x[1], reverse=True)
        topk = label_scores[:top_k]

        return AttributePrediction(label=topk[0][0], confidence=topk[0][1], topk=topk)

    @torch.no_grad()
    def predict(
        self,
        rgb_img: Image.Image,
        labels: List[str],
        templates: List[str],
        top_k: int = 3,
    ) -> AttributePrediction:
        """Convenience wrapper: encode image then score labels."""
        return self.score_labels(self.encode_image(rgb_img), labels, templates, top_k=top_k)
