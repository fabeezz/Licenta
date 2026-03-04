from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.item import ItemOut
from app.services.item_service import item_service
from app.schemas.outfit import OutfitOut, OutfitItemRef
from app.services.outfits.outfit_service import outfit_service

router = APIRouter(
    prefix="/wardrobe",
    tags=["wardrobes"],
)


@router.get("/items", response_model=list[ItemOut])
def list_wardrobe_items(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    category: str | None = Query(None, description="Category of the item"),
    brand: str | None = Query(None, description="Brand of the item"),
    dominant_color: str | None = Query(None, description="Dominant color of the item"),
    material: str | None = Query(None, description="Material of the item"),
    season: str | None = Query(None, description="Season of the item"),
    occasion: str | None = Query(None, description="Occasion of the item"),
    sort_by: str = Query("created_at", description="Sort by created_at, wear_count, last_worn_at"),
    sort_dir: str = Query("desc", description="Sort direction asc, desc"),
    limit: int = Query(50, ge=1, le=200),
    offset: int = Query(0, ge=0),
):
    return list(
        item_service.list_items(
            db,
            user_id=current_user.id,
            category=category,
            brand=brand,
            dominant_color=dominant_color,
            material=material,
            season=season,
            occasion=occasion,
            sort_by=sort_by,
            sort_dir=sort_dir,
            limit=limit,
            offset=offset,
        )
    )


# @router.get("/recommendation", response_model=list[ItemOut])
# def recommend_outfit(
#     db: Session = Depends(get_db),
#     current_user: User = Depends(get_current_user),
#     occasion: str | None = Query(None),
#     season: str | None = Query(None),
#     limit: int = Query(3, ge=1, le=10),
# ):
#     return item_service.recommend_simple(
#         db, user_id=current_user.id, occasion=occasion, season=season, limit=limit
#     )


@router.get("/stats")
def get_stats(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return item_service.get_basic_stats(db, user_id=current_user.id)

@router.get("/outfits", response_model=list[OutfitOut])
def recommend_outfits_mvp1(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    season: str | None = Query(None),
    occasion: str | None = Query(None),
    limit: int = Query(6, ge=1, le=20),
):
    outfits = outfit_service.recommend_mvp1(
        db,
        user_id=current_user.id,
        season=season,
        occasion=occasion,
        limit=limit,
    )

    def ref(it):
        dom = None
        if it.color_tags and isinstance(it.color_tags.get("dominant"), list) and it.color_tags["dominant"]:
            dom = it.color_tags["dominant"][0]
        return OutfitItemRef(
            id=it.id,
            category=it.category,
            image_no_bg_name=it.image_no_bg_name,
            image_original_name=it.image_original_name,
            dominant_color=dom,
        )

    return [
        OutfitOut(
            top=ref(o.top),
            bottom=ref(o.bottom),
            outer=ref(o.outer),
            shoes=ref(o.shoes),
            score=o.score,
        )
        for o in outfits
    ]
