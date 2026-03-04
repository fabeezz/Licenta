from __future__ import annotations

from datetime import datetime
from typing import Iterable, List

from sqlalchemy.orm import Session
from sqlalchemy import asc, desc, func

from app.models.wardrobe import WardrobeItem
from app.schemas.wardrobe import WardrobeItemUpdate
from app.services.color_extractor_colorthief import ColorThiefExtractor
from app.services.clip_attribute_classifier import ClipAttributeClassifier
from app.services.category_classifier_clip import ClipCategoryClassifier
from app.services.material_classifier import MaterialClassifier
from app.services.occasion_classifier import OccasionClassifier
from app.services.pipeline import ItemPipeline
from app.services.ai_config import CATEGORIES_EN, CLIP_MODEL_ID

class WardrobeService:
    """
    Application service layer for wardrobe operations.
    Keeps FastAPI routers thin and centralizes domain logic.
    """

    def __init__(self) -> None:
        # 1. LOAD THE HEAVY MODEL ONCE
        self._base_clip = ClipAttributeClassifier(CLIP_MODEL_ID)
        
        # 2. INJECT IT INTO THE WRAPPERS
        category_classifier = ClipCategoryClassifier(self._base_clip)
        material_classifier = MaterialClassifier(self._base_clip)
        occasion_classifier = OccasionClassifier(self._base_clip)

        # 3. BUILD THE PIPELINE
        self._pipeline = ItemPipeline(
            color_extractor=ColorThiefExtractor(palette_size=5, quality=2),
            category_classifier=category_classifier,
            material_classifier=material_classifier,
            occasion_classifier=occasion_classifier,
            categories_en=CATEGORIES_EN,
        )

    # -------- Creation --------

    def create_item_with_upload(
        self,
        db: Session,
        file_bytes: bytes,
        ext: str,
        *,
        brand: str | None = None,
        material: str | None = None,
        season: str | None = None,
        occasion: str | None = None,
    ) -> WardrobeItem:
        """
        Full pipeline: save, remove bg, preprocess, extract colors, classify, persist.
        """
        result = self._pipeline.process_upload(file_bytes, ext)

        # AI suggested values
        ai_category = result["category"]
        ai_material = result.get("material")
        ai_season = result.get("season")
        ai_occasion = result.get("occasion")

        # user overrides AI if provided
        material_value = material or ai_material
        season_value = season or ai_season
        occasion_value = occasion or ai_occasion

        item = WardrobeItem(
            image_original_name=result["image_original_name"],
            image_no_bg_name=result["image_no_bg_name"],
            color_tags=result["color_tags"],
            category=ai_category,
            brand=(brand.lower() if brand else None),
            material=(material_value.lower() if material_value else None),
            season=(season_value.lower() if season_value else None),
            occasion=(occasion_value.lower() if occasion_value else None),
            wear_count=0,
        )

        db.add(item)
        db.commit()
        db.refresh(item)
        return item

    # -------- Read / list --------

    def list_items(
        self,
        db: Session,
        *,
        category: str | None = None,
        brand: str | None = None,
        material: str | None = None,
        season: str | None = None,
        occasion: str | None = None,
        sort_by: str = "created_at",
        sort_dir: str = "desc",
        limit: int = 50,
        offset: int = 0,
    ) -> Iterable[WardrobeItem]:
        query = db.query(WardrobeItem)

        if category:
            query = query.filter(WardrobeItem.category == category)
        if brand:
            query = query.filter(WardrobeItem.brand == brand)
        if material:
            query = query.filter(WardrobeItem.material == material)
        if season:
            query = query.filter(WardrobeItem.season == season)
        if occasion:
            query = query.filter(WardrobeItem.occasion == occasion)

        sort_field_map = {
            "created_at": WardrobeItem.created_at,
            "wear_count": WardrobeItem.wear_count,
            "last_worn_at": WardrobeItem.last_worn_at,
        }
        sort_col = sort_field_map.get(sort_by, WardrobeItem.created_at)

        if sort_dir == "asc":
            query = query.order_by(asc(sort_col))
        else:
            query = query.order_by(desc(sort_col))

        return query.offset(offset).limit(limit).all()

    # -------- Update / mutations --------

    def get_item_or_404(self, db: Session, item_id: int) -> WardrobeItem:
        item = db.query(WardrobeItem).filter(WardrobeItem.id == item_id).first()
        if not item:
            from fastapi import HTTPException
            raise HTTPException(status_code=404, detail="Item not found")
        return item

    def update_item_meta(
        self,
        db: Session,
        item_id: int,
        payload: WardrobeItemUpdate,
    ) -> WardrobeItem:
        item = self.get_item_or_404(db, item_id)

        data = payload.dict(exclude_unset=True)
        for field, value in data.items():
            setattr(item, field, value)

        db.commit()
        db.refresh(item)
        return item

    def mark_item_worn(self, db: Session, item_id: int) -> WardrobeItem:
        item = self.get_item_or_404(db, item_id)

        item.wear_count += 1
        item.last_worn_at = datetime.utcnow()

        db.commit()
        db.refresh(item)
        return item
    
    def get_basic_stats(self, db: Session) -> dict:
        total = db.query(WardrobeItem).count()
        by_category = (
            db.query(WardrobeItem.category, func.count(WardrobeItem.id))
            .group_by(WardrobeItem.category)
            .all()
        )
        return {
            "total_items": total,
            "by_category": [{"category": c, "count": n} for c, n in by_category],
        }

    def recommend_simple(
        self,
        db: Session,
        *,
        occasion: str | None = None,
        season: str | None = None,
        limit: int = 3,
    ) -> list[WardrobeItem]:
        query = db.query(WardrobeItem)
        if occasion:
            query = query.filter(WardrobeItem.occasion == occasion)
        if season:
            query = query.filter(WardrobeItem.season == season)
        
        query = query.order_by(WardrobeItem.wear_count.asc(), WardrobeItem.created_at.asc())
        return query.limit(limit).all()

# Singleton instance created at module load time
wardrobe_service = WardrobeService()
