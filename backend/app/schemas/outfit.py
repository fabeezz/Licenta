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
