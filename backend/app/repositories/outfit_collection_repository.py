from __future__ import annotations

from typing import Sequence

from sqlalchemy import select
from sqlalchemy.orm import Session, selectinload

from app.models.outfit import Outfit
from app.models.outfit_collection import OutfitCollection


class OutfitCollectionRepository:
    def __init__(self, db: Session) -> None:
        self._db = db

    def add(self, collection: OutfitCollection) -> OutfitCollection:
        self._db.add(collection)
        self._db.flush()
        return collection

    def delete(self, collection: OutfitCollection) -> None:
        self._db.delete(collection)
        self._db.flush()

    def get_for_user(self, collection_id: int, user_id: int) -> OutfitCollection | None:
        stmt = select(OutfitCollection).where(
            OutfitCollection.id == collection_id,
            OutfitCollection.user_id == user_id,
        )
        return self._db.scalars(stmt).first()

    def get_loaded(self, collection_id: int, user_id: int) -> OutfitCollection | None:
        stmt = (
            select(OutfitCollection)
            .where(
                OutfitCollection.id == collection_id,
                OutfitCollection.user_id == user_id,
            )
            .options(
                selectinload(OutfitCollection.outfits).selectinload(Outfit.shoe),
                selectinload(OutfitCollection.outfits).selectinload(Outfit.bottom),
                selectinload(OutfitCollection.outfits).selectinload(Outfit.top),
                selectinload(OutfitCollection.outfits).selectinload(Outfit.outer),
            )
        )
        return self._db.scalars(stmt).first()

    def list_for_user(
        self,
        user_id: int,
        *,
        skip: int = 0,
        limit: int = 100,
    ) -> Sequence[OutfitCollection]:
        stmt = (
            select(OutfitCollection)
            .where(OutfitCollection.user_id == user_id)
            .options(
                selectinload(OutfitCollection.outfits).selectinload(Outfit.shoe),
                selectinload(OutfitCollection.outfits).selectinload(Outfit.bottom),
                selectinload(OutfitCollection.outfits).selectinload(Outfit.top),
                selectinload(OutfitCollection.outfits).selectinload(Outfit.outer),
            )
            .order_by(OutfitCollection.id.desc())
            .offset(skip)
            .limit(limit)
        )
        return self._db.scalars(stmt).all()

    def verify_outfits_owned(self, user_id: int, outfit_ids: set[int]) -> set[int]:
        stmt = select(Outfit.id).where(
            Outfit.id.in_(outfit_ids),
            Outfit.user_id == user_id,
        )
        return {row for row in self._db.scalars(stmt).all()}
