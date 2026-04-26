from __future__ import annotations

from fastapi import APIRouter, Depends

from app.core.dependencies import get_current_user, get_weather_service
from app.models.user import User
from app.schemas.weather import WeatherTodayResponse
from app.services.weather_service import WeatherService

router = APIRouter(prefix="/weather", tags=["weather"])


@router.get("/today", response_model=WeatherTodayResponse)
async def get_today(
    lat: float,
    lon: float,
    _: User = Depends(get_current_user),
    weather_service: WeatherService = Depends(get_weather_service),
) -> WeatherTodayResponse:
    return await weather_service.get_today(lat=lat, lon=lon)
