from __future__ import annotations

from datetime import date
from typing import Literal

from pydantic import BaseModel

from app.schemas.item import ItemMinimal
from app.schemas.weather import DayForecast

BagSize = Literal["carry_on", "checked", "both"]


# ── Request schemas ──────────────────────────────────────────────────────────

class DayActivities(BaseModel):
    date: date
    activities: list[str] = []


class TripGenerateRequest(BaseModel):
    city_key: str
    start_date: date
    end_date: date
    bag_size: BagSize
    activities: list[str]
    day_activities: list[DayActivities] | None = None


class TripSaveRequest(BaseModel):
    city_key: str
    start_date: date
    end_date: date
    bag_size: BagSize
    activities: list[str]
    collection_name: str
    outfits: list["GeneratedOutfitIn"]


# ── Generated plan schemas ───────────────────────────────────────────────────

class GeneratedOutfitIn(BaseModel):
    """Outfit data sent back from client when saving."""
    day_label: str
    is_travel: bool = False
    top_id: int | None = None
    bottom_id: int | None = None
    shoe_id: int | None = None
    outer_id: int | None = None
    bag_id: int | None = None
    style: str | None = None
    weather_tags: list[str] = []


class GeneratedOutfitSlots(BaseModel):
    top: ItemMinimal | None = None
    bottom: ItemMinimal | None = None
    shoes: ItemMinimal | None = None
    outer: ItemMinimal | None = None
    bag: ItemMinimal | None = None


class GeneratedOutfit(BaseModel):
    day_label: str
    is_travel: bool = False
    slots: GeneratedOutfitSlots
    style: str | None = None
    weather_note: str = ""
    weather_tags: list[str] = []
    warnings: list[str] = []


class TripPlanResponse(BaseModel):
    city: str
    country: str
    city_key: str
    flag: str
    start_date: date
    end_date: date
    bag_size: BagSize
    forecast: list[DayForecast]
    outfits: list[GeneratedOutfit]
    global_warnings: list[str] = []


# ── Persisted trip schemas ───────────────────────────────────────────────────

class DestinationOut(BaseModel):
    key: str
    city: str
    country: str
    flag: str
    lat: float
    lon: float


class TripRead(BaseModel):
    model_config = {"from_attributes": True}

    id: int
    city: str
    country: str
    city_key: str
    start_date: date
    end_date: date
    bag_size: str
    activities: list[str]
    collection_id: int | None
