from __future__ import annotations

from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field, field_validator


class ItemBase(BaseModel):
    """Shared read fields for all item response shapes."""

    model_config = {"from_attributes": True}

    id: int
    image_original_name: str
    image_no_bg_name: str | None = None


class ItemMinimal(ItemBase):
    """Minimal item projection used inside outfit responses."""

    dominant_color: str | None = None


class ItemOut(ItemBase):
    """Full item representation returned by item CRUD endpoints."""

    category: str | None = None
    color_tags: dict | None = None
    dominant_color: str | None = None
    brand: str | None = None
    material: str | None = None
    weather: list[str] = []
    occasion: str | None = None
    wear_count: int
    last_worn_at: datetime | None = None
    created_at: datetime


class ItemUpdate(BaseModel):
    """Partial update payload for ``PATCH /items/{id}``."""

    category: str | None = None
    brand: str | None = None
    material: str | None = None
    weather: list[str] | None = None
    occasion: str | None = None
    color_tags: dict | None = None

    @field_validator("category", "material", "occasion", mode="before")
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

    @field_validator("color_tags", mode="before")
    @classmethod
    def color_tags_to_lowercase(cls, v: dict | None) -> dict | None:
        """Enforce lowercase for all color tags."""
        if not isinstance(v, dict):
            return v
        
        lowered = {}
        for key, val in v.items():
            if isinstance(val, list):
                lowered[key] = [i.lower() if isinstance(i, str) else i for i in val]
            elif isinstance(val, str):
                lowered[key] = val.lower()
            else:
                lowered[key] = val
        return lowered

    @field_validator("brand", mode="before")
    @classmethod
    def to_title_case(cls, v: str | None) -> str | None:
        """Convert the brand name to Title Case."""
        if isinstance(v, str):
            return v.title()
        return v


class ItemListQuery(BaseModel):
    """Validated query parameters for ``GET /items``."""

    category: str | None = None
    brand: str | None = None
    dominant_color: str | None = None
    colors: list[str] | None = None
    material: str | None = None
    weather: str | None = None
    occasion: str | None = None
    sort_by: Literal["created_at", "wear_count", "last_worn_at"] = "created_at"
    sort_dir: Literal["asc", "desc"] = "desc"
    limit: int = Field(50, ge=1, le=200)
    offset: int = Field(0, ge=0)

    @field_validator("category", "material", "weather", "occasion", "dominant_color", mode="before")
    @classmethod
    def to_lowercase(cls, v: str | None) -> str | None:
        if isinstance(v, str):
            return v.lower()
        return v

    @field_validator("colors", mode="before")
    @classmethod
    def list_to_lowercase(cls, v: list[str] | None) -> list[str] | None:
        """Enforce lowercase for list of strings."""
        if isinstance(v, list):
            return [i.lower() if isinstance(i, str) else i for i in v]
        return v

    @field_validator("brand", mode="before")
    @classmethod
    def to_title_case(cls, v: str | None) -> str | None:
        """Convert the brand name to Title Case."""
        if isinstance(v, str):
            return v.title()
        return v
