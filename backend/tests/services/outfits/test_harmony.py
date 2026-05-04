"""Unit tests for the harmony scoring and suggestion module."""
from __future__ import annotations

import random
from unittest.mock import MagicMock

import pytest

from app.services.outfits.harmony import (
    HarmonyMode,
    NEUTRALS,
    score_outfit,
    suggest_outfit,
)


def _item(category: str, dominant_color: str | None = None) -> MagicMock:
    """Build a minimal mock Item."""
    item = MagicMock()
    item.category = category
    item.dominant_color = dominant_color
    item.color_tags = {"dominant": [dominant_color]} if dominant_color else None
    return item


# ── score_outfit ─────────────────────────────────────────────────────────────

class TestNeutralAnchor:
    def test_all_neutrals_get_anchor_bonus(self):
        combo = {
            "top": _item("t-shirt", "black"),
            "bottom": _item("jeans", "navy"),
            "shoes": _item("sneakers", "white"),
        }
        s = score_outfit(combo, HarmonyMode.NEUTRAL_ACCENT)
        assert s >= 2.0  # anchor bonus

    def test_no_neutrals_no_anchor_bonus(self):
        combo = {
            "top": _item("t-shirt", "red"),
            "bottom": _item("jeans", "green"),
            "shoes": _item("sneakers", "blue"),
        }
        s = score_outfit(combo, HarmonyMode.NEUTRAL_ACCENT)
        assert s < 2.0


class TestBoldCap:
    def test_three_bold_colors_penalty(self):
        combo = {
            "top": _item("t-shirt", "red"),
            "bottom": _item("jeans", "green"),
            "shoes": _item("sneakers", "blue"),
            "outer": _item("jacket", "orange"),
        }
        s = score_outfit(combo, HarmonyMode.NEUTRAL_ACCENT)
        # 4 bold colors → −2 penalty (2 beyond cap)
        assert s <= 0


class TestMonochromeMode:
    def test_same_tonal_group_gets_bonus(self):
        # blue and cyan are in the same tonal group
        combo = {
            "top": _item("t-shirt", "blue"),
            "bottom": _item("jeans", "navy"),  # navy is neutral
            "shoes": _item("sneakers", "cyan"),
        }
        s = score_outfit(combo, HarmonyMode.MONOCHROME)
        assert s > 0

    def test_different_groups_no_bonus(self):
        combo = {
            "top": _item("t-shirt", "red"),
            "bottom": _item("jeans", "blue"),
            "shoes": _item("sneakers", "white"),
        }
        no_bonus = score_outfit(combo, HarmonyMode.MONOCHROME)
        # red and blue are different tonal groups
        combo_same = {
            "top": _item("t-shirt", "red"),
            "bottom": _item("jeans", "burgundy"),
            "shoes": _item("sneakers", "white"),
        }
        with_bonus = score_outfit(combo_same, HarmonyMode.MONOCHROME)
        assert with_bonus > no_bonus


class TestComplementaryMode:
    def test_blue_orange_detected(self):
        combo = {
            "top": _item("t-shirt", "blue"),
            "bottom": _item("jeans", "orange"),
            "shoes": _item("sneakers", "white"),
        }
        s = score_outfit(combo, HarmonyMode.COMPLEMENTARY)
        assert s > 0

    def test_red_green_detected(self):
        combo = {
            "top": _item("t-shirt", "red"),
            "bottom": _item("jeans", "green"),
            "shoes": _item("sneakers", "black"),
        }
        s = score_outfit(combo, HarmonyMode.COMPLEMENTARY)
        assert s > 0

    def test_non_complementary_pair_no_bonus(self):
        combo = {
            "top": _item("t-shirt", "red"),
            "bottom": _item("jeans", "orange"),
            "shoes": _item("sneakers", "white"),
        }
        s = score_outfit(combo, HarmonyMode.COMPLEMENTARY)
        # Red and orange are not complementary
        complementary_combo = {
            "top": _item("t-shirt", "red"),
            "bottom": _item("jeans", "green"),
            "shoes": _item("sneakers", "white"),
        }
        s_comp = score_outfit(complementary_combo, HarmonyMode.COMPLEMENTARY)
        assert s_comp > s


class TestAnalogousMode:
    def test_neighbors_on_wheel_get_bonus(self):
        # orange and yellow are neighbors
        combo = {
            "top": _item("t-shirt", "orange"),
            "bottom": _item("jeans", "yellow"),
            "shoes": _item("sneakers", "black"),
        }
        s = score_outfit(combo, HarmonyMode.ANALOGOUS)
        assert s > 0

    def test_far_apart_no_bonus(self):
        combo = {
            "top": _item("t-shirt", "red"),
            "bottom": _item("jeans", "blue"),
            "shoes": _item("sneakers", "white"),
        }
        s = score_outfit(combo, HarmonyMode.ANALOGOUS)
        neighbor_combo = {
            "top": _item("t-shirt", "blue"),
            "bottom": _item("jeans", "cyan"),
            "shoes": _item("sneakers", "white"),
        }
        s_neighbor = score_outfit(neighbor_combo, HarmonyMode.ANALOGOUS)
        assert s_neighbor > s


class TestLayeringPenalty:
    def test_hoodie_plus_coat_penalized(self):
        with_penalty = score_outfit({
            "top": _item("hoodie", "gray"),
            "outer": _item("coat", "black"),
            "shoes": _item("boots", "black"),
        }, HarmonyMode.NEUTRAL_ACCENT)

        without_penalty = score_outfit({
            "top": _item("t-shirt", "white"),
            "outer": _item("coat", "black"),
            "shoes": _item("boots", "black"),
        }, HarmonyMode.NEUTRAL_ACCENT)

        assert without_penalty > with_penalty

    def test_sweater_plus_jacket_penalized(self):
        with_penalty = score_outfit({
            "top": _item("sweater", "beige"),
            "outer": _item("jacket", "black"),
            "shoes": _item("sneakers", "white"),
        }, HarmonyMode.NEUTRAL_ACCENT)

        without_penalty = score_outfit({
            "top": _item("shirt", "white"),
            "outer": _item("jacket", "black"),
            "shoes": _item("sneakers", "white"),
        }, HarmonyMode.NEUTRAL_ACCENT)

        assert without_penalty > with_penalty


class TestShoesTieIn:
    def test_neutral_shoes_get_bonus(self):
        with_neutral_shoes = score_outfit({
            "top": _item("t-shirt", "red"),
            "bottom": _item("jeans", "blue"),
            "shoes": _item("sneakers", "white"),
        }, HarmonyMode.NEUTRAL_ACCENT)

        with_clashing_shoes = score_outfit({
            "top": _item("t-shirt", "red"),
            "bottom": _item("jeans", "blue"),
            "shoes": _item("sneakers", "orange"),
        }, HarmonyMode.NEUTRAL_ACCENT)

        assert with_neutral_shoes > with_clashing_shoes


# ── suggest_outfit ────────────────────────────────────────────────────────────

class TestSuggestOutfit:
    def test_returns_none_for_empty_candidates(self):
        result = suggest_outfit({})
        assert result is None

    def test_returns_none_when_all_slots_empty(self):
        result = suggest_outfit({"top": [], "bottom": []})
        assert result is None

    def test_returns_dict_with_items(self):
        tops = [_item("t-shirt", "black"), _item("shirt", "white")]
        bottoms = [_item("jeans", "navy"), _item("pants", "gray")]
        shoes = [_item("sneakers", "white")]
        result = suggest_outfit({"top": tops, "bottom": bottoms, "shoes": shoes})
        assert result is not None
        assert "top" in result
        assert "bottom" in result
        assert "shoes" in result

    def test_partial_slots_ok(self):
        tops = [_item("t-shirt", "black")]
        result = suggest_outfit({"top": tops})
        assert result is not None
        assert result["top"] in tops

    def test_uses_allowed_modes(self):
        tops = [_item("t-shirt", "black"), _item("shirt", "red")]
        bottoms = [_item("jeans", "navy"), _item("pants", "green")]
        rng = random.Random(42)
        result = suggest_outfit(
            {"top": tops, "bottom": bottoms},
            allowed_modes=[HarmonyMode.NEUTRAL_ACCENT],
            rng=rng,
        )
        assert result is not None

    def test_deterministic_with_seeded_rng(self):
        tops = [_item("t-shirt", "black"), _item("shirt", "red"), _item("sweater", "blue")]
        bottoms = [_item("jeans", "navy"), _item("pants", "gray"), _item("shorts", "orange")]
        shoes = [_item("sneakers", "white"), _item("boots", "brown")]

        r1 = suggest_outfit(
            {"top": tops, "bottom": bottoms, "shoes": shoes},
            rng=random.Random(123),
        )
        r2 = suggest_outfit(
            {"top": tops, "bottom": bottoms, "shoes": shoes},
            rng=random.Random(123),
        )
        assert r1 is not None
        assert r2 is not None
        assert r1["top"] is r2["top"]
        assert r1["bottom"] is r2["bottom"]

    def test_style_consistent_combo_preferred(self):
        """suggest_outfit should prefer a same-style combo over a mixed-style one."""
        sporty_top = _item("t-shirt", "black")
        sporty_top.style = ["sporty"]
        sporty_bottom = _item("shorts", "gray")
        sporty_bottom.style = ["sporty"]
        formal_bottom = _item("pants", "navy")
        formal_bottom.style = ["formal"]

        # Two bottoms: one sporty, one formal. Top is sporty.
        # Over many runs the sporty bottom should almost always win.
        wins = 0
        for seed in range(50):
            result = suggest_outfit(
                {"top": [sporty_top], "bottom": [sporty_bottom, formal_bottom]},
                rng=random.Random(seed),
            )
            if result and result["bottom"] is sporty_bottom:
                wins += 1
        assert wins >= 40, f"Expected ≥40/50 sporty wins, got {wins}"

    def test_neutral_combo_scores_higher_than_rainbow(self):
        """With enough samples the scorer should prefer the neutral combo."""
        neutral_top = _item("t-shirt", "white")
        rainbow_top = _item("t-shirt", "red")
        bottom = _item("jeans", "navy")
        shoes = _item("sneakers", "black")

        neutral_score = score_outfit(
            {"top": neutral_top, "bottom": bottom, "shoes": shoes},
            HarmonyMode.NEUTRAL_ACCENT,
        )
        rainbow_score = score_outfit(
            {"top": rainbow_top, "bottom": _item("pants", "green"), "shoes": _item("sneakers", "blue")},
            HarmonyMode.NEUTRAL_ACCENT,
        )
        assert neutral_score > rainbow_score
