from __future__ import annotations

from pydantic import BaseModel, field_validator

from app.schemas.item import ItemMinimal


class OutfitItemRef(ItemMinimal):
    """Item reference used in AI recommendation responses."""

    category: str | None = None


class OutfitOut(BaseModel):
    """AI recommendation result (not a CRUD outfit)."""

    top: OutfitItemRef
    bottom: OutfitItemRef
    outer: OutfitItemRef
    shoes: OutfitItemRef
    score: float


class OutfitCreate(BaseModel):
    """Payload for ``POST /outfits``."""

    name: str
    shoe_id: int
    bottom_id: int
    top_id: int
    outer_id: int | None = None
    weather: list[str] = []
    style: str | None = None

    @field_validator("style", mode="before")
    @classmethod
    def to_lowercase(cls, v: str | None) -> str | None:
        if isinstance(v, str):
            return v.lower()
        return v

    @field_validator("weather", mode="before")
    @classmethod
    def weather_to_lowercase(cls, v: list[str] | None) -> list[str]:
        if isinstance(v, list):
            return [t.lower() if isinstance(t, str) else t for t in v]
        return []


class OutfitUpdate(BaseModel):
    """Partial update payload for ``PATCH /outfits/{id}``."""

    name: str | None = None
    weather: list[str] | None = None
    style: str | None = None

    @field_validator("style", mode="before")
    @classmethod
    def to_lowercase(cls, v: str | None) -> str | None:
        if isinstance(v, str):
            return v.lower()
        return v

    @field_validator("weather", mode="before")
    @classmethod
    def weather_to_lowercase(cls, v: list[str] | None) -> list[str] | None:
        if isinstance(v, list):
            return [t.lower() if isinstance(t, str) else t for t in v]
        return v


class OutfitResponse(BaseModel):
    """Full outfit representation returned by outfit CRUD endpoints."""

    model_config = {"from_attributes": True}

    id: int
    user_id: int
    name: str
    weather: list[str]
    style: str | None
    shoe: ItemMinimal
    bottom: ItemMinimal
    top: ItemMinimal
    outer: ItemMinimal | None
