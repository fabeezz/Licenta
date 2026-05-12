from __future__ import annotations

import io

from fastapi import APIRouter, Depends, UploadFile, File, status
from PIL import Image
from sqlalchemy.orm import Session

from app.core.config import settings
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
from app.services.inspiration.search import match_inspiration
from app.services.outfit_service import OutfitService
from app.services.outfits.filters import item_matches_style, item_matches_weather, prefer_explicit_style
from app.services.outfits.harmony import HarmonyMode, suggest_outfit
from app.services.outfits.slots import OUTER_CATEGORIES, SHOES_CATEGORIES, TOP_CATEGORIES
from app.services.pipeline import ItemPipeline
from app.services.storage import save_upload

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
    all_items = item_repo.list_for_user(current_user.id, ItemListQuery(limit=200, offset=0))

    weather_tags = [t.strip().lower() for t in payload.weather.split(",")] if payload.weather else []
    style = payload.style.strip().lower() if payload.style else None

    # When no style is explicitly requested, use the user's first preferred style as a soft hint.
    effective_style = style or (
        (current_user.preferred_styles or [None])[0] if not style else None
    )

    def passes_filters(item) -> bool:
        if weather_tags and not item_matches_weather(item, weather_tags):
            return False
        if style and not item_matches_style(item, style):
            return False
        return True

    # Dress category: acts as a combined top+skips bottom — keep in TOP bucket
    _DRESS_CATS = {"dress"}
    _TOP_CATS = TOP_CATEGORIES | _DRESS_CATS
    _BOTTOM_CATS = {"jeans", "pants", "shorts", "skirt"}

    candidates: dict[str, list] = {"top": [], "bottom": [], "outer": [], "shoes": []}
    for item in all_items:
        if not passes_filters(item):
            continue
        cat = (item.category or "").strip().lower()
        if cat in _TOP_CATS:
            candidates["top"].append(item)
        elif cat in _BOTTOM_CATS:
            candidates["bottom"].append(item)
        elif cat in OUTER_CATEGORIES:
            candidates["outer"].append(item)
        elif cat in SHOES_CATEGORIES:
            candidates["shoes"].append(item)

    # Prefer items that explicitly declare the effective style so all slots feel cohesive.
    if effective_style:
        for slot in ("top", "bottom", "shoes", "outer"):
            candidates[slot] = prefer_explicit_style(candidates[slot], effective_style)

    allowed_modes: list[HarmonyMode] | None = None
    if payload.modes:
        allowed_modes = []
        for m in payload.modes:
            try:
                allowed_modes.append(HarmonyMode(m.lower()))
            except ValueError:
                pass
        if not allowed_modes:
            allowed_modes = None

    result = suggest_outfit(candidates, allowed_modes=allowed_modes)
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
    """Upload an inspiration image (e.g. Pinterest screenshot) and get the closest
    wardrobe items per slot ranked by CLIP similarity."""
    raw = await image.read()
    ext = (image.filename or "jpg").rsplit(".", 1)[-1].lower()
    screenshot_name = save_upload(raw, ext)

    rgb_img = Image.open(io.BytesIO(raw)).convert("RGB")
    all_items = item_repo.list_all_for_user(current_user.id)
    result = match_inspiration(rgb_img, pipeline.base_classifier, all_items)

    source_url = f"/media/{screenshot_name}"

    def _to_slot_match(slot_match) -> InspirationSlotMatch:
        return InspirationSlotMatch(
            best=ItemMinimal.model_validate(slot_match.best) if slot_match.best else None,
            alternates=[ItemMinimal.model_validate(it) for it in slot_match.alternates],
            score=slot_match.score,
        )

    return InspirationResponse(
        source_image_url=source_url,
        top=_to_slot_match(result.top),
        bottom=_to_slot_match(result.bottom),
        outer=_to_slot_match(result.outer),
        shoes=_to_slot_match(result.shoes),
    )
