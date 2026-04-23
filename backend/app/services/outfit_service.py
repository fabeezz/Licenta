from __future__ import annotations

from sqlalchemy.orm import Session, joinedload

from app.models.outfit import Outfit
from app.models.item import Item
from app.schemas.outfit import OutfitCreate, OutfitUpdate


class OutfitService:
    def create(self, db: Session, payload: OutfitCreate, *, user_id: int) -> Outfit:
        from fastapi import HTTPException

        item_ids = {payload.shoe_id, payload.bottom_id, payload.top_id}
        if payload.outer_id is not None:
            item_ids.add(payload.outer_id)

        owned = {
            row.id
            for row in db.query(Item.id)
            .filter(Item.id.in_(item_ids), Item.user_id == user_id)
            .all()
        }
        if owned != item_ids:
            raise HTTPException(status_code=400, detail="One or more items are invalid or not owned by user")

        outfit = Outfit(
            user_id=user_id,
            name=payload.name,
            season=payload.season,
            occasion=payload.occasion,
            shoe_id=payload.shoe_id,
            bottom_id=payload.bottom_id,
            top_id=payload.top_id,
            outer_id=payload.outer_id,
        )
        db.add(outfit)
        db.commit()
        db.refresh(outfit)
        return self._load(db, outfit.id, user_id=user_id)

    def list(
        self,
        db: Session,
        *,
        user_id: int,
        season: str | None = None,
        occasion: str | None = None,
        skip: int = 0,
        limit: int = 100,
    ) -> list[Outfit]:
        query = db.query(Outfit).filter(Outfit.user_id == user_id)

        if season:
            query = query.filter(Outfit.season == season)
        if occasion:
            query = query.filter(Outfit.occasion == occasion)

        return (
            query.options(
                joinedload(Outfit.shoe),
                joinedload(Outfit.bottom),
                joinedload(Outfit.top),
                joinedload(Outfit.outer),
            )
            .order_by(Outfit.id.desc())
            .offset(skip)
            .limit(limit)
            .all()
        )

    def get_or_404(self, db: Session, outfit_id: int, *, user_id: int) -> Outfit:
        return self._load(db, outfit_id, user_id=user_id)

    def update(self, db: Session, outfit_id: int, payload: OutfitUpdate, *, user_id: int) -> Outfit:
        outfit = self.get_or_404(db, outfit_id, user_id=user_id)
        for field, value in payload.model_dump(exclude_unset=True).items():
            setattr(outfit, field, value)
        db.commit()
        db.refresh(outfit)
        return self._load(db, outfit.id, user_id=user_id)

    def delete(self, db: Session, outfit_id: int, *, user_id: int) -> None:
        outfit = self.get_or_404(db, outfit_id, user_id=user_id)
        db.delete(outfit)
        db.commit()

    def _load(self, db: Session, outfit_id: int, *, user_id: int) -> Outfit:
        from fastapi import HTTPException

        outfit = (
            db.query(Outfit)
            .filter(Outfit.id == outfit_id, Outfit.user_id == user_id)
            .options(
                joinedload(Outfit.shoe),
                joinedload(Outfit.bottom),
                joinedload(Outfit.top),
                joinedload(Outfit.outer),
            )
            .first()
        )
        if not outfit:
            raise HTTPException(status_code=404, detail="Outfit not found")
        return outfit


outfit_service = OutfitService()
