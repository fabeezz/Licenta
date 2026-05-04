from __future__ import annotations

from typing import Sequence

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.models.trip import Trip


class TripRepository:
    def __init__(self, db: Session) -> None:
        self._db = db

    def add(self, trip: Trip) -> Trip:
        self._db.add(trip)
        self._db.flush()
        self._db.refresh(trip)
        return trip

    def get_for_user(self, trip_id: int, user_id: int) -> Trip | None:
        stmt = select(Trip).where(Trip.id == trip_id, Trip.user_id == user_id)
        return self._db.scalars(stmt).first()

    def list_for_user(self, user_id: int, *, skip: int = 0, limit: int = 50) -> Sequence[Trip]:
        stmt = (
            select(Trip)
            .where(Trip.user_id == user_id)
            .order_by(Trip.id.desc())
            .offset(skip)
            .limit(limit)
        )
        return self._db.scalars(stmt).all()
