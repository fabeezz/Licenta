from datetime import datetime
from fastapi import APIRouter, UploadFile, File, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import asc, desc

from app.db.session import get_db
from app.models.wardrobe import WardrobeItem
from app.schemas.wardrobe import WardrobeItemOut, WardrobeItemUpdate
from app.services.image_processing import (
    save_upload,
    remove_background,
    extract_colors,
    classify_category_stub,
)
from app.services.color_extractor_colorthief import ColorThiefExtractor
from app.services.category_classifier_clip import ClipCategoryClassifier
from app.services.pipeline import ItemPipeline

router = APIRouter(
    prefix="/wardrobe",
    tags=["wardrobe"],
)

CATEGORIES_EN = [
    "t-shirt", "shirt", "hoodie", "sweater",
    "jacket", "coat", "jeans", "pants", "shorts",
    "skirt", "dress", "blazer",
    "sneakers", "shoes", "boots",
    "bag", "hat"
]

_pipeline = ItemPipeline(
    color_extractor=ColorThiefExtractor(palette_size=5, quality=2),
    category_classifier=ClipCategoryClassifier("openai/clip-vit-base-patch32"),
    categories_en=CATEGORIES_EN
)

@router.post("/items")
async def create_item(
    image: UploadFile = File(...),
    brand: str | None = None,
    material: str | None = None,
    season: str | None = None,
    occasion: str | None = None, 
    db: Session = Depends(get_db)
):
    content = await image.read()
    ext = (image.filename.rsplit(".", 1)[1].lower() if image.filename and "." in image.filename else "png")

    result = _pipeline.process_upload(content, ext)

    item = WardrobeItem(
        image_original_name=result["image_original_name"],
        image_no_bg_name=result["image_no_bg_name"],
        color_tags=result["color_tags"],
        category=result["category"],
        brand=brand,
        material=material,
        season=season,
        occasion=occasion,
        wear_count=0
        # optionally store confidence/topk in DB later
    )
#     item = WardrobeItem(
#         image_original_name=original_name,
#         image_no_bg_name=no_bg_name,
#         color_tags=color_tags,
#         category=category,
#         brand=brand,
#         material=material,
#         season=season,
#         occasion=occasion,
#         wear_count=0,
#     )

    db.add(item)
    db.commit()
    db.refresh(item)
    return item

# @router.post("/items", response_model=WardrobeItemOut)
# async def create_item(
#     image: UploadFile = File(...),
#     brand: str | None = None,
#     material: str | None = None,
#     season: str | None = None,
#     occasion: str | None = None,
#     db: Session = Depends(get_db),
# ):
#     """
#     Pipeline complet pentru un articol:
#     - upload imagine
#     - salvare original
#     - background removal
#     - color extraction (ColorThief + mapare RGB->nume)
#     - clasificare categorie (wip)
#     - salvare în DB
#     """
#     content = await image.read()

#     if "." in image.filename:
#         ext = image.filename.rsplit(".", 1)[1].lower()
#     else:
#         ext = "png"

#     original_name = save_upload(content, ext)
#     no_bg_name = remove_background(original_name)

#     color_tags = extract_colors(no_bg_name)
#     # wip
#     category = classify_category_stub(no_bg_name)

#     item = WardrobeItem(
#         image_original_name=original_name,
#         image_no_bg_name=no_bg_name,
#         color_tags=color_tags,
#         category=category,
#         brand=brand,
#         material=material,
#         season=season,
#         occasion=occasion,
#         wear_count=0,
#     )
#     db.add(item)
#     db.commit()
#     db.refresh(item)

#     return item

@router.patch("/items/{item_id}", response_model=WardrobeItemOut)
def update_item_meta(
    item_id: int,
    payload: WardrobeItemUpdate,
    db: Session = Depends(get_db),
):
    item = db.query(WardrobeItem).filter(WardrobeItem.id == item_id).first()
    if not item:
        raise HTTPException(status_code=404, detail="Item not found")

    data = payload.dict(exclude_unset=True)

    for field, value in data.items():
        setattr(item, field, value)

    db.commit()
    db.refresh(item)
    return item

@router.get("/items", response_model=list[WardrobeItemOut])
def list_items(
    db: Session = Depends(get_db),
    # filtre
    category: str | None = Query(None),
    brand: str | None = Query(None),
    material: str | None = Query(None),
    season: str | None = Query(None),
    occasion: str | None = Query(None),
    # sortare
    sort_by: str = Query("created_at", description="created_at | wear_count | last_worn_at"),
    sort_dir: str = Query("desc", description="asc | desc"),
    # paginație simplă (opțional)
    limit: int = Query(50, ge=1, le=200),
    offset: int = Query(0, ge=0),
):
    query = db.query(WardrobeItem)

    # aplicăm filtre doar dacă sunt setate
    if category:
        query = query.filter(WardrobeItem.category == category)
    if brand:
        query = query.filter(WardrobeItem.brand == brand)
    if material:
        query = query.filter(WardrobeItem.material == material)
    if season:
        query = query.filter(WardrobeItem.season == season)
    if occasion:
        query = query.filter(WardrobeItem.occasion == occasion)

    # mapare câmp sort_by -> coloană
    sort_field_map = {
        "created_at": WardrobeItem.created_at,
        "wear_count": WardrobeItem.wear_count,
        "last_worn_at": WardrobeItem.last_worn_at,
    }
    sort_col = sort_field_map.get(sort_by, WardrobeItem.created_at)

    if sort_dir == "asc":
        query = query.order_by(asc(sort_col))
    else:
        query = query.order_by(desc(sort_col))

    items = query.offset(offset).limit(limit).all()
    return items

@router.post("/items/{item_id}/wear", response_model=WardrobeItemOut)
def mark_item_worn(
    item_id: int,
    db: Session = Depends(get_db),
):
    """
    Marchează un item ca fiind purtat acum:
    - crește wear_count
    - setează last_worn_at = acum
    """
    item = db.query(WardrobeItem).filter(WardrobeItem.id == item_id).first()
    if not item:
        raise HTTPException(status_code=404, detail="Item not found")

    item.wear_count += 1
    item.last_worn_at = datetime.utcnow()

    db.commit()
    db.refresh(item)
    return item

# @router.get("/items", response_model=list[WardrobeItemOut])
# def list_items(db: Session = Depends(get_db)):
#     """
#     Returnează toate articolele din garderobă.
#     """
#     items = db.query(WardrobeItem).order_by(WardrobeItem.id.desc()).all()
#     return items
