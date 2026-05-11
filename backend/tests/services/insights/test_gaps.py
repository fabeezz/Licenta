"""Unit tests for the wardrobe gap analysis module."""
from __future__ import annotations

from unittest.mock import MagicMock

from app.services.insights.gaps import Gap, _analyze_full, analyze


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


# ── Empty wardrobe (bypasses onboarding guard via _analyze_full) ──────────────

class TestEmptyWardrobe:
    def test_all_slots_missing(self):
        gaps = _analyze_full([])
        assert _keys(gaps, "slot") == {"top", "bottom", "outer", "shoes"}

    def test_all_weather_missing(self):
        gaps = _analyze_full([])
        assert _keys(gaps, "weather") == {"warm", "cold", "rainy"}

    def test_all_styles_missing(self):
        gaps = _analyze_full([])
        assert _keys(gaps, "style") == {"casual", "formal", "sporty"}

    def test_color_neutrals_missing(self):
        gaps = _analyze_full([])
        assert "neutrals" in _keys(gaps, "color")


# ── Slot coverage ─────────────────────────────────────────────────────────────

class TestSlotCoverage:
    def test_one_tshirt_top_low_not_missing(self):
        items = [_item(category="t-shirt")]
        sevs = _severities(_analyze_full(items), "slot")
        assert sevs.get("top") == "low"

    def test_two_tshirts_no_top_gap(self):
        items = [_item(category="t-shirt"), _item(category="t-shirt")]
        keys = _keys(_analyze_full(items), "slot")
        assert "top" not in keys

    def test_only_tops_other_slots_missing(self):
        items = [_item(category="t-shirt"), _item(category="t-shirt")]
        keys = _keys(_analyze_full(items), "slot")
        assert {"bottom", "outer", "shoes"}.issubset(keys)

    def test_unknown_category_does_not_credit_slots(self):
        items = [_item(category="hat")]
        keys = _keys(_analyze_full(items), "slot")
        assert {"top", "bottom", "outer", "shoes"}.issubset(keys)

    def test_dress_credits_both_top_and_bottom(self):
        items = [_item(category="dress"), _item(category="dress")]
        keys = _keys(_analyze_full(items), "slot")
        assert "top" not in keys
        assert "bottom" not in keys

    def test_dress_satisfies_outfit_completability_top_bottom(self):
        items = (
            [_item(category="dress") for _ in range(2)]
            + [_item(category="sneakers") for _ in range(2)]
        )
        keys = _keys(_analyze_full(items), "outfit")
        assert "completability" not in keys


# ── Weather coverage ──────────────────────────────────────────────────────────

class TestWeatherCoverage:
    def test_all_weather_item_credits_all_tags(self):
        items = [_item(weather=["all-weather"]) for _ in range(3)]
        keys = _keys(_analyze_full(items), "weather")
        assert not {"warm", "cold", "rainy"}.intersection(keys)

    def test_warm_only_cold_and_rainy_flagged(self):
        items = [_item(weather=["warm"]) for _ in range(3)]
        keys = _keys(_analyze_full(items), "weather")
        assert "cold" in keys
        assert "rainy" in keys
        assert "warm" not in keys

    def test_one_rainy_item_low_not_missing(self):
        items = [_item(weather=["rainy"])]
        sevs = _severities(_analyze_full(items), "weather")
        assert sevs.get("rainy") == "low"


# ── Style coverage ────────────────────────────────────────────────────────────

class TestStyleCoverage:
    def test_no_formal_flagged(self):
        items = [_item(style=["casual"]) for _ in range(5)]
        keys = _keys(_analyze_full(items), "style")
        assert "formal" in keys
        assert "casual" not in keys

    def test_one_formal_low(self):
        items = [_item(style=["formal"])]
        sevs = _severities(_analyze_full(items), "style")
        assert sevs.get("formal") == "low"

    def test_three_formal_no_gap(self):
        items = [_item(style=["formal"]) for _ in range(3)]
        keys = _keys(_analyze_full(items), "style")
        assert "formal" not in keys


# ── Color balance ─────────────────────────────────────────────────────────────

class TestColorBalance:
    def test_no_colors_neutrals_missing(self):
        items = [_item() for _ in range(5)]  # no color_tags
        keys = _keys(_analyze_full(items), "color")
        assert "neutrals" in keys

    def test_only_colorful_no_neutrals_missing(self):
        items = [_item(dominant_color="red") for _ in range(5)]
        keys = _keys(_analyze_full(items), "color")
        assert "neutrals" in keys

    def test_dark_neutrals_suppress_neutrals_missing(self):
        items = [_item(dominant_color="black") for _ in range(5)]
        keys = _keys(_analyze_full(items), "color")
        assert "neutrals" not in keys

    def test_very_few_colorful_flagged_low(self):
        # 10 items: 9 dark, 1 colorful → colorful < 15 %
        items = [_item(dominant_color="black") for _ in range(9)]
        items.append(_item(dominant_color="red"))
        sevs = _severities(_analyze_full(items), "color")
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


# ── Onboarding mode ───────────────────────────────────────────────────────────

class TestOnboardingMode:
    def test_empty_wardrobe_returns_onboarding_gap(self):
        gaps = analyze([])
        assert len(gaps) == 1
        assert gaps[0].dimension == "onboarding"
        assert gaps[0].key == "starter"

    def test_nine_items_returns_onboarding_gap(self):
        items = [_item(category="t-shirt") for _ in range(9)]
        gaps = analyze(items)
        assert len(gaps) == 1
        assert gaps[0].dimension == "onboarding"

    def test_ten_items_skips_onboarding(self):
        items = [_item(category="t-shirt") for _ in range(10)]
        gaps = analyze(items)
        assert not any(g.dimension == "onboarding" for g in gaps)


# ── Outfit completability ─────────────────────────────────────────────────────

class TestOutfitCompletability:
    def test_missing_shoes_emits_completability_first(self):
        # tops + bottoms but no shoes
        items = (
            [_item(category="t-shirt", weather=["warm"], style=["casual"], dominant_color="white") for _ in range(5)]
            + [_item(category="jeans", weather=["all-weather"], style=["casual"], dominant_color="navy") for _ in range(5)]
        )
        gaps = _analyze_full(items)
        assert gaps[0].dimension == "outfit"
        assert gaps[0].key == "completability"
        assert gaps[0].severity == "missing"

    def test_all_required_slots_present_no_completability_gap(self):
        items = (
            [_item(category="t-shirt") for _ in range(4)]
            + [_item(category="jeans") for _ in range(4)]
            + [_item(category="sneakers") for _ in range(4)]
        )
        keys = _keys(_analyze_full(items), "outfit")
        assert "completability" not in keys

    def test_outer_not_required_for_completability(self):
        # no jacket/outer — should still be completable
        items = (
            [_item(category="t-shirt") for _ in range(4)]
            + [_item(category="jeans") for _ in range(4)]
            + [_item(category="sneakers") for _ in range(4)]
        )
        gaps = _analyze_full(items)
        assert not any(g.dimension == "outfit" for g in gaps)


# ── Color bucket fix ──────────────────────────────────────────────────────────

class TestColorBucketFix:
    def test_burgundy_olive_not_counted_as_neutrals(self):
        # 5 burgundy + 5 olive → previously counted as dark, suppressing "neutrals missing"
        # after fix, they are dark-colorful and should NOT suppress neutrals
        items = (
            [_item(dominant_color="burgundy") for _ in range(5)]
            + [_item(dominant_color="olive") for _ in range(5)]
        )
        keys = _keys(_analyze_full(items), "color")
        assert "neutrals" in keys

    def test_burgundy_contributes_to_colorful(self):
        # 9 black (neutral) + 1 burgundy → colorful < 15 % → low
        items = [_item(dominant_color="black") for _ in range(9)]
        items.append(_item(dominant_color="burgundy"))
        sevs = _severities(_analyze_full(items), "color")
        assert sevs.get("colorful") == "low"

    def test_black_navy_gray_counted_as_dark_neutrals(self):
        items = (
            [_item(dominant_color="black") for _ in range(4)]
            + [_item(dominant_color="navy") for _ in range(3)]
            + [_item(dominant_color="gray") for _ in range(3)]
        )
        keys = _keys(_analyze_full(items), "color")
        assert "neutrals" not in keys


# ── Proportional thresholds ───────────────────────────────────────────────────

class TestProportionalThresholds:
    def test_large_wardrobe_4_tops_is_low(self):
        # 50 items, slot threshold = max(2, round(0.12 * 50)) = max(2, 6) = 6
        # 4 tops < 6 → low
        items = (
            [_item(category="t-shirt") for _ in range(4)]
            + [_item(category="jeans", weather=["warm"], style=["casual"], dominant_color="navy") for _ in range(24)]
            + [_item(category="sneakers", weather=["warm"], style=["casual"], dominant_color="white") for _ in range(22)]
        )
        assert len(items) == 50
        sevs = _severities(_analyze_full(items), "slot")
        assert sevs.get("top") == "low"

    def test_small_wardrobe_4_tops_no_gap(self):
        # 10 items, slot threshold = max(2, round(0.12 * 10)) = max(2, 1) = 2
        # 4 tops >= 2 → no top gap (though 0 outer/shoes → missing)
        items = (
            [_item(category="t-shirt") for _ in range(4)]
            + [_item(category="jeans") for _ in range(4)]
            + [_item(category="sneakers") for _ in range(2)]
        )
        assert len(items) == 10
        keys = _keys(_analyze_full(items), "slot")
        assert "top" not in keys
