from datetime import datetime
from typing import Dict, List, Optional

from pydantic import BaseModel


class WardrobeItemOut(BaseModel):
    id: int
    image_original_name: str
    image_no_bg_name: Optional[str] = None
    category: Optional[str] = None
    color_tags: Optional[Dict[str, List[str]]] = None
    created_at: datetime

    class Config:
        from_attributes = True