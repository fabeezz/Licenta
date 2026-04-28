from __future__ import annotations

import math
import random
from datetime import date
from typing import Sequence

from app.models.item import Item
from app.models.outfit import Outfit
from app.models.outfit_collection import OutfitCollection
from app.models.trip import Trip
from app.models.user import User
from app.repositories.item_repository import ItemRepository
from app.repositories.outfit_collection_repository import OutfitCollectionRepository
from app.repositories.trip_repository import TripRepository
from app.schemas.item import ItemListQuery, ItemMinimal
from app.schemas.trip import (
    BagSize,
    DestinationOut,
    GeneratedOutfit,
    GeneratedOutfitSlots,
    TripGenerateRequest,
    TripPlanResponse,
    TripRead,
    TripSaveRequest,
)
from app.schemas.weather import DayForecast
from app.services.trip.activity_map import ACTIVITIES, dominant_style
from app.services.trip.destinations import DESTINATIONS
from app.services.weather_service import WeatherService

# ── Category → slot mapping ───────────────────────────────────────────────────

_TOP_CATS = {"t-shirt", "shirt", "hoodie", "sweater", "dress"}
_BOTTOM_CATS = {"jeans", "pants", "shorts", "skirt"}
_SHOE_CATS = {"sneakers", "shoes", "boots"}
_OUTER_CATS = {"jacket", "coat", "blazer"}

# ── Weather-tag thresholds (Celsius) ─────────────────────────────────────────

_COLD_THRESHOLD = 15.0
_RAINY_THRESHOLD = 1.0  # mm precipitation


def _day_weather_tags(forecast: DayForecast) -> list[str]:
    tags: list[str] = []
    avg = (forecast.temp_max_c + forecast.temp_min_c) / 2
    if avg < _COLD_THRESHOLD:
        tags.append("cold")
    else:
        tags.append("warm")
    if forecast.precip_mm >= _RAINY_THRESHOLD:
        tags.append("rainy")
    if not tags or tags == ["warm"]:
        pass  # already handled
    return tags or ["warm"]


def _item_matches_weather(item: Item, required_tags: list[str]) -> bool:
    """Item matches if its weather list intersects required tags OR includes 'all-weather'."""
    item_tags = set(item.weather or [])
    if "all-weather" in item_tags:
        return True
    return bool(item_tags.intersection(required_tags))


def _item_matches_style(item: Item, style: str) -> bool:
    return not item.style or style in item.style


def _to_minimal(item: Item) -> ItemMinimal:
    return ItemMinimal(
        id=item.id,
        image_original_name=item.image_original_name,
        image_no_bg_name=item.image_no_bg_name,
        dominant_color=item.dominant_color,
    )


def _pool_size(bag_size: BagSize, num_days: int) -> int:
    if bag_size == "carry_on":
        return math.ceil(num_days / 2)
    if bag_size == "checked":
        return num_days
    return num_days  # both: unique per day


def _pick_with_pool(
    candidates: list[Item],
    pool: list[Item],
    pool_limit: int,
    bag_size: BagSize,
) -> Item | None:
    """Pick one item respecting the reuse pool."""
    if not candidates:
        return None

    if bag_size == "carry_on":
        # Prefer items already in pool (reuse); only add new if pool not full
        in_pool = [c for c in candidates if c in pool]
        if in_pool:
            return random.choice(in_pool)
        if len(pool) < pool_limit:
            chosen = random.choice(candidates)
            pool.append(chosen)
            return chosen
        # Force reuse from whatever pool has
        pool_cands = [c for c in pool if c in candidates]
        return random.choice(pool_cands) if pool_cands else random.choice(pool[:pool_limit])

    # checked / both: prefer items NOT already used (unique per day)
    fresh = [c for c in candidates if c not in pool]
    chosen = random.choice(fresh) if fresh else random.choice(candidates)
    pool.append(chosen)
    return chosen


class TripService:
    def __init__(
        self,
        item_repo: ItemRepository,
        collection_repo: OutfitCollectionRepository,
        trip_repo: TripRepository,
        weather_svc: WeatherService,
        db,
    ) -> None:
        self._items = item_repo
        self._collections = collection_repo
        self._trips = trip_repo
        self._weather = weather_svc
        self._db = db

    async def list_destinations(self) -> list[DestinationOut]:
        return [
            DestinationOut(
                key=d.key,
                city=d.city,
                country=d.country,
                flag=d.flag,
                lat=d.lat,
                lon=d.lon,
            )
            for d in DESTINATIONS.values()
        ]

    async def generate_plan(self, user: User, req: TripGenerateRequest) -> TripPlanResponse:
        dest = DESTINATIONS.get(req.city_key)
        if dest is None:
            from fastapi import HTTPException, status
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Unknown destination")

        # Fetch weather forecast
        forecast = await self._weather.get_forecast_range(
            lat=dest.lat, lon=dest.lon, start=req.start_date, end=req.end_date
        )

        # Load user's full wardrobe
        all_items = self._items.list_for_user(
            user.id,
            ItemListQuery(limit=200, offset=0),
        )

        tops = [i for i in all_items if i.category in _TOP_CATS]
        bottoms = [i for i in all_items if i.category in _BOTTOM_CATS]
        shoes_all = [i for i in all_items if i.category in _SHOE_CATS]
        outers = [i for i in all_items if i.category in _OUTER_CATS]

        num_days = len(forecast)
        limit = _pool_size(req.bag_size, num_days)

        # Per-slot reuse pools (travel outfit uses separate pools)
        top_pool: list[Item] = []
        bottom_pool: list[Item] = []
        shoe_pool: list[Item] = []
        outer_pool: list[Item] = []

        outfits: list[GeneratedOutfit] = []
        global_warnings: list[str] = []

        # ── Travel outfit (casual; weather based on Day-1 forecast) ────────────
        travel_tags = _day_weather_tags(forecast[0]) if forecast else ["warm"]
        travel_style = "casual"
        travel_outfit = self._build_outfit(
            day_label="Travel",
            is_travel=True,
            style=travel_style,
            weather_tags=travel_tags,
            tops=tops,
            bottoms=bottoms,
            shoes=shoes_all,
            outers=outers,
            top_pool=[],
            bottom_pool=[],
            shoe_pool=[],
            outer_pool=[],
            bag_size="checked",  # travel outfit pools are independent / unrestricted
            pool_limit=999,
        )
        outfits.append(travel_outfit)

        # ── Per-day outfits ──────────────────────────────────────────────────
        for i, day in enumerate(forecast):
            weather_tags = _day_weather_tags(day)
            style = dominant_style(req.activities) if req.activities else "casual"
            label = f"Day {i + 1}"

            day_outfit = self._build_outfit(
                day_label=label,
                is_travel=False,
                style=style,
                weather_tags=weather_tags,
                tops=tops,
                bottoms=bottoms,
                shoes=shoes_all,
                outers=outers,
                top_pool=top_pool,
                bottom_pool=bottom_pool,
                shoe_pool=shoe_pool,
                outer_pool=outer_pool,
                bag_size=req.bag_size,
                pool_limit=limit,
            )
            outfits.append(day_outfit)

        # Collect global warnings (deduplicated)
        seen: set[str] = set()
        for o in outfits[1:]:  # skip travel for global scope
            for w in o.warnings:
                if w not in seen:
                    global_warnings.append(w)
                    seen.add(w)

        return TripPlanResponse(
            city=dest.city,
            country=dest.country,
            city_key=dest.key,
            flag=dest.flag,
            start_date=req.start_date,
            end_date=req.end_date,
            bag_size=req.bag_size,
            forecast=forecast,
            outfits=outfits,
            global_warnings=global_warnings,
        )

    def _build_outfit(
        self,
        *,
        day_label: str,
        is_travel: bool,
        style: str,
        weather_tags: list[str],
        tops: list[Item],
        bottoms: list[Item],
        shoes: list[Item],
        outers: list[Item],
        top_pool: list[Item],
        bottom_pool: list[Item],
        shoe_pool: list[Item],
        outer_pool: list[Item],
        bag_size: BagSize,
        pool_limit: int,
    ) -> GeneratedOutfit:
        warnings: list[str] = []
        needs_outer = "cold" in weather_tags or "rainy" in weather_tags

        def filtered(items: list[Item]) -> list[Item]:
            return [
                i for i in items
                if _item_matches_weather(i, weather_tags) and _item_matches_style(i, style)
            ]

        top_cands = filtered(tops)
        bottom_cands = filtered(bottoms)
        shoe_cands = filtered(shoes)
        outer_cands = filtered(outers) if needs_outer else []

        top = _pick_with_pool(top_cands, top_pool, pool_limit, bag_size)
        bottom = _pick_with_pool(bottom_cands, bottom_pool, pool_limit, bag_size)
        shoe = _pick_with_pool(shoe_cands, shoe_pool, pool_limit, bag_size)
        outer = _pick_with_pool(outer_cands, outer_pool, pool_limit, bag_size) if needs_outer else None

        if top is None:
            warnings.append(f"No {style} top found for {day_label}")
        if bottom is None:
            warnings.append(f"No {style} bottom found for {day_label}")
        if shoe is None:
            warnings.append(f"No suitable shoes found for {day_label}")
        if needs_outer and outer is None:
            tag = "warm outer" if "cold" in weather_tags else "rain layer"
            warnings.append(f"No {tag} found for {day_label}")

        cond_label = "Rainy" if "rainy" in weather_tags else ("Cold" if "cold" in weather_tags else "Sunny")
        temp_note = ""
        weather_note = cond_label

        return GeneratedOutfit(
            day_label=day_label,
            is_travel=is_travel,
            slots=GeneratedOutfitSlots(
                top=_to_minimal(top) if top else None,
                bottom=_to_minimal(bottom) if bottom else None,
                shoes=_to_minimal(shoe) if shoe else None,
                outer=_to_minimal(outer) if outer else None,
            ),
            style=style,
            weather_note=weather_note,
            weather_tags=weather_tags,
            warnings=warnings,
        )

    def save_plan(self, user: User, req: TripSaveRequest) -> TripRead:
        dest = DESTINATIONS.get(req.city_key)
        if dest is None:
            from fastapi import HTTPException, status
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Unknown destination")

        # Create collection
        collection = OutfitCollection(
            name=req.collection_name,
            user_id=user.id,
        )
        collection = self._collections.add(collection)

        # Create outfit rows and link them
        for o in req.outfits:
            if o.top_id is None or o.bottom_id is None or o.shoe_id is None:
                continue  # skip incomplete outfits (warned on client)
            outfit = Outfit(
                name=f"{dest.city} – {o.day_label}",
                user_id=user.id,
                top_id=o.top_id,
                bottom_id=o.bottom_id,
                shoe_id=o.shoe_id,
                outer_id=o.outer_id,
                weather=o.weather_tags,
                style=o.style,
            )
            self._db.add(outfit)
            self._db.flush()
            collection.outfits.append(outfit)

        # Create trip record
        trip = Trip(
            user_id=user.id,
            city=dest.city,
            country=dest.country,
            city_key=dest.key,
            lat=dest.lat,
            lon=dest.lon,
            start_date=req.start_date,
            end_date=req.end_date,
            bag_size=req.bag_size,
            activities=req.activities,
            weather_snapshot=[],
            collection_id=collection.id,
        )
        self._trips.add(trip)
        self._db.commit()
        self._db.refresh(trip)

        return TripRead.model_validate(trip)
