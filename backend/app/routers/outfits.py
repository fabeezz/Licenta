from __future__ import annotations

from fastapi import APIRouter, Depends, UploadFile, File, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user, get_item_repository, get_outfit_service, get_pipeline
from app.db.session import get_db
from app.models.user import User
from app.repositories.item_repository import ItemRepository
from app.schemas.item import ItemListQuery, ItemMinimal
from app.schemas.outfit import (
    InspirationResponse,
    InspirationSlotMatch,
    OutfitCreate,
    OutfitResponse,
    OutfitSuggestRequest,
    OutfitSuggestResponse,
    OutfitUpdate,
)
from app.services.outfit_service import OutfitService
from app.services.outfits.suggester import build_suggestion, run_inspiration
from app.services.pipeline import ItemPipeline
from app.services.trip.config import WARDROBE_QUERY_LIMIT

router = APIRouter(prefix="/outfits", tags=["outfits"])


@router.post("", response_model=OutfitResponse, status_code=status.HTTP_201_CREATED)
def create_outfit(
    payload: OutfitCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: OutfitService = Depends(get_outfit_service),
):
    """Create a new outfit from a set of item IDs for the authenticated user."""
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
    """Return a paginated, optionally filtered list of outfits for the authenticated user."""
    return svc.list(
        db, user_id=current_user.id, skip=skip, limit=limit, weather=weather, style=style
    )


@router.get("/{outfit_id}", response_model=OutfitResponse, responses={404: {"description": "Outfit not found"}})
def get_outfit(
    outfit_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: OutfitService = Depends(get_outfit_service),
):
    """Return a single outfit by ID, scoped to the authenticated user."""
    return svc.get_or_404(db, outfit_id, user_id=current_user.id)


@router.patch("/{outfit_id}", response_model=OutfitResponse, responses={404: {"description": "Outfit not found"}})
def update_outfit(
    outfit_id: int,
    payload: OutfitUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: OutfitService = Depends(get_outfit_service),
):
    """Update the name or item composition of the specified outfit."""
    return svc.update(db, outfit_id, payload, user_id=current_user.id)


@router.delete("/{outfit_id}", status_code=status.HTTP_204_NO_CONTENT, responses={404: {"description": "Outfit not found"}})
def delete_outfit(
    outfit_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: OutfitService = Depends(get_outfit_service),
):
    """Delete the specified outfit."""
    svc.delete(db, outfit_id, user_id=current_user.id)


@router.post("/suggest", response_model=OutfitSuggestResponse)
def suggest_outfit_endpoint(
    payload: OutfitSuggestRequest,
    current_user: User = Depends(get_current_user),
    item_repo: ItemRepository = Depends(get_item_repository),
):
    """Return harmony-suggested item IDs per slot for the current user's wardrobe."""
    items = item_repo.list_for_user(current_user.id, ItemListQuery(limit=WARDROBE_QUERY_LIMIT, offset=0))
    result = build_suggestion(items, payload, current_user.preferred_styles)
    if result is None:
        return OutfitSuggestResponse()
    return OutfitSuggestResponse(
        top=result["top"].id if "top" in result else None,
        bottom=result["bottom"].id if "bottom" in result else None,
        outer=result["outer"].id if "outer" in result else None,
        shoes=result["shoes"].id if "shoes" in result else None,
    )


@router.post("/from-image", response_model=InspirationResponse)
async def inspiration_from_image(
    image: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
    item_repo: ItemRepository = Depends(get_item_repository),
    pipeline: ItemPipeline = Depends(get_pipeline),
):
    """Upload an inspiration image and get the closest wardrobe items per slot by CLIP similarity."""
    raw = await image.read()
    ext = (image.filename or "jpg").rsplit(".", 1)[-1].lower()
    items = item_repo.list_all_for_user(current_user.id)
    screenshot_name, result = run_inspiration(raw, ext, items, pipeline.base_classifier)

    def _to_slot_match(slot_match) -> InspirationSlotMatch:
        return InspirationSlotMatch(
            best=ItemMinimal.model_validate(slot_match.best) if slot_match.best else None,
            alternates=[ItemMinimal.model_validate(it) for it in slot_match.alternates],
            score=slot_match.score,
        )

    return InspirationResponse(
        source_image_url=f"/media/{screenshot_name}",
        top=_to_slot_match(result.top),
        bottom=_to_slot_match(result.bottom),
        outer=_to_slot_match(result.outer),
        shoes=_to_slot_match(result.shoes),
    )
