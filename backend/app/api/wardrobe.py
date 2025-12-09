from fastapi import APIRouter, UploadFile, File, Depends
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.models.wardrobe import WardrobeItem
from app.schemas.wardrobe import WardrobeItemOut
from app.services.image_processing import (
    save_upload,
    remove_background,
    extract_colors_colorthief,
    classify_category_stub,
)

router = APIRouter(
    prefix="/wardrobe",
    tags=["wardrobe"],
)

@router.post("/items", response_model=WardrobeItemOut)
async def create_item(
    image: UploadFile = File(...),
    db: Session = Depends(get_db),
):
    """
    Pipeline complet pentru un articol:
    - upload imagine
    - salvare original
    - background removal
    - color extraction (ColorThief + mapare RGB->nume)
    - clasificare categorie (wip)
    - salvare în DB
    """
    content = await image.read()

    if "." in image.filename:
        ext = image.filename.rsplit(".", 1)[1].lower()
    else:
        ext = "png"

    original_name = save_upload(content, ext)
    no_bg_name = remove_background(original_name)
    color_tags = extract_colors_colorthief(no_bg_name)

    # wip
    category = classify_category_stub(no_bg_name)

    item = WardrobeItem(
        image_original_name=original_name,
        image_no_bg_name=no_bg_name,
        color_tags=color_tags,
        category=category,
    )
    db.add(item)
    db.commit()
    db.refresh(item)

    return item


@router.get("/items", response_model=list[WardrobeItemOut])
def list_items(db: Session = Depends(get_db)):
    """
    Returnează toate articolele din garderobă.
    """
    items = db.query(WardrobeItem).order_by(WardrobeItem.id.desc()).all()
    return items
