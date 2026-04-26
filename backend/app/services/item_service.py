from __future__ import annotations

from datetime import datetime
from typing import Sequence

from sqlalchemy.orm import Session

from app.core.exceptions import NotFoundError
from app.models.item import Item
from app.repositories.item_repository import ItemRepository
from app.schemas.item import ItemListQuery, ItemUpdate
from app.services.pipeline import ItemPipeline


class ItemService:
    """Business logic for wardrobe item operations.

    Coordinates the AI pipeline and the item repository.  All persistence
    is delegated to :class:`~app.repositories.item_repository.ItemRepository`;
    this class never issues ``db.query`` directly.

    Args:
        repo: Item repository for all persistence operations.
        pipeline: Fully-constructed AI processing pipeline.
    """

    def __init__(self, repo: ItemRepository, pipeline: ItemPipeline) -> None:
        self._repo = repo
        self._pipeline = pipeline

    # ── Creation ─────────────────────────────────────────────────────────────

    def create_item_with_upload(
        self,
        db: Session,
        file_bytes: bytes,
        ext: str,
        *,
        user_id: int,
        brand: str | None = None,
        material: str | None = None,
        weather: list[str] | None = None,
        occasion: str | None = None,
    ) -> Item:
        """Run the AI pipeline on *file_bytes* and persist the resulting item.

        User-supplied attributes override AI predictions when provided.

        Args:
            db: Active database session.
            file_bytes: Raw bytes of the uploaded image.
            ext: File extension without the leading dot.
            user_id: Owner of the new item.
            brand: Optional user-supplied brand (stored in Title Case).
            material: Optional override for AI-predicted material.
            weather: Optional override for AI-inferred weather tags.
            occasion: Optional override for AI-predicted occasion.

        Returns:
            The persisted :class:`~app.models.item.Item` instance.
        """
        result = self._pipeline.process_upload(file_bytes, ext)
        material_value = material or result.material
        weather_value = weather if weather is not None else result.weather
        occasion_value = occasion or result.occasion

        item = Item(
            user_id=user_id,
            image_original_name=result.image_original_name,
            image_no_bg_name=result.image_no_bg_name,
            color_tags=result.color_tags,
            category=result.category,
            brand=(brand.title() if brand else None),
            material=(material_value.lower() if material_value else None),
            weather=[t.lower() for t in weather_value],
            occasion=(occasion_value.lower() if occasion_value else None),
            wear_count=0,
        )
        self._repo.add(item)
        db.commit()
        db.refresh(item)
        return item

    # ── Read ─────────────────────────────────────────────────────────────────

    def get_item_for_user(self, item_id: int, *, user_id: int) -> Item:
        """Return the item owned by *user_id*, raising :exc:`NotFoundError` if absent.

        Args:
            item_id: Primary key of the requested item.
            user_id: Owner constraint.

        Returns:
            The matching :class:`~app.models.item.Item`.

        Raises:
            NotFoundError: If the item does not exist or belongs to another user.
        """
        item = self._repo.get_for_user(item_id, user_id)
        if not item:
            raise NotFoundError("Item", item_id)
        return item

    def list_items(self, filters: ItemListQuery, *, user_id: int) -> Sequence[Item]:
        """Return filtered and paginated items for *user_id*.

        Args:
            filters: Validated query parameters.
            user_id: Owner constraint.

        Returns:
            Sequence of matching :class:`~app.models.item.Item` instances.
        """
        return self._repo.list_for_user(user_id, filters)

    def get_basic_stats(self, *, user_id: int) -> dict:
        """Return inventory statistics for *user_id*.

        Args:
            user_id: User whose stats are requested.

        Returns:
            Dict with ``total_items`` and ``by_category`` breakdown.
        """
        return self._repo.stats_for_user(user_id)

    # ── Mutations ────────────────────────────────────────────────────────────

    def delete_item(self, db: Session, item_id: int, *, user_id: int) -> None:
        """Delete the item if it exists and is owned by *user_id*.

        Args:
            db: Active database session.
            item_id: Primary key of the item to delete.
            user_id: Owner constraint.

        Raises:
            NotFoundError: If the item does not exist or belongs to another user.
        """
        item = self.get_item_for_user(item_id, user_id=user_id)
        self._repo.delete(item)
        db.commit()

    def update_item_meta(
        self,
        db: Session,
        item_id: int,
        payload: ItemUpdate,
        *,
        user_id: int,
    ) -> Item:
        """Apply a partial metadata update to an item.

        Args:
            db: Active database session.
            item_id: Primary key of the item to update.
            payload: Fields to update; unset fields are left unchanged.
            user_id: Owner constraint.

        Returns:
            The updated :class:`~app.models.item.Item`.

        Raises:
            NotFoundError: If the item does not exist or belongs to another user.
        """
        item = self.get_item_for_user(item_id, user_id=user_id)
        for field, value in payload.model_dump(exclude_unset=True).items():
            setattr(item, field, value)
        db.commit()
        db.refresh(item)
        return item

    def mark_item_worn(self, db: Session, item_id: int, *, user_id: int) -> Item:
        """Increment the wear counter and record the current timestamp.

        Args:
            db: Active database session.
            item_id: Primary key of the item worn.
            user_id: Owner constraint.

        Returns:
            The updated :class:`~app.models.item.Item`.

        Raises:
            NotFoundError: If the item does not exist or belongs to another user.
        """
        item = self.get_item_for_user(item_id, user_id=user_id)
        item.wear_count += 1
        item.last_worn_at = datetime.utcnow()
        db.commit()
        db.refresh(item)
        return item
