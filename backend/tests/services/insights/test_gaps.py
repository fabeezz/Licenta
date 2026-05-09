"""Unit tests for the wardrobe gap analysis module."""
from __future__ import annotations

from unittest.mock import MagicMock

import pytest

from app.services.insights.gaps import Gap, analyze


def _item(
    *,
    category: str | None = None,
    weather: list[str] | None = None,
    style: list[str] | None = None,
    dominant_color: str | None = None,
) -> MagicMock:
    item = MagicMock()
    item.category = category
    item.weather = weather or []
    item.style = style or []
    item.color_tags = {"dominant": [dominant_color]} if dominant_color else None
    return item


def _keys(gaps: list[Gap], dimension: str) -> set[str]:
    return {g.key for g in gaps if g.dimension == dimension}


def _severities(gaps: list[Gap], dimension: str) -> dict[str, str]:
    return {g.key: g.severity for g in gaps if g.dimension == dimension}


# ── Empty wardrobe ────────────────────────────────────────────────────────────

class TestEmptyWardrobe:
    def test_all_slots_missing(self):
        gaps = analyze([])
        assert _keys(gaps, "slot") == {"top", "bottom", "outer", "shoes"}

    def test_all_weather_missing(self):
        gaps = analyze([])
        assert _keys(gaps, "weather") == {"warm", "cold", "rainy"}

    def test_all_styles_missing(self):
        gaps = analyze([])
        assert _keys(gaps, "style") == {"casual", "formal", "sporty"}

    def test_color_neutrals_missing(self):
        gaps = analyze([])
        assert "neutrals" in _keys(gaps, "color")


# ── Slot coverage ─────────────────────────────────────────────────────────────

class TestSlotCoverage:
    def test_one_tshirt_top_low_not_missing(self):
        items = [_item(category="t-shirt")]
        sevs = _severities(analyze(items), "slot")
        assert sevs.get("top") == "low"

    def test_two_tshirts_no_top_gap(self):
        items = [_item(category="t-shirt"), _item(category="t-shirt")]
        keys = _keys(analyze(items), "slot")
        assert "top" not in keys

    def test_only_tops_other_slots_missing(self):
        items = [_item(category="t-shirt"), _item(category="t-shirt")]
        keys = _keys(analyze(items), "slot")
        assert {"bottom", "outer", "shoes"}.issubset(keys)

    def test_unknown_category_does_not_credit_slots(self):
        items = [_item(category="hat")]
        keys = _keys(analyze(items), "slot")
        assert {"top", "bottom", "outer", "shoes"}.issubset(keys)


# ── Weather coverage ──────────────────────────────────────────────────────────

class TestWeatherCoverage:
    def test_all_weather_item_credits_all_tags(self):
        items = [_item(weather=["all-weather"]) for _ in range(3)]
        keys = _keys(analyze(items), "weather")
        assert not {"warm", "cold", "rainy"}.intersection(keys)

    def test_warm_only_cold_and_rainy_flagged(self):
        items = [_item(weather=["warm"]) for _ in range(3)]
        keys = _keys(analyze(items), "weather")
        assert "cold" in keys
        assert "rainy" in keys
        assert "warm" not in keys

    def test_one_rainy_item_low_not_missing(self):
        items = [_item(weather=["rainy"])]
        sevs = _severities(analyze(items), "weather")
        assert sevs.get("rainy") == "low"


# ── Style coverage ────────────────────────────────────────────────────────────

class TestStyleCoverage:
    def test_no_formal_flagged(self):
        items = [_item(style=["casual"]) for _ in range(5)]
        keys = _keys(analyze(items), "style")
        assert "formal" in keys
        assert "casual" not in keys

    def test_one_formal_low(self):
        items = [_item(style=["formal"])]
        sevs = _severities(analyze(items), "style")
        assert sevs.get("formal") == "low"

    def test_three_formal_no_gap(self):
        items = [_item(style=["formal"]) for _ in range(3)]
        keys = _keys(analyze(items), "style")
        assert "formal" not in keys


# ── Color balance ─────────────────────────────────────────────────────────────

class TestColorBalance:
    def test_no_colors_neutrals_missing(self):
        items = [_item() for _ in range(5)]  # no color_tags
        keys = _keys(analyze(items), "color")
        assert "neutrals" in keys

    def test_only_colorful_no_neutrals_missing(self):
        items = [_item(dominant_color="red") for _ in range(5)]
        keys = _keys(analyze(items), "color")
        assert "neutrals" in keys

    def test_dark_neutrals_suppress_neutrals_missing(self):
        items = [_item(dominant_color="black") for _ in range(5)]
        keys = _keys(analyze(items), "color")
        assert "neutrals" not in keys

    def test_very_few_colorful_flagged_low(self):
        # 10 items: 9 dark, 1 colorful → colorful < 15 %
        items = [_item(dominant_color="black") for _ in range(9)]
        items.append(_item(dominant_color="red"))
        sevs = _severities(analyze(items), "color")
        assert sevs.get("colorful") == "low"


# ── Balanced wardrobe produces no gaps ───────────────────────────────────────

class TestBalancedWardrobe:
    def test_well_stocked_wardrobe_returns_empty(self):
        items = (
            [_item(category="t-shirt", weather=["warm"], style=["casual"], dominant_color="white") for _ in range(3)]
            + [_item(category="jeans", weather=["all-weather"], style=["casual"], dominant_color="navy") for _ in range(3)]
            + [_item(category="jacket", weather=["cold"], style=["casual", "formal"], dominant_color="black") for _ in range(3)]
            + [_item(category="sneakers", weather=["all-weather"], style=["casual", "sporty"], dominant_color="gray") for _ in range(3)]
            + [_item(category="shirt", weather=["warm"], style=["formal"], dominant_color="red") for _ in range(3)]
            + [_item(category="pants", weather=["all-weather"], style=["formal", "sporty"], dominant_color="beige") for _ in range(2)]
        )
        gaps = analyze(items)
        assert gaps == [], f"Expected no gaps, got: {gaps}"
