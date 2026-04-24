from __future__ import annotations

from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field


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
    season: str | None = None
    occasion: str | None = None
    wear_count: int
    last_worn_at: datetime | None = None
    created_at: datetime


class ItemUpdate(BaseModel):
    """Partial update payload for ``PATCH /items/{id}``.

    ``color_tags`` is intentionally excluded — it is AI-derived and not
    user-settable via the API.
    """

    category: str | None = None
    brand: str | None = None
    material: str | None = None
    season: str | None = None
    occasion: str | None = None


class ItemListQuery(BaseModel):
    """Validated query parameters for ``GET /items``."""

    category: str | None = None
    brand: str | None = None
    dominant_color: str | None = None
    colors: list[str] | None = None
    material: str | None = None
    season: str | None = None
    occasion: str | None = None
    sort_by: Literal["created_at", "wear_count", "last_worn_at"] = "created_at"
    sort_dir: Literal["asc", "desc"] = "desc"
    limit: int = Field(50, ge=1, le=200)
    offset: int = Field(0, ge=0)
