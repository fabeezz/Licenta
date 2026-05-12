from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, field_validator

from app.schemas._validators import lowercase_str, lowercase_str_list
from app.schemas.item import ItemMinimal


class OutfitCreate(BaseModel):
    """Payload for ``POST /outfits``."""

    name: str
    shoe_id: int
    bottom_id: int
    top_id: int
    outer_id: int | None = None
    weather: list[str] = []
    style: str | None = None
    source: Literal["manual", "inspiration"] = "manual"

    @field_validator("style", mode="before")
    @classmethod
    def to_lowercase(cls, v: str | None) -> str | None:
        return lowercase_str(v)

    @field_validator("weather", mode="before")
    @classmethod
    def weather_to_lowercase(cls, v: list[str] | None) -> list[str]:
        return lowercase_str_list(v) or []


class OutfitUpdate(BaseModel):
    """Partial update payload for ``PATCH /outfits/{id}``."""

    name: str | None = None
    weather: list[str] | None = None
    style: str | None = None

    @field_validator("style", mode="before")
    @classmethod
    def to_lowercase(cls, v: str | None) -> str | None:
        return lowercase_str(v)

    @field_validator("weather", mode="before")
    @classmethod
    def weather_to_lowercase(cls, v: list[str] | None) -> list[str] | None:
        return lowercase_str_list(v)


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
    source: str


class OutfitSuggestRequest(BaseModel):
    """Body for ``POST /outfits/suggest``."""

    style: str | None = None
    weather: str | None = None
    modes: list[str] | None = None


class OutfitSuggestResponse(BaseModel):
    """Suggested item IDs per slot; ``None`` means no suitable item found for that slot."""

    top: int | None = None
    bottom: int | None = None
    outer: int | None = None
    shoes: int | None = None


class InspirationSlotMatch(BaseModel):
    """Top-ranked items for a single slot from an inspiration search."""

    best: ItemMinimal | None
    alternates: list[ItemMinimal]
    score: float | None


class InspirationResponse(BaseModel):
    """Result of POST /outfits/from-image; one ranked slot-match per outfit slot."""

    source_image_url: str
    top: InspirationSlotMatch
    bottom: InspirationSlotMatch
    outer: InspirationSlotMatch
    shoes: InspirationSlotMatch
