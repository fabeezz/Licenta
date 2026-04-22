from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.outfit import OutfitCreate, OutfitUpdate, OutfitResponse
from app.services.outfit_service import outfit_service

router = APIRouter(prefix="/outfits", tags=["outfits"])


@router.post("/", response_model=OutfitResponse, status_code=status.HTTP_201_CREATED)
def create_outfit(
    payload: OutfitCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return outfit_service.create(db, payload, user_id=current_user.id)


@router.get("/", response_model=list[OutfitResponse])
def list_outfits(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return outfit_service.list(db, user_id=current_user.id, skip=skip, limit=limit)


@router.get("/{outfit_id}", response_model=OutfitResponse)
def get_outfit(
    outfit_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return outfit_service.get_or_404(db, outfit_id, user_id=current_user.id)


@router.patch("/{outfit_id}", response_model=OutfitResponse)
def update_outfit(
    outfit_id: int,
    payload: OutfitUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return outfit_service.update(db, outfit_id, payload, user_id=current_user.id)


@router.delete("/{outfit_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_outfit(
    outfit_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    outfit_service.delete(db, outfit_id, user_id=current_user.id)
