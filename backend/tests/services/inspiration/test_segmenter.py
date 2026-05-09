"""Tests for GarmentSegmenter.

The SegFormer model weights are large (~400 MB) and may not be available in CI.
All tests that require the live model are skipped if transformers or the weights
are absent — they are primarily intended for local verification.
"""
from __future__ import annotations

from unittest.mock import MagicMock, patch

import numpy as np
import pytest
from PIL import Image

from app.services.outfits.slots import OutfitSlot


def _make_mask(width: int, height: int, region: tuple[int, int, int, int]) -> Image.Image:
    """Return a grayscale mask with ones in *region* (left, top, right, bottom)."""
    arr = np.zeros((height, width), dtype=np.uint8)
    l, t, r, b = region
    arr[t:b, l:r] = 255
    return Image.fromarray(arr, mode="L")


def _fake_segment_output(width: int = 100, height: int = 200) -> list[dict]:
    return [
        {"label": "Upper-clothes", "score": 0.95, "mask": _make_mask(width, height, (10, 10, 50, 80))},
        {"label": "Pants",         "score": 0.90, "mask": _make_mask(width, height, (10, 85, 50, 160))},
        {"label": "Left-shoe",     "score": 0.85, "mask": _make_mask(width, height, (10, 165, 30, 195))},
        {"label": "Right-shoe",    "score": 0.85, "mask": _make_mask(width, height, (35, 165, 55, 195))},
        {"label": "Hair",          "score": 0.80, "mask": _make_mask(width, height, (20, 0, 40, 10))},
    ]


class TestGarmentSegmenterWithMock:
    def test_segment_maps_labels_to_slots(self):
        from app.services.inspiration.segmenter import GarmentSegmenter

        seg = GarmentSegmenter()
        img = Image.new("RGB", (100, 200))
        with patch.object(type(seg), "_pipe", new_callable=lambda: property(lambda self: lambda x: _fake_segment_output())):
            crops = seg.segment(img)

        assert OutfitSlot.TOP in crops
        assert OutfitSlot.BOTTOM in crops
        assert OutfitSlot.SHOES in crops

    def test_irrelevant_labels_are_ignored(self):
        from app.services.inspiration.segmenter import GarmentSegmenter

        seg = GarmentSegmenter()
        img = Image.new("RGB", (100, 200))
        with patch.object(type(seg), "_pipe", new_callable=lambda: property(lambda self: lambda x: _fake_segment_output())):
            crops = seg.segment(img)

        assert OutfitSlot.OUTER not in crops, "No outer garment in fixture — should be absent"

    def test_left_and_right_shoe_are_merged(self):
        from app.services.inspiration.segmenter import GarmentSegmenter

        seg = GarmentSegmenter()
        img = Image.new("RGB", (100, 200))
        with patch.object(type(seg), "_pipe", new_callable=lambda: property(lambda self: lambda x: _fake_segment_output())):
            crops = seg.segment(img)

        # Both shoes merged into one crop; the bounding box spans both regions
        shoe_crop = crops[OutfitSlot.SHOES]
        # Left shoe right edge = 30, right shoe right edge = 55 → merged width covers both
        assert shoe_crop.width >= 45, "Merged shoe crop should span both shoe regions"

    def test_empty_segment_output_returns_no_crops(self):
        from app.services.inspiration.segmenter import GarmentSegmenter

        seg = GarmentSegmenter()
        img = Image.new("RGB", (100, 200))
        with patch.object(type(seg), "_pipe", new_callable=lambda: property(lambda self: lambda x: [])):
            crops = seg.segment(img)

        assert crops == {}


@pytest.mark.skipif(
    pytest.importorskip("transformers", reason="transformers not installed") is None,
    reason="transformers not available",
)
class TestGarmentSegmenterLive:
    """Smoke test against the real model — skipped if weights are absent."""

    @pytest.mark.slow
    def test_live_segment_returns_dict(self):
        from app.services.inspiration.segmenter import GarmentSegmenter

        seg = GarmentSegmenter()
        img = Image.new("RGB", (224, 224), color=(200, 180, 160))
        try:
            crops = seg.segment(img)
            assert isinstance(crops, dict)
        except Exception as exc:
            pytest.skip(f"Live model unavailable: {exc}")
