from __future__ import annotations

import math
from datetime import date

import httpx

from app.core.exceptions import WeatherUnavailableError
from app.schemas.weather import DayForecast, HourPoint, WeatherTodayResponse


class WeatherService:
    def __init__(self, client: httpx.AsyncClient, base_url: str) -> None:
        self._client = client
        self._base_url = base_url

    async def get_forecast_range(
        self,
        *,
        lat: float,
        lon: float,
        start: date,
        end: date,
    ) -> list[DayForecast]:
        """Return daily forecast for *start*–*end* inclusive (Celsius)."""
        params = {
            "latitude": lat,
            "longitude": lon,
            "daily": "temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code",
            "start_date": start.isoformat(),
            "end_date": end.isoformat(),
            "timezone": "auto",
        }
        try:
            r = await self._client.get(self._base_url, params=params, timeout=10.0)
            r.raise_for_status()
        except httpx.HTTPError as exc:
            raise WeatherUnavailableError(str(exc)) from exc

        daily = r.json()["daily"]
        return [
            DayForecast(
                date=daily["time"][i],
                temp_max_c=daily["temperature_2m_max"][i],
                temp_min_c=daily["temperature_2m_min"][i],
                precip_mm=daily["precipitation_sum"][i] or 0.0,
                weather_code=daily["weather_code"][i],
            )
            for i in range(len(daily["time"]))
        ]

    async def get_today(self, *, lat: float, lon: float) -> WeatherTodayResponse:
        params = {
            "latitude": lat,
            "longitude": lon,
            "current": "temperature_2m,precipitation,weather_code",
            "hourly": "temperature_2m,precipitation,weather_code",
            "forecast_days": 1,
            "timezone": "auto",
        }
        try:
            r = await self._client.get(self._base_url, params=params, timeout=10.0)
            r.raise_for_status()
        except httpx.HTTPError as exc:
            raise WeatherUnavailableError(str(exc)) from exc

        data = r.json()
        current = data["current"]
        hourly = data["hourly"]

        hours = [
            HourPoint(
                time=hourly["time"][i],
                temp_c=hourly["temperature_2m"][i],
                precip_mm=hourly["precipitation"][i],
                code=hourly["weather_code"][i],
            )
            for i in range(len(hourly["time"]))
        ]

        return WeatherTodayResponse(
            current_temp_c=current["temperature_2m"],
            current_precip_mm=current["precipitation"],
            current_code=current["weather_code"],
            hours=hours,
        )
