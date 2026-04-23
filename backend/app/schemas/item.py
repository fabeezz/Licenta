from datetime import datetime
from pydantic import BaseModel

class ItemOut(BaseModel):
    id: int
    image_original_name: str
    image_no_bg_name: str | None = None
    category: str | None = None
    color_tags: dict | None = None

    brand: str | None = None
    material: str | None = None
    season: str | None = None
    occasion: str | None = None

    wear_count: int
    last_worn_at: datetime | None = None

    created_at: datetime

    model_config = {"from_attributes": True}


class ItemUpdate(BaseModel):
    """
    Payload pentru PATCH /item/update/{id}.
    Toate câmpurile opționale → update parțial.
    """
    category: str | None = None
    brand: str | None = None
    material: str | None = None
    season: str | None = None
    occasion: str | None = None
    color_tags: dict | None = None
