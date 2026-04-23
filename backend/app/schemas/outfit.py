from pydantic import BaseModel, model_validator
from typing import Any


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
    dominant_color: str | None = None

    @model_validator(mode="before")
    @classmethod
    def extract_dominant_color(cls, data: Any) -> Any:
        if isinstance(data, dict):
            if not data.get("dominant_color") and "color_tags" in data:
                tags = data["color_tags"]
                if tags and "dominant" in tags and tags["dominant"]:
                    data["dominant_color"] = tags["dominant"][0]
            return data
        
        # For ORM objects (from_attributes=True)
        # If the object has color_tags but no dominant_color attribute,
        # we can't easily add an attribute to the ORM object without risks.
        # However, we can return a proxy or a dict.
        # Most reliable way in Pydantic V2 to augment ORM loading:
        if not hasattr(data, "dominant_color") and hasattr(data, "color_tags"):
            tags = data.color_tags
            if tags and "dominant" in tags and tags["dominant"]:
                # We can return a dict that pydantic will use instead of the object
                return {
                    "id": data.id,
                    "image_original_name": data.image_original_name,
                    "image_no_bg_name": data.image_no_bg_name,
                    "dominant_color": tags["dominant"][0]
                }
        return data


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
