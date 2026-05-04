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
from app.services.trip.activity_map import style_for_day
from app.services.trip.destinations import DESTINATIONS
from app.services.weather_service import WeatherService

# ── Category → slot mapping ───────────────────────────────────────────────────

_TOP_CATS = {"t-shirt", "shirt", "hoodie", "sweater", "dress"}
_THIN_TOP_CATS = {"t-shirt", "shirt"}        # used when an outer layer is added
_BOTTOM_CATS = {"jeans", "pants", "shorts", "skirt"}
_SHOE_CATS = {"sneakers", "shoes", "boots"}
_OUTER_CATS = {"jacket", "coat", "blazer"}
_BAG_CATS = {"bag", "backpack"}

# ── Weather-tag thresholds (Celsius) ─────────────────────────────────────────

_SNOW_THRESHOLD = 0.0
_COLD_THRESHOLD = 15.0
_RAINY_THRESHOLD = 1.0  # mm precipitation


def _day_weather_tags(forecast: DayForecast) -> list[str]:
    tags: list[str] = []
    avg = (forecast.temp_max_c + forecast.temp_min_c) / 2
    if avg <= _SNOW_THRESHOLD:
        tags.append("snow")
        tags.append("cold")
    elif avg < _COLD_THRESHOLD:
        tags.append("cold")
    else:
        tags.append("warm")
    if forecast.precip_mm >= _RAINY_THRESHOLD:
        tags.append("rainy")
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
        bags = [i for i in all_items if i.category in _BAG_CATS]

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
            bags=bags,
            top_pool=[],
            bottom_pool=[],
            shoe_pool=[],
            outer_pool=[],
            bag_size="checked",  # travel outfit pools are independent / unrestricted
            pool_limit=999,
        )
        outfits.append(travel_outfit)

        # Build per-day activity lookup (keyed by ISO date string — DayForecast.date is a string)
        day_act_map: dict[str, list[str]] = {}
        if req.day_activities:
            for da in req.day_activities:
                day_act_map[da.date.isoformat()] = da.activities

        # ── Per-day outfits ──────────────────────────────────────────────────
        for i, day in enumerate(forecast):
            weather_tags = _day_weather_tags(day)
            day_acts = day_act_map.get(day.date, req.activities if not req.day_activities else [])
            style = style_for_day(day_acts)
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
                bags=bags,
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
        bags: list[Item],
        top_pool: list[Item],
        bottom_pool: list[Item],
        shoe_pool: list[Item],
        outer_pool: list[Item],
        bag_size: BagSize,
        pool_limit: int,
    ) -> GeneratedOutfit:
        warnings: list[str] = []
        is_snowy = "snow" in weather_tags
        is_cold = "cold" in weather_tags
        is_rainy = "rainy" in weather_tags
        needs_outer = is_cold or is_rainy or is_snowy

        # Rain is additive for clothing — strip it from top/bottom/shoe matching so sporty/formal
        # items aren't excluded just because it's raining. Outers use full tags (rainy IS the reason).
        clothing_weather_tags = [t for t in weather_tags if t != "rainy"]

        def filtered(items: list[Item], match_style: bool = True) -> list[Item]:
            return [
                i for i in items
                if _item_matches_weather(i, clothing_weather_tags)
                and (not match_style or _item_matches_style(i, style))
            ]

        def filtered_outer(items: list[Item], match_style: bool = True) -> list[Item]:
            # Outers match against full weather tags (including rainy) since that's why we add them.
            return [
                i for i in items
                if _item_matches_weather(i, weather_tags)
                and (not match_style or _item_matches_style(i, style))
            ]

        # Top: when layering, restrict to thin pieces (t-shirt/shirt) so hoodie+jacket is avoided.
        if needs_outer:
            thin_tops = [t for t in tops if t.category in _THIN_TOP_CATS]
            top_cands = filtered(thin_tops) or filtered(tops)  # fall back to all tops if thin unavailable
        else:
            top_cands = filtered(tops)

        bottom_cands = filtered(bottoms)
        shoe_cands = filtered(shoes)

        # Snow: prefer boots
        if is_snowy:
            boot_cands = [s for s in shoe_cands if s.category == "boots"]
            shoe_cands_final = boot_cands if boot_cands else shoe_cands
        else:
            shoe_cands_final = shoe_cands

        # Outer: prefer style match + weather match; fall back progressively so a weather-appropriate
        # outer is always preferred over nothing, even if the style doesn't align.
        if needs_outer:
            styled_weather = filtered_outer(outers, match_style=True)    # style + weather
            any_weather   = filtered_outer(outers, match_style=False)    # weather, any style
            any_outer     = filtered(outers, match_style=False)          # last resort: ignore weather tag
            outer_cands_all = styled_weather or any_weather or any_outer
            if is_snowy:
                coat_cands = [o for o in outer_cands_all if o.category == "coat"]
                outer_cands = coat_cands if coat_cands else outer_cands_all
            else:
                outer_cands = outer_cands_all
        else:
            outer_cands = []

        top = _pick_with_pool(top_cands, top_pool, pool_limit, bag_size)
        shoe = _pick_with_pool(shoe_cands_final, shoe_pool, pool_limit, bag_size)
        outer = _pick_with_pool(outer_cands, outer_pool, pool_limit, bag_size) if needs_outer else None

        # Dress skips bottom — if a dress was chosen as the top, no separate bottom is needed.
        if top is not None and top.category == "dress":
            bottom = None
        else:
            bottom = _pick_with_pool(bottom_cands, bottom_pool, pool_limit, bag_size)
            if bottom is None:
                warnings.append(f"No {style} bottom found for {day_label}")

        # Bag — only for travel day; picked from user's bags if available.
        bag: Item | None = None
        if is_travel and bags:
            bag = random.choice(bags)

        if top is None:
            warnings.append(f"No {style} top found for {day_label}")
        if shoe is None:
            warnings.append(f"No suitable shoes found for {day_label}")
        if needs_outer and outer is None:
            if is_snowy:
                warnings.append(f"No heavy coat found for {day_label} — pack your warmest layer")
            elif is_cold:
                warnings.append(f"No outer layer found for {day_label}")
            else:
                warnings.append(f"No rain layer found for {day_label} — consider a light jacket")

        if is_snowy:
            cond_label = "Snowy"
        elif is_rainy and is_cold:
            cond_label = "Cold & Rainy"
        elif is_rainy:
            cond_label = "Rainy"
        elif is_cold:
            cond_label = "Cold"
        else:
            cond_label = "Sunny"
        weather_note = cond_label

        return GeneratedOutfit(
            day_label=day_label,
            is_travel=is_travel,
            slots=GeneratedOutfitSlots(
                top=_to_minimal(top) if top else None,
                bottom=_to_minimal(bottom) if bottom else None,
                shoes=_to_minimal(shoe) if shoe else None,
                outer=_to_minimal(outer) if outer else None,
                bag=_to_minimal(bag) if bag else None,
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
                is_trip=True,
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
