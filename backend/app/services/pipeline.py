from __future__ import annotations

import logging
from dataclasses import dataclass
from typing import Any, Optional
from PIL import Image

from app.core.logging import log_latency
from app.services.storage import save_upload
from app.services.image.bg_removal import remove_background
from app.services.image.preprocess import ImagePrepConfig, load_prepared_rgb
from app.services.image.color_extractor_colorthief import ColorThiefExtractor
from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier, AttributePrediction
from app.services.ai.category_classifier import ClipCategoryClassifier
from app.services.ai.material_classifier import ClipMaterialClassifier
from app.services.ai.style_classifier import ClipStyleClassifier
from app.services.ai.config import CONFIDENCE_THRESHOLDS
from app.services.ai.weather_logic import infer_weather

logger = logging.getLogger(__name__)


@dataclass(frozen=True)
class PipelineResult:
    image_original_name: str
    image_no_bg_name: str
    color_tags: dict[str, Any]
    category: Optional[str]
    category_confidence: Optional[float]
    category_topk: list[Any]
    material: Optional[str]
    material_confidence: Optional[float]
    style: list[str]
    weather: list[str]


def _threshold(pred: AttributePrediction, attr: str) -> tuple[Optional[str], Optional[float]]:
    if pred.confidence < CONFIDENCE_THRESHOLDS.get(attr, 0.0):
        return None, None
    return pred.label, pred.confidence


class ItemPipeline:
    def __init__(
        self,
        color_extractor: ColorThiefExtractor,
        base_classifier: ClipAttributeClassifier,
        category_classifier: ClipCategoryClassifier,
        material_classifier: ClipMaterialClassifier,
        style_classifier: ClipStyleClassifier,
        categories_en: list[str],
        prep_cfg: ImagePrepConfig | None = None,
    ) -> None:
        self.color_extractor = color_extractor
        self.base_classifier = base_classifier
        self.category_classifier = category_classifier
        self.material_classifier = material_classifier
        self.style_classifier = style_classifier
        self.categories_en = categories_en
        self.prep_cfg = prep_cfg or ImagePrepConfig(max_size=400, crop_to_alpha=True)

    def process_upload(self, file_bytes: bytes, ext: str) -> PipelineResult:
        with log_latency("pipeline.process_upload", logger):
            logger.info("Starting pipeline for upload (ext=%s)", ext)

            with log_latency("pipeline.save_upload", logger):
                original_name = save_upload(file_bytes, ext)
            logger.debug("Saved original image: %s", original_name)

            with log_latency("pipeline.remove_background", logger):
                nobg_name = remove_background(original_name)
            logger.debug("Background removed: %s -> %s", original_name, nobg_name)

            rgb_img: Image.Image = load_prepared_rgb(nobg_name, self.prep_cfg)

            with log_latency("pipeline.color_extract", logger):
                colors = self.color_extractor.extract(rgb_img)
            logger.debug("Extracted colors: %s", colors)

            with log_latency("pipeline.encode_image", logger):
                image_embed = self.base_classifier.encode_image(rgb_img)

            with log_latency("pipeline.category", logger):
                cat_pred = self.category_classifier.score(image_embed, self.categories_en, top_k=3)
            logger.info("Predicted category: %s (conf=%.3f)", cat_pred.label, cat_pred.confidence)

            with log_latency("pipeline.material", logger):
                mat_pred = self.material_classifier.score(image_embed, cat_pred.label)
            logger.info("Predicted material: %s (conf=%.3f)", mat_pred.label, mat_pred.confidence)

            with log_latency("pipeline.style", logger):
                style_threshold = CONFIDENCE_THRESHOLDS.get("style", 0.30)
                style_results = self.style_classifier.score_multi(image_embed, cat_pred.label, threshold=style_threshold)
            logger.info("Predicted style: %s", style_results)

            # Use raw (pre-threshold) category/material for weather — rule-based, doesn't need confidence
            weather = infer_weather(cat_pred.label, mat_pred.label)
            logger.info("Inferred weather: %s", weather)

            category, category_confidence = _threshold(cat_pred, "category")
            material, material_confidence = _threshold(mat_pred, "material")
            style = [lbl for lbl, _ in style_results]

        return PipelineResult(
            image_original_name=original_name,
            image_no_bg_name=nobg_name,
            color_tags=colors,
            category=category,
            category_confidence=category_confidence,
            category_topk=cat_pred.topk,
            material=material,
            material_confidence=material_confidence,
            style=style,
            weather=weather,
        )
