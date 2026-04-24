from __future__ import annotations

import logging
from dataclasses import dataclass
from typing import Any
from PIL import Image

from app.core.logging import log_latency
from app.services.storage import save_upload
from app.services.image.bg_removal import remove_background
from app.services.image.preprocess import ImagePrepConfig, load_prepared_rgb
from app.services.image.color_extractor_colorthief import ColorThiefExtractor
from app.services.ai.category_classifier import ClipCategoryClassifier, ClipPrediction
from app.services.ai.material_classifier import ClipMaterialClassifier
from app.services.ai.occasion_classifier import ClipOccasionClassifier
from app.services.ai.season_logic import infer_season

logger = logging.getLogger(__name__)


@dataclass(frozen=True)
class PipelineResult:
    """Typed output of :meth:`ItemPipeline.process_upload`.

    Attributes:
        image_original_name: Filename of the saved original upload.
        image_no_bg_name: Filename of the background-removed image.
        color_tags: Color analysis dict (``dominant``, ``palette``, …).
        category: Predicted clothing category label.
        category_confidence: Confidence score for the category prediction.
        category_topk: Top-k category predictions.
        material: Predicted material label.
        material_confidence: Confidence score for the material prediction.
        occasion: Predicted occasion label.
        occasion_confidence: Confidence score for the occasion prediction.
        season: Rule-inferred season string.
    """

    image_original_name: str
    image_no_bg_name: str
    color_tags: dict[str, Any]
    category: str
    category_confidence: float
    category_topk: list[Any]
    material: str
    material_confidence: float
    occasion: str
    occasion_confidence: float
    season: str


class ItemPipeline:
    """Orchestrates the full item-processing pipeline.

    Accepts all collaborators via constructor injection so they can be
    replaced in tests without touching service code.

    Args:
        color_extractor: Extracts dominant and palette colors from an image.
        category_classifier: CLIP-based clothing category predictor.
        material_classifier: CLIP-based material predictor.
        occasion_classifier: CLIP-based occasion predictor.
        categories_en: List of category label strings for CLIP prompts.
        prep_cfg: Image pre-processing configuration.
    """

    def __init__(
        self,
        color_extractor: ColorThiefExtractor,
        category_classifier: ClipCategoryClassifier,
        material_classifier: ClipMaterialClassifier,
        occasion_classifier: ClipOccasionClassifier,
        categories_en: list[str],
        prep_cfg: ImagePrepConfig | None = None,
    ) -> None:
        self.color_extractor = color_extractor
        self.category_classifier = category_classifier
        self.material_classifier = material_classifier
        self.occasion_classifier = occasion_classifier
        self.categories_en = categories_en
        self.prep_cfg = prep_cfg or ImagePrepConfig(max_size=400, crop_to_alpha=True)

    def process_upload(self, file_bytes: bytes, ext: str) -> PipelineResult:
        """Run the full pipeline on a raw upload and return a typed result.

        Args:
            file_bytes: Raw bytes of the uploaded image file.
            ext: File extension without the leading dot (e.g. ``"png"``).

        Returns:
            :class:`PipelineResult` containing predictions and image filenames.
        """
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

            with log_latency("pipeline.category", logger):
                pred: ClipPrediction = self.category_classifier.predict(
                    rgb_img, self.categories_en, top_k=3
                )
            logger.info("Predicted category: %s (conf=%.3f)", pred.label, pred.confidence)

            with log_latency("pipeline.material", logger):
                mat_pred = self.material_classifier.predict(rgb_img, pred.label)
            logger.info("Predicted material: %s (conf=%.3f)", mat_pred.label, mat_pred.confidence)

            with log_latency("pipeline.occasion", logger):
                occ_pred = self.occasion_classifier.predict(rgb_img, pred.label)
            logger.info("Predicted occasion: %s (conf=%.3f)", occ_pred.label, occ_pred.confidence)

            season = infer_season(pred.label, mat_pred.label)
            logger.info("Inferred season: %s", season)

        return PipelineResult(
            image_original_name=original_name,
            image_no_bg_name=nobg_name,
            color_tags=colors,
            category=pred.label,
            category_confidence=pred.confidence,
            category_topk=pred.topk,
            material=mat_pred.label,
            material_confidence=mat_pred.confidence,
            occasion=occ_pred.label,
            occasion_confidence=occ_pred.confidence,
            season=season,
        )
