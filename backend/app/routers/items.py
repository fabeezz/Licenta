from __future__ import annotations

from fastapi import APIRouter, Depends, File, Form, UploadFile, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user, get_item_service
from app.db.session import get_db
from app.models.user import User
from app.schemas.item import ItemListQuery, ItemOut, ItemUpdate
from app.services.item_service import ItemService

router = APIRouter(prefix="/items", tags=["items"])


@router.post("", response_model=ItemOut, status_code=status.HTTP_201_CREATED)
async def create_item(
    image: UploadFile = File(...),
    brand: str | None = Form(None),
    material: str | None = Form(None),
    weather: str | None = Form(None),
    style: str | None = Form(None),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: ItemService = Depends(get_item_service),
):
    """Upload an image, run the AI classification pipeline, and create a wardrobe item."""
    content = await image.read()
    ext = (
        image.filename.rsplit(".", 1)[1].lower()
        if image.filename and "." in image.filename
        else "png"
    )
    weather_tags = [t.strip().lower() for t in weather.split(",")] if weather else None
    style_tags = [t.strip().lower() for t in style.split(",")] if style else None
    return svc.create_item_with_upload(
        db,
        content,
        ext,
        user_id=current_user.id,
        brand=brand,
        material=material,
        weather=weather_tags,
        style=style_tags,
    )


@router.get("/stats")
def get_stats(
    current_user: User = Depends(get_current_user),
    svc: ItemService = Depends(get_item_service),
):
    """Return basic wardrobe statistics (total items, category breakdown) for the authenticated user."""
    return svc.get_basic_stats(user_id=current_user.id)


@router.get("/stats/by-color")
def get_color_stats(
    current_user: User = Depends(get_current_user),
    svc: ItemService = Depends(get_item_service),
):
    """Return a color distribution breakdown of the authenticated user's wardrobe."""
    return svc.get_color_stats(user_id=current_user.id)


@router.get("/stats/by-weather")
def get_weather_stats(
    current_user: User = Depends(get_current_user),
    svc: ItemService = Depends(get_item_service),
):
    """Return a breakdown of wardrobe items grouped by weather tag for the authenticated user."""
    return svc.get_weather_stats(user_id=current_user.id)


@router.get("", response_model=list[ItemOut])
def list_items(
    current_user: User = Depends(get_current_user),
    svc: ItemService = Depends(get_item_service),
    filters: ItemListQuery = Depends(),
):
    """Return a filtered, paginated list of wardrobe items for the authenticated user."""
    return list(svc.list_items(filters, user_id=current_user.id))


@router.get("/{item_id}", response_model=ItemOut, responses={404: {"description": "Item not found"}})
def get_item(
    item_id: int,
    current_user: User = Depends(get_current_user),
    svc: ItemService = Depends(get_item_service),
):
    """Return a single wardrobe item by ID, scoped to the authenticated user."""
    return svc.get_item_for_user(item_id, user_id=current_user.id)


@router.patch("/{item_id}", response_model=ItemOut, responses={404: {"description": "Item not found"}})
def update_item(
    item_id: int,
    payload: ItemUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: ItemService = Depends(get_item_service),
):
    """Update metadata fields (brand, material, tags) for the specified wardrobe item."""
    return svc.update_item_meta(db, item_id, payload, user_id=current_user.id)


@router.delete("/{item_id}", status_code=status.HTTP_204_NO_CONTENT, responses={404: {"description": "Item not found"}})
def delete_item(
    item_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: ItemService = Depends(get_item_service),
):
    """Delete a wardrobe item and its associated media files."""
    svc.delete_item(db, item_id, user_id=current_user.id)


@router.post("/{item_id}/wear", response_model=ItemOut, responses={404: {"description": "Item not found"}})
def mark_item_worn(
    item_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: ItemService = Depends(get_item_service),
):
    """Increment the wear count and update the last-worn date for the specified item."""
    return svc.mark_item_worn(db, item_id, user_id=current_user.id)
