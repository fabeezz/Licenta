from fastapi import APIRouter, UploadFile, File, Depends, Query
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.schemas.wardrobe import WardrobeItemOut, WardrobeItemUpdate
from app.services.wardrobe_service import wardrobe_service

router = APIRouter(
    prefix="/wardrobe",
    tags=["wardrobe"],
)


@router.post("/items", response_model=WardrobeItemOut)
async def create_item(
    image: UploadFile = File(...),
    brand: str | None = None,
    material: str | None = None,
    season: str | None = None,
    occasion: str | None = None,
    db: Session = Depends(get_db),
):
    """
    Creează un nou articol în garderobă, rulând pipeline-ul de imagine:
    - salvare original
    - background removal
    - extragere culori
    - clasificare categorie cu CLIP
    """
    content = await image.read()
    ext = (
        image.filename.rsplit(".", 1)[1].lower()
        if image.filename and "." in image.filename
        else "png"
    )

    return wardrobe_service.create_item_with_upload(
        db,
        content,
        ext,
        brand=brand,
        material=material,
        season=season,
        occasion=occasion,
    )

@router.patch("/items/{item_id}", response_model=WardrobeItemOut)
def update_item_meta(
    item_id: int,
    payload: WardrobeItemUpdate,
    db: Session = Depends(get_db),
):
    """
    Actualizează metadatele unui articol (categorie, brand, material, sezon, ocazie).
    """
    return wardrobe_service.update_item_meta(db, item_id, payload)

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
    """
    Listează articolele din garderobă cu filtre, sortare și paginație.
    """
    return list(
        wardrobe_service.list_items(
            db,
            category=category,
            brand=brand,
            material=material,
            season=season,
            occasion=occasion,
            sort_by=sort_by,
            sort_dir=sort_dir,
            limit=limit,
            offset=offset,
        )
    )

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
    return wardrobe_service.mark_item_worn(db, item_id)

@router.get("/stats")
def get_stats(db: Session = Depends(get_db)):
    return wardrobe_service.get_basic_stats(db)

@router.get("/recommendation", response_model=list[WardrobeItemOut])
def recommend_outfit(
    db: Session = Depends(get_db),
    occasion: str | None = Query(None),
    season: str | None = Query(None),
    limit: int = Query(3, ge=1, le=10),
):
    return wardrobe_service.recommend_simple(
        db, occasion=occasion, season=season, limit=limit
    )

# @router.get("/items", response_model=list[WardrobeItemOut])
# def list_items(db: Session = Depends(get_db)):
#     """
#     Returnează toate articolele din garderobă.
#     """
#     items = db.query(WardrobeItem).order_by(WardrobeItem.id.desc()).all()
#     return items
