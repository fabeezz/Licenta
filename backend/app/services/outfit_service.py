from __future__ import annotations

from typing import Sequence

from sqlalchemy.orm import Session

from app.core.exceptions import InvalidItemsError, NotFoundError
from app.models.outfit import Outfit
from app.repositories.outfit_repository import OutfitRepository
from app.schemas.outfit import OutfitCreate, OutfitUpdate


class OutfitService:
    """Business logic for outfit CRUD operations.

    Delegates all persistence to :class:`~app.repositories.outfit_repository.OutfitRepository`.

    Args:
        repo: Outfit repository for all persistence operations.
    """

    def __init__(self, repo: OutfitRepository) -> None:
        self._repo = repo

    # ── Creation ─────────────────────────────────────────────────────────────

    def create(self, db: Session, payload: OutfitCreate, *, user_id: int) -> Outfit:
        """Create a new outfit after validating that all items are owned by the user.

        Args:
            db: Active database session.
            payload: Outfit creation data.
            user_id: Owner of the new outfit.

        Returns:
            The persisted :class:`~app.models.outfit.Outfit` with all item relations loaded.

        Raises:
            InvalidItemsError: If any referenced item ID is invalid or not owned.
        """
        item_ids: set[int] = {payload.shoe_id, payload.bottom_id, payload.top_id}
        if payload.outer_id is not None:
            item_ids.add(payload.outer_id)

        owned = self._repo.verify_items_owned(user_id, item_ids)
        if owned != item_ids:
            raise InvalidItemsError()

        outfit = Outfit(
            user_id=user_id,
            name=payload.name,
            weather=payload.weather,
            occasion=payload.occasion,
            shoe_id=payload.shoe_id,
            bottom_id=payload.bottom_id,
            top_id=payload.top_id,
            outer_id=payload.outer_id,
        )
        self._repo.add(outfit)
        db.commit()
        loaded = self._repo.get_loaded(outfit.id, user_id=user_id)
        if not loaded:
            raise NotFoundError("Outfit", outfit.id)
        return loaded

    # ── Read ─────────────────────────────────────────────────────────────────

    def list(
        self,
        db: Session,
        *,
        user_id: int,
        weather: str | None = None,
        occasion: str | None = None,
        skip: int = 0,
        limit: int = 100,
    ) -> Sequence[Outfit]:
        """Return outfits owned by *user_id* with optional filtering.

        Args:
            db: Active database session (unused directly; passed for symmetry).
            user_id: Owner constraint.
            weather: Optional comma-separated weather filter.
            occasion: Optional occasion filter.
            skip: Pagination offset.
            limit: Maximum number of results.

        Returns:
            Sequence of :class:`~app.models.outfit.Outfit` instances.
        """
        return self._repo.list_for_user(
            user_id, weather=weather, occasion=occasion, skip=skip, limit=limit
        )

    def get_or_404(self, db: Session, outfit_id: int, *, user_id: int) -> Outfit:
        """Return the outfit with all item relations, raising :exc:`NotFoundError` if absent.

        Args:
            db: Active database session.
            outfit_id: Primary key of the requested outfit.
            user_id: Owner constraint.

        Returns:
            The matching :class:`~app.models.outfit.Outfit`.

        Raises:
            NotFoundError: If the outfit does not exist or belongs to another user.
        """
        outfit = self._repo.get_loaded(outfit_id, user_id=user_id)
        if not outfit:
            raise NotFoundError("Outfit", outfit_id)
        return outfit

    # ── Mutations ────────────────────────────────────────────────────────────

    def update(
        self, db: Session, outfit_id: int, payload: OutfitUpdate, *, user_id: int
    ) -> Outfit:
        """Apply a partial metadata update to an outfit.

        Args:
            db: Active database session.
            outfit_id: Primary key of the outfit to update.
            payload: Fields to update; unset fields are left unchanged.
            user_id: Owner constraint.

        Returns:
            The updated :class:`~app.models.outfit.Outfit`.

        Raises:
            NotFoundError: If the outfit does not exist or belongs to another user.
        """
        outfit = self.get_or_404(db, outfit_id, user_id=user_id)
        for field, value in payload.model_dump(exclude_unset=True).items():
            setattr(outfit, field, value)
        db.commit()
        return outfit

    def delete(self, db: Session, outfit_id: int, *, user_id: int) -> None:
        """Delete an outfit.

        Args:
            db: Active database session.
            outfit_id: Primary key of the outfit to delete.
            user_id: Owner constraint.

        Raises:
            NotFoundError: If the outfit does not exist or belongs to another user.
        """
        outfit = self._repo.get_for_user(outfit_id, user_id=user_id)
        if not outfit:
            raise NotFoundError("Outfit", outfit_id)
        self._repo.delete(outfit)
        db.commit()
