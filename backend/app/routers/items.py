from fastapi import APIRouter, UploadFile, File, Depends, Query, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.item import ItemOut, ItemUpdate
from app.services.item_service import item_service

router = APIRouter(
    prefix="/item",
    tags=["items"],
)


@router.post("/create", response_model=ItemOut)
async def create_item(
    image: UploadFile = File(...),
    brand: str | None = None,
    material: str | None = None,
    season: str | None = None,
    occasion: str | None = None,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    content = await image.read()
    ext = (
        image.filename.rsplit(".", 1)[1].lower()
        if image.filename and "." in image.filename
        else "png"
    )
    return item_service.create_item_with_upload(
        db,
        content,
        ext,
        user_id=current_user.id,
        brand=brand,
        material=material,
        season=season,
        occasion=occasion,
    )


@router.get("/read/{item_id}", response_model=ItemOut)
def get_item(
    item_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return item_service.get_item_or_404(db, item_id, user_id=current_user.id)


@router.patch("/update/{item_id}", response_model=ItemOut)
def update_item_meta(
    item_id: int,
    payload: ItemUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return item_service.update_item_meta(db, item_id, payload, user_id=current_user.id)


@router.delete("/delete/{item_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_item(
    item_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    item_service.delete_item(db, item_id, user_id=current_user.id)
    return


@router.post("/wear/{item_id}", response_model=ItemOut)
def mark_item_worn(
    item_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return item_service.mark_item_worn(db, item_id, user_id=current_user.id)
