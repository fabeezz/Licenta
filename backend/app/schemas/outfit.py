from pydantic import BaseModel


class OutfitItemRef(BaseModel):
    id: int
    category: str | None = None
    image_no_bg_name: str | None = None
    image_original_name: str
    dominant_color: str | None = None


class OutfitOut(BaseModel):
    top: OutfitItemRef
    bottom: OutfitItemRef
    outer: OutfitItemRef
    shoes: OutfitItemRef
    score: float


# ── Manual outfit saving (CRUD) ──────────────────────────────────────────────

class ItemMinimal(BaseModel):
    model_config = {"from_attributes": True}

    id: int
    image_original_name: str
    image_no_bg_name: str | None


class OutfitCreate(BaseModel):
    name: str
    shoe_id: int
    bottom_id: int
    top_id: int
    outer_id: int | None = None
    season: str | None = None
    occasion: str | None = None


class OutfitUpdate(BaseModel):
    name: str | None = None
    season: str | None = None
    occasion: str | None = None


class OutfitResponse(BaseModel):
    model_config = {"from_attributes": True}

    id: int
    user_id: int
    name: str
    season: str | None
    occasion: str | None
    shoe: ItemMinimal
    bottom: ItemMinimal
    top: ItemMinimal
    outer: ItemMinimal | None
