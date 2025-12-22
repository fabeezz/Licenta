# app/services/pipeline.py
from typing import Any, Dict, List
from PIL import Image

from app.services.storage import save_upload
from app.services.bg_removal import remove_background
from app.services.preprocess import ImagePrepConfig, load_prepared_rgb
from app.services.color_extractor_colorthief import ColorThiefExtractor
from app.services.category_classifier_clip import ClipCategoryClassifier, ClipPrediction

class ItemPipeline:
    def __init__(
        self,
        color_extractor: ColorThiefExtractor,
        category_classifier: ClipCategoryClassifier,
        categories_en: List[str],
        prep_cfg: ImagePrepConfig = ImagePrepConfig(max_size=400, crop_to_alpha=True),
    ):
        self.color_extractor = color_extractor
        self.category_classifier = category_classifier
        self.categories_en = categories_en
        self.prep_cfg = prep_cfg

    def process_upload(self, file_bytes: bytes, ext: str) -> Dict[str, Any]:
        # 1) save original
        original_name = save_upload(file_bytes, ext)

        # 2) background removal
        nobg_name = remove_background(original_name)

        # 3) load + preprocess NO-BG for both tasks (recommended)
        rgb_img: Image.Image = load_prepared_rgb(nobg_name, self.prep_cfg)

        # 4) colors
        colors = self.color_extractor.extract(rgb_img)

        # 5) category via CLIP
        pred: ClipPrediction = self.category_classifier.predict(rgb_img, self.categories_en, top_k=3)

        return {
            "image_original_name": original_name,
            "image_no_bg_name": nobg_name,
            "color_tags": colors,
            "category": pred.label,
            "category_confidence": pred.confidence,
            "category_topk": pred.topk,
        }
