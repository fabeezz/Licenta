from __future__ import annotations

from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user, get_outfit_service
from app.db.session import get_db
from app.models.user import User
from app.schemas.outfit import OutfitCreate, OutfitResponse, OutfitUpdate
from app.services.outfit_service import OutfitService

router = APIRouter(prefix="/outfits", tags=["outfits"])


@router.post("", response_model=OutfitResponse, status_code=status.HTTP_201_CREATED)
def create_outfit(
    payload: OutfitCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: OutfitService = Depends(get_outfit_service),
):
    return svc.create(db, payload, user_id=current_user.id)


@router.get("", response_model=list[OutfitResponse])
def list_outfits(
    skip: int = 0,
    limit: int = 100,
    weather: str | None = None,
    style: str | None = None,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: OutfitService = Depends(get_outfit_service),
):
    return svc.list(
        db, user_id=current_user.id, skip=skip, limit=limit, weather=weather, style=style
    )


@router.get("/{outfit_id}", response_model=OutfitResponse)
def get_outfit(
    outfit_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: OutfitService = Depends(get_outfit_service),
):
    return svc.get_or_404(db, outfit_id, user_id=current_user.id)


@router.patch("/{outfit_id}", response_model=OutfitResponse)
def update_outfit(
    outfit_id: int,
    payload: OutfitUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: OutfitService = Depends(get_outfit_service),
):
    return svc.update(db, outfit_id, payload, user_id=current_user.id)


@router.delete("/{outfit_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_outfit(
    outfit_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: OutfitService = Depends(get_outfit_service),
):
    svc.delete(db, outfit_id, user_id=current_user.id)
