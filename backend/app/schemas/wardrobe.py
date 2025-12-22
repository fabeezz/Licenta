from pydantic import BaseModel
from typing import Optional, Dict
from datetime import datetime


class WardrobeItemOut(BaseModel):
    id: int
    image_original_name: str
    image_no_bg_name: Optional[str] = None
    category: Optional[str] = None
    color_tags: Optional[Dict] = None

    brand: Optional[str] = None
    material: Optional[str] = None
    season: Optional[str] = None
    occasion: Optional[str] = None

    wear_count: int
    last_worn_at: Optional[datetime] = None

    created_at: datetime

    class Config:
        from_attributes = True


class WardrobeItemUpdate(BaseModel):
    """
    Payload pentru PATCH /items/{id}.
    Toate câmpurile sunt opționale → upsert de meta.
    """
    category: Optional[str] = None
    brand: Optional[str] = None
    material: Optional[str] = None
    season: Optional[str] = None
    occasion: Optional[str] = None
    # dacă vrei să lași user-ul să editeze manual culoarea:
    # color_tags: Optional[Dict] = None
