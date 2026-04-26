from __future__ import annotations

import httpx

from app.core.exceptions import WeatherUnavailableError
from app.schemas.weather import HourPoint, WeatherTodayResponse


class WeatherService:
    def __init__(self, client: httpx.AsyncClient, base_url: str) -> None:
        self._client = client
        self._base_url = base_url

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
