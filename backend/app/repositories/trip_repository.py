from __future__ import annotations

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
