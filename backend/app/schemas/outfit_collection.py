from __future__ import annotations

from pydantic import BaseModel, Field

from app.schemas.outfit import OutfitResponse


class CollectionCreate(BaseModel):
    name: str
    outfit_ids: list[int] = Field(min_length=1)


class CollectionUpdate(BaseModel):
    name: str | None = None


class CollectionResponse(BaseModel):
    model_config = {"from_attributes": True}

    id: int
    name: str
    outfits: list[OutfitResponse]
