import logging
from typing import Any, Dict, List
from PIL import Image

from app.services.storage import save_upload
from app.services.image.bg_removal import remove_background
from app.services.image.preprocess import ImagePrepConfig, load_prepared_rgb
from app.services.image.color_extractor_colorthief import ColorThiefExtractor
from app.services.ai.category_classifier import ClipCategoryClassifier, ClipPrediction
from app.services.ai.material_classifier import ClipMaterialClassifier
from app.services.ai.occasion_classifier import ClipOccasionClassifier
from app.services.ai.season_logic import infer_season

logger = logging.getLogger(__name__)

class ItemPipeline:
    def __init__(
        self,
        color_extractor: ColorThiefExtractor,
        category_classifier: ClipCategoryClassifier,
        material_classifier: ClipMaterialClassifier,
        occasion_classifier: ClipOccasionClassifier,
        categories_en: List[str],
        prep_cfg: ImagePrepConfig = ImagePrepConfig(max_size=400, crop_to_alpha=True),
    ):
        self.color_extractor = color_extractor
        self.category_classifier = category_classifier
        self.material_classifier = material_classifier
        self.occasion_classifier = occasion_classifier
        self.categories_en = categories_en
        self.prep_cfg = prep_cfg

    def process_upload(self, file_bytes: bytes, ext: str) -> Dict[str, Any]:
        logger.info("Starting pipeline for upload (ext=%s)", ext)

        original_name = save_upload(file_bytes, ext)
        logger.debug("Saved original image: %s", original_name)

        nobg_name = remove_background(original_name)
        logger.debug("Background removed: %s -> %s", original_name, nobg_name)

        rgb_img: Image.Image = load_prepared_rgb(nobg_name, self.prep_cfg)
        colors = self.color_extractor.extract(rgb_img)
        logger.debug("Extracted colors: %s", colors)

        # category
        pred: ClipPrediction = self.category_classifier.predict(
            rgb_img, self.categories_en, top_k=3
        )
        logger.info("Predicted category: %s (conf=%.3f)", pred.label, pred.confidence)

        # material
        mat_pred = self.material_classifier.predict(rgb_img, pred.label)
        
        logger.info("Predicted material: %s (conf=%.3f)", mat_pred.label, mat_pred.confidence)

        # occasion
        occ_pred = self.occasion_classifier.predict(rgb_img, pred.label)
        logger.info("Predicted occasion: %s (conf=%.3f)", occ_pred.label, occ_pred.confidence)

        # season
        season = infer_season(pred.label, mat_pred.label)
        logger.info("Inferred season: %s", season)

        return {
            "image_original_name": original_name,
            "image_no_bg_name": nobg_name,
            "color_tags": colors,
            "category": pred.label,
            "category_confidence": pred.confidence,
            "category_topk": pred.topk,
            "material": mat_pred.label,
            "material_confidence": mat_pred.confidence,
            "occasion": occ_pred.label,
            "occasion_confidence": occ_pred.confidence,
            "season": season,
        }
