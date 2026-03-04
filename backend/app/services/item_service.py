from __future__ import annotations

from datetime import datetime
from typing import Iterable

from sqlalchemy.orm import Session
from sqlalchemy import asc, desc, func, text

from app.models.item import Item
from app.schemas.item import ItemUpdate
from app.services.image.color_extractor_colorthief import ColorThiefExtractor
from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier
from app.services.ai.category_classifier import ClipCategoryClassifier
from app.services.ai.material_classifier import ClipMaterialClassifier
from app.services.ai.occasion_classifier import ClipOccasionClassifier
from app.services.pipeline import ItemPipeline
from app.services.ai.config import CATEGORIES_EN, CLIP_MODEL_ID


class ItemService:
    """
    Application service layer for wardrobe operations.
    Keeps FastAPI routers thin and centralizes domain logic.
    """

    def __init__(self) -> None:
        self._base_clip = ClipAttributeClassifier(CLIP_MODEL_ID)
        category_classifier = ClipCategoryClassifier(self._base_clip)
        material_classifier = ClipMaterialClassifier(self._base_clip)
        occasion_classifier = ClipOccasionClassifier(self._base_clip)
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
        user_id: int,
        brand: str | None = None,
        material: str | None = None,
        season: str | None = None,
        occasion: str | None = None,
    ) -> Item:
        result = self._pipeline.process_upload(file_bytes, ext)
        ai_category = result["category"]
        ai_material = result.get("material")
        ai_season = result.get("season")
        ai_occasion = result.get("occasion")
        material_value = material or ai_material
        season_value = season or ai_season
        occasion_value = occasion or ai_occasion

        item = Item(
            user_id=user_id,
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

    def delete_item(self, db: Session, item_id: int, *, user_id: int) -> None:
        item = self.get_item_or_404(db, item_id, user_id=user_id)
        db.delete(item)
        db.commit()

    # -------- Read / list --------

    def list_items(
        self,
        db: Session,
        *,
        user_id: int,
        category: str | None = None,
        brand: str | None = None,
        dominant_color: str | None = None,
        material: str | None = None,
        season: str | None = None,
        occasion: str | None = None,
        sort_by: str = "created_at",
        sort_dir: str = "desc",
        limit: int = 50,
        offset: int = 0,
    ) -> Iterable[Item]:
        query = db.query(Item).filter(Item.user_id == user_id)
        if category:
            query = query.filter(Item.category == category)
        if brand:
            query = query.filter(Item.brand == brand)
        if dominant_color:
            dc = dominant_color.strip().lower()
            query = query.filter(
                Item.color_tags.isnot(None),
                text("(items.color_tags->'dominant'->>0) = :dc").bindparams(dc=dc),
            )
        if material:
            query = query.filter(Item.material == material)
        if season:
            query = query.filter(Item.season == season)
        if occasion:
            query = query.filter(Item.occasion == occasion)
        sort_field_map = {
            "created_at": Item.created_at,
            "wear_count": Item.wear_count,
            "last_worn_at": Item.last_worn_at,
        }
        sort_col = sort_field_map.get(sort_by, Item.created_at)
        if sort_dir == "asc":
            query = query.order_by(asc(sort_col))
        else:
            query = query.order_by(desc(sort_col))
        return query.offset(offset).limit(limit).all()

    # -------- Update / mutations --------

    def get_item_or_404(self, db: Session, item_id: int, *, user_id: int) -> Item:
        from fastapi import HTTPException
        item = db.query(Item).filter(Item.id == item_id, Item.user_id == user_id).first()
        if not item:
            raise HTTPException(status_code=404, detail="Item not found")
        return item

    def update_item_meta(
        self,
        db: Session,
        item_id: int,
        payload: ItemUpdate,
        *,
        user_id: int,
    ) -> Item:
        item = self.get_item_or_404(db, item_id, user_id=user_id)
        data = payload.model_dump(exclude_unset=True) if hasattr(payload, "model_dump") else payload.dict(exclude_unset=True)
        for field, value in data.items():
            setattr(item, field, value)
        db.commit()
        db.refresh(item)
        return item

    def mark_item_worn(self, db: Session, item_id: int, *, user_id: int) -> Item:
        item = self.get_item_or_404(db, item_id, user_id=user_id)
        item.wear_count += 1
        item.last_worn_at = datetime.utcnow()
        db.commit()
        db.refresh(item)
        return item

    def get_basic_stats(self, db: Session, *, user_id: int) -> dict:
        query = db.query(Item).filter(Item.user_id == user_id)
        total = query.count()
        by_category = (
            query.with_entities(Item.category, func.count(Item.id))
            .group_by(Item.category)
            .all()
        )
        return {
            "total_items": total,
            "by_category": [{"category": c, "count": n} for c, n in by_category],
        }

    # def recommend_simple(
    #     self,
    #     db: Session,
    #     *,
    #     user_id: int,
    #     occasion: str | None = None,
    #     season: str | None = None,
    #     limit: int = 3,
    # ) -> list[Item]:
    #     query = db.query(Item).filter(Item.user_id == user_id)
    #     if occasion:
    #         query = query.filter(Item.occasion == occasion)
    #     if season:
    #         query = query.filter(Item.season == season)
    #     query = query.order_by(Item.wear_count.asc(), Item.created_at.asc())
    #     return query.limit(limit).all()


item_service = ItemService()
