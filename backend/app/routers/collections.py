from __future__ import annotations

from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_collection_service, get_current_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.outfit_collection import CollectionCreate, CollectionResponse, CollectionUpdate
from app.services.outfit_collection_service import OutfitCollectionService

router = APIRouter(prefix="/collections", tags=["collections"])


@router.post("", response_model=CollectionResponse, status_code=status.HTTP_201_CREATED)
def create_collection(
    payload: CollectionCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: OutfitCollectionService = Depends(get_collection_service),
):
    """Create a new outfit collection for the authenticated user."""
    return svc.create(db, payload, user_id=current_user.id)


@router.get("", response_model=list[CollectionResponse])
def list_collections(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: OutfitCollectionService = Depends(get_collection_service),
):
    """Return a paginated list of outfit collections for the authenticated user."""
    return svc.list(db, user_id=current_user.id, skip=skip, limit=limit)


@router.get("/{collection_id}", response_model=CollectionResponse, responses={404: {"description": "Collection not found"}})
def get_collection(
    collection_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: OutfitCollectionService = Depends(get_collection_service),
):
    """Return a single outfit collection by ID, scoped to the authenticated user."""
    return svc.get_or_404(db, collection_id, user_id=current_user.id)


@router.patch("/{collection_id}", response_model=CollectionResponse, responses={404: {"description": "Collection not found"}})
def update_collection(
    collection_id: int,
    payload: CollectionUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: OutfitCollectionService = Depends(get_collection_service),
):
    """Update the name or outfit membership of the specified collection."""
    return svc.update(db, collection_id, payload, user_id=current_user.id)


@router.delete("/{collection_id}", status_code=status.HTTP_204_NO_CONTENT, responses={404: {"description": "Collection not found"}})
def delete_collection(
    collection_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: OutfitCollectionService = Depends(get_collection_service),
):
    """Delete the specified outfit collection."""
    svc.delete(db, collection_id, user_id=current_user.id)
