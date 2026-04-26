from __future__ import annotations

from typing import Sequence

from sqlalchemy import select
from sqlalchemy.dialects.postgresql import array
from sqlalchemy.orm import Session, joinedload

from app.models.item import Item
from app.models.outfit import Outfit

def _item_loads() -> tuple:
    return (
        joinedload(Outfit.shoe),
        joinedload(Outfit.bottom),
        joinedload(Outfit.top),
        joinedload(Outfit.outer),
    )


class OutfitRepository:
    """Persistence layer for :class:`~app.models.outfit.Outfit`.

    All queries use SQLAlchemy 2.0 ``select()`` style.  The repository owns
    ``flush`` + ``refresh`` but **not** ``commit``; the service layer decides
    transaction boundaries.

    Args:
        db: Active SQLAlchemy session injected by FastAPI.
    """

    def __init__(self, db: Session) -> None:
        self._db = db

    # ── Mutations ────────────────────────────────────────────────────────────

    def add(self, outfit: Outfit) -> Outfit:
        """Persist a new *outfit* and flush it.

        Args:
            outfit: Transient ``Outfit`` instance to persist.

        Returns:
            The same instance after ``flush``.
        """
        self._db.add(outfit)
        self._db.flush()
        return outfit

    def delete(self, outfit: Outfit) -> None:
        """Delete *outfit* from the database (no commit).

        Args:
            outfit: Persistent ``Outfit`` instance to remove.
        """
        self._db.delete(outfit)
        self._db.flush()

    # ── Queries ──────────────────────────────────────────────────────────────

    def get_for_user(self, outfit_id: int, user_id: int) -> Outfit | None:
        """Return the outfit with *outfit_id* owned by *user_id*, without eager loads.

        Args:
            outfit_id: Primary key of the requested outfit.
            user_id: Owner constraint.

        Returns:
            The matching ``Outfit``, or ``None`` if not found.
        """
        stmt = select(Outfit).where(Outfit.id == outfit_id, Outfit.user_id == user_id)
        return self._db.scalars(stmt).first()

    def get_loaded(self, outfit_id: int, user_id: int) -> Outfit | None:
        """Return the outfit with all item relationships eagerly loaded.

        Args:
            outfit_id: Primary key of the requested outfit.
            user_id: Owner constraint.

        Returns:
            The matching ``Outfit`` with ``shoe``, ``bottom``, ``top``, ``outer``
            populated, or ``None`` if not found.
        """
        stmt = (
            select(Outfit)
            .where(Outfit.id == outfit_id, Outfit.user_id == user_id)
            .options(*_item_loads())
        )
        return self._db.scalars(stmt).first()

    def list_for_user(
        self,
        user_id: int,
        *,
        weather: str | None = None,
        occasion: str | None = None,
        skip: int = 0,
        limit: int = 100,
    ) -> Sequence[Outfit]:
        """Return outfits owned by *user_id* with optional filtering.

        Args:
            user_id: Only return outfits that belong to this user.
            weather: Optional comma-separated weather filter.
            occasion: Optional occasion filter.
            skip: Pagination offset.
            limit: Maximum number of results.

        Returns:
            Sequence of ``Outfit`` instances with item relationships loaded.
        """
        stmt = (
            select(Outfit)
            .where(Outfit.user_id == user_id)
            .options(*_item_loads())
            .order_by(Outfit.id.desc())
            .offset(skip)
            .limit(limit)
        )
        if weather:
            tags = [t.strip().lower() for t in weather.split(",")]
            stmt = stmt.where(Outfit.weather.op("?|")(array(tags)))
        if occasion:
            stmt = stmt.where(Outfit.occasion == occasion)
        return self._db.scalars(stmt).all()

    def verify_items_owned(self, user_id: int, item_ids: set[int]) -> set[int]:
        """Return the subset of *item_ids* that are owned by *user_id*.

        Args:
            user_id: User performing the operation.
            item_ids: Set of item primary keys to validate.

        Returns:
            Set of IDs that exist and belong to *user_id*.
        """
        stmt = select(Item.id).where(
            Item.id.in_(item_ids), Item.user_id == user_id
        )
        return {row for row in self._db.scalars(stmt).all()}
