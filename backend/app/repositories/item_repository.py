from __future__ import annotations

from typing import Sequence

from sqlalchemy import asc, desc, func, select
from sqlalchemy.dialects.postgresql import array
from sqlalchemy.orm import Session

from app.models.item import Item
from app.schemas.item import ItemListQuery

_COLOR_GROUPS: dict[str, list[str]] = {
    "light": ["white", "beige", "pink", "yellow", "cyan"],
    "dark": ["black", "gray", "burgundy", "brown", "olive", "dark green", "navy"],
    "colorful": ["red", "orange", "green", "blue", "purple"],
}

_SORT_FIELDS: dict[str, object] = {
    "created_at": Item.created_at,
    "wear_count": Item.wear_count,
    "last_worn_at": Item.last_worn_at,
}


class ItemRepository:
    """Persistence layer for :class:`~app.models.item.Item`.

    All queries use SQLAlchemy 2.0 ``select()`` style.  The repository owns
    ``flush`` + ``refresh`` but **not** ``commit``; the service layer decides
    transaction boundaries.

    Args:
        db: Active SQLAlchemy session injected by FastAPI.
    """

    def __init__(self, db: Session) -> None:
        self._db = db

    # ── Mutations ────────────────────────────────────────────────────────────

    def add(self, item: Item) -> Item:
        """Persist a new *item* and refresh it in the current session.

        Args:
            item: Transient ``Item`` instance to persist.

        Returns:
            The same instance after ``flush`` + ``refresh``.
        """
        self._db.add(item)
        self._db.flush()
        self._db.refresh(item)
        return item

    def delete(self, item: Item) -> None:
        """Delete *item* from the database (no commit).

        Args:
            item: Persistent ``Item`` instance to remove.
        """
        self._db.delete(item)
        self._db.flush()

    # ── Queries ──────────────────────────────────────────────────────────────

    def get_for_user(self, item_id: int, user_id: int) -> Item | None:
        """Return the item with *item_id* owned by *user_id*, or ``None``.

        Args:
            item_id: Primary key of the requested item.
            user_id: Owner constraint — the item must belong to this user.

        Returns:
            The matching ``Item``, or ``None`` if not found.
        """
        stmt = select(Item).where(Item.id == item_id, Item.user_id == user_id)
        return self._db.scalars(stmt).first()

    def list_for_user(self, user_id: int, filters: ItemListQuery) -> Sequence[Item]:
        """Return items owned by *user_id* with optional filtering and pagination.

        Args:
            user_id: Only return items that belong to this user.
            filters: Validated query parameters from the request.

        Returns:
            Sequence of matching ``Item`` instances.
        """
        stmt = select(Item).where(Item.user_id == user_id)

        if filters.category:
            stmt = stmt.where(Item.category == filters.category)
        if filters.brand:
            stmt = stmt.where(Item.brand == filters.brand)
        if filters.dominant_color:
            dc = filters.dominant_color.strip().lower()
            stmt = stmt.where(
                Item.color_tags.isnot(None),
                Item.dominant_color == dc,
            )
        if filters.colors:
            allowed: set[str] = set()
            for group in filters.colors:
                allowed.update(_COLOR_GROUPS.get(group.strip().lower(), []))
            if allowed:
                stmt = stmt.where(
                    Item.color_tags.isnot(None),
                    Item.dominant_color.in_(list(allowed)),
                )
        if filters.material:
            stmt = stmt.where(Item.material == filters.material)
        if filters.weather:
            tags = [t.strip().lower() for t in filters.weather.split(",")]
            stmt = stmt.where(Item.weather.op("?|")(array(tags)))
        if filters.occasion:
            stmt = stmt.where(Item.occasion == filters.occasion)

        sort_col = _SORT_FIELDS.get(filters.sort_by, Item.created_at)
        order_fn = asc if filters.sort_dir == "asc" else desc
        stmt = stmt.order_by(order_fn(sort_col)).offset(filters.offset).limit(filters.limit)

        return self._db.scalars(stmt).all()

    def stats_for_user(self, user_id: int) -> dict:
        """Return basic inventory statistics for *user_id*.

        Args:
            user_id: User whose stats are requested.

        Returns:
            Dict with ``total_items`` and ``by_category`` breakdown.
        """
        total_stmt = select(func.count(Item.id)).where(Item.user_id == user_id)
        total: int = self._db.scalar(total_stmt) or 0

        breakdown_stmt = (
            select(Item.category, func.count(Item.id))
            .where(Item.user_id == user_id)
            .group_by(Item.category)
        )
        by_category = [
            {"category": cat, "count": cnt}
            for cat, cnt in self._db.execute(breakdown_stmt).all()
        ]
        return {"total_items": total, "by_category": by_category}
