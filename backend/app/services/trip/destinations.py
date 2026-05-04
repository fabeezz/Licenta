from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class Destination:
    key: str
    city: str
    country: str
    flag: str
    lat: float
    lon: float


DESTINATIONS: dict[str, Destination] = {
    d.key: d
    for d in [
        Destination("paris", "Paris", "France", "🇫🇷", 48.8566, 2.3522),
        Destination("london", "London", "United Kingdom", "🇬🇧", 51.5074, -0.1278),
        Destination("milan", "Milan", "Italy", "🇮🇹", 45.4642, 9.1900),
        Destination("rome", "Rome", "Italy", "🇮🇹", 41.9028, 12.4964),
        Destination("barcelona", "Barcelona", "Spain", "🇪🇸", 41.3851, 2.1734),
        Destination("tokyo", "Tokyo", "Japan", "🇯🇵", 35.6762, 139.6503),
        Destination("new_york", "New York", "United States", "🇺🇸", 40.7128, -74.0060),
        Destination("bali", "Bali", "Indonesia", "🇮🇩", -8.3405, 115.0920),
        Destination("amsterdam", "Amsterdam", "Netherlands", "🇳🇱", 52.3676, 4.9041),
        Destination("dubai", "Dubai", "UAE", "🇦🇪", 25.2048, 55.2708),
    ]
}
