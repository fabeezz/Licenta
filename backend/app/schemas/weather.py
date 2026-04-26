from __future__ import annotations

from pydantic import BaseModel


class HourPoint(BaseModel):
    time: str
    temp_c: float
    precip_mm: float
    code: int


class WeatherTodayResponse(BaseModel):
    current_temp_c: float
    current_precip_mm: float
    current_code: int
    hours: list[HourPoint]
