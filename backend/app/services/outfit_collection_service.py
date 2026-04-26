from __future__ import annotations

from typing import Sequence

from sqlalchemy.orm import Session

from app.core.exceptions import InvalidOutfitsError, NotFoundError
from app.models.outfit import Outfit
from app.models.outfit_collection import OutfitCollection
from app.repositories.outfit_collection_repository import OutfitCollectionRepository
from app.schemas.outfit_collection import CollectionCreate, CollectionUpdate


class OutfitCollectionService:
    def __init__(self, repo: OutfitCollectionRepository) -> None:
        self._repo = repo

    def create(self, db: Session, payload: CollectionCreate, *, user_id: int) -> OutfitCollection:
        outfit_ids = set(payload.outfit_ids)
        owned = self._repo.verify_outfits_owned(user_id, outfit_ids)
        if owned != outfit_ids:
            raise InvalidOutfitsError()

        outfits = db.query(Outfit).filter(Outfit.id.in_(outfit_ids)).all()
        collection = OutfitCollection(name=payload.name, user_id=user_id)
        collection.outfits = outfits
        self._repo.add(collection)
        db.commit()
        loaded = self._repo.get_loaded(collection.id, user_id=user_id)
        if not loaded:
            raise NotFoundError("OutfitCollection", collection.id)
        return loaded

    def list(self, db: Session, *, user_id: int, skip: int = 0, limit: int = 100) -> Sequence[OutfitCollection]:
        return self._repo.list_for_user(user_id, skip=skip, limit=limit)

    def get_or_404(self, db: Session, collection_id: int, *, user_id: int) -> OutfitCollection:
        collection = self._repo.get_loaded(collection_id, user_id=user_id)
        if not collection:
            raise NotFoundError("OutfitCollection", collection_id)
        return collection

    def update(self, db: Session, collection_id: int, payload: CollectionUpdate, *, user_id: int) -> OutfitCollection:
        collection = self.get_or_404(db, collection_id, user_id=user_id)
        for field, value in payload.model_dump(exclude_unset=True).items():
            setattr(collection, field, value)
        db.commit()
        return collection

    def delete(self, db: Session, collection_id: int, *, user_id: int) -> None:
        collection = self._repo.get_for_user(collection_id, user_id=user_id)
        if not collection:
            raise NotFoundError("OutfitCollection", collection_id)
        self._repo.delete(collection)
        db.commit()
