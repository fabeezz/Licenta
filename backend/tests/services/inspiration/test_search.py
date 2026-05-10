"""Unit tests for inspiration search helpers.

No models or DB required — uses synthetic embeddings and mock Item objects.
"""
from __future__ import annotations

from unittest.mock import MagicMock

import numpy as np
import pytest

from app.services.inspiration.search import cosine_topk, match_inspiration, SlotMatch
from app.services.outfits.slots import OutfitSlot


def _make_item(item_id: int, category: str, embedding: np.ndarray | None) -> MagicMock:
    item = MagicMock()
    item.id = item_id
    item.category = category
    item.embedding = embedding.astype(np.float32).tobytes() if embedding is not None else None
    item.image_no_bg_name = None
    item.image_original_name = f"item_{item_id}.jpg"
    return item


def _unit_vec(d: int, idx: int) -> np.ndarray:
    v = np.zeros(d, dtype=np.float32)
    v[idx] = 1.0
    return v


def _make_tensor(embed: np.ndarray) -> MagicMock:
    tensor = MagicMock()
    tensor.cpu.return_value.numpy.return_value.squeeze.return_value = embed
    return tensor


def _make_classifier(embed: np.ndarray) -> MagicMock:
    """Classifier whose encode_image always returns *embed* and score_labels says no outer."""
    classifier = MagicMock()
    classifier.encode_image.return_value = _make_tensor(embed)
    pred = MagicMock()
    pred.label = "shirt or top"  # no outer by default
    pred.confidence = 0.9
    classifier.score_labels.return_value = pred
    return classifier


class TestCosineTopk:
    def test_returns_empty_for_no_items(self):
        query = _unit_vec(4, 0)
        assert cosine_topk(query, [], OutfitSlot.TOP) == []

    def test_filters_by_slot_category(self):
        items = [
            _make_item(1, "t-shirt", _unit_vec(4, 0)),   # TOP
            _make_item(2, "jeans",   _unit_vec(4, 1)),   # BOTTOM
            _make_item(3, "shirt",   _unit_vec(4, 0)),   # TOP
        ]
        query = _unit_vec(4, 0)
        results = cosine_topk(query, items, OutfitSlot.TOP, k=5)
        ids = [item.id for item, _ in results]
        assert 2 not in ids, "BOTTOM item should be excluded from TOP search"
        assert set(ids) == {1, 3}

    def test_items_without_embedding_are_skipped(self):
        items = [
            _make_item(1, "t-shirt", _unit_vec(4, 0)),
            _make_item(2, "shirt",   None),
        ]
        query = _unit_vec(4, 0)
        results = cosine_topk(query, items, OutfitSlot.TOP, k=5)
        ids = [item.id for item, _ in results]
        assert 2 not in ids

    def test_ordering_is_descending_similarity(self):
        v0 = _unit_vec(4, 0)
        v1 = np.array([0.9, 0.1, 0.0, 0.0], dtype=np.float32)
        v2 = np.array([0.5, 0.5, 0.0, 0.0], dtype=np.float32)
        items = [
            _make_item(1, "sneakers", v1),
            _make_item(2, "shoes",    v2),
            _make_item(3, "boots",    v0),
        ]
        query = v0
        results = cosine_topk(query, items, OutfitSlot.SHOES, k=3)
        ids = [item.id for item, _ in results]
        assert ids[0] == 3, "Exact match should rank first"

    def test_respects_k_limit(self):
        items = [_make_item(i, "sneakers", _unit_vec(4, i % 4)) for i in range(1, 6)]
        query = _unit_vec(4, 0)
        results = cosine_topk(query, items, OutfitSlot.SHOES, k=2)
        assert len(results) <= 2

    def test_scores_are_in_valid_range(self):
        items = [_make_item(i, "jeans", _unit_vec(4, i % 4)) for i in range(1, 4)]
        query = _unit_vec(4, 0)
        for _, score in cosine_topk(query, items, OutfitSlot.BOTTOM, k=5):
            assert -1.0 <= score <= 1.0 + 1e-6

    def test_dress_maps_to_top_slot(self):
        items = [_make_item(1, "dress", _unit_vec(4, 0))]
        query = _unit_vec(4, 0)
        results = cosine_topk(query, items, OutfitSlot.TOP, k=5)
        assert len(results) == 1

    def test_color_rerank_falls_back_on_missing_image(self):
        """Color re-ranking must not crash if item image is missing on disk."""
        from PIL import Image as PILImage
        ref_img = PILImage.new("RGB", (32, 32), color=(200, 100, 50))
        items = [_make_item(1, "t-shirt", _unit_vec(4, 0))]
        query = _unit_vec(4, 0)
        # Should not raise even though item image file does not exist.
        results = cosine_topk(query, items, OutfitSlot.TOP, k=5, ref_img=ref_img)
        assert len(results) == 1
        _, score = results[0]
        assert score > 0


class TestMatchInspiration:
    def test_empty_wardrobe_returns_none_best(self, monkeypatch):
        from app.services.inspiration import search as search_mod

        monkeypatch.setattr(search_mod._segmenter, "segment", lambda img: {})
        embed = _unit_vec(8, 0)
        classifier = _make_classifier(embed)
        from PIL import Image as PILImage
        img = PILImage.new("RGB", (64, 64))
        result = match_inspiration(img, classifier, [])
        assert result.top.best is None
        assert result.shoes.best is None

    def test_best_item_selected_per_slot(self, monkeypatch):
        from app.services.inspiration import search as search_mod
        from app.services.outfits.slots import OutfitSlot as Slot

        top_embed = _unit_vec(8, 0)
        shoe_embed = _unit_vec(8, 1)

        # Encode order with A+B: (0) global, (1) TOP crop (outer-detect + slot), (2) SHOES crop.
        embeds = [top_embed, top_embed, shoe_embed]
        call_count = [0]

        def _encode(img):
            vec = embeds[call_count[0]]
            call_count[0] += 1
            return _make_tensor(vec)

        pred = MagicMock()
        pred.label = "shirt or top"  # no outer
        pred.confidence = 0.9

        classifier = MagicMock()
        classifier.encode_image.side_effect = _encode
        classifier.score_labels.return_value = pred

        monkeypatch.setattr(search_mod._segmenter, "segment", lambda img: {
            Slot.TOP: MagicMock(),
            Slot.SHOES: MagicMock(),
        })

        items = [
            _make_item(1, "t-shirt",  top_embed),
            _make_item(2, "sneakers", shoe_embed),
        ]
        from PIL import Image as PILImage
        img = PILImage.new("RGB", (64, 64))
        result = match_inspiration(img, classifier, items)
        assert result.top.best is not None
        assert result.top.best.id == 1
        assert result.shoes.best is not None
        assert result.shoes.best.id == 2

    def test_outer_omitted_when_no_outer_in_reference(self, monkeypatch):
        from app.services.inspiration import search as search_mod
        from app.services.outfits.slots import OutfitSlot as Slot

        embed = _unit_vec(8, 0)
        classifier = _make_classifier(embed)

        monkeypatch.setattr(search_mod._segmenter, "segment", lambda img: {
            Slot.TOP: MagicMock(),
        })

        outer_item = _make_item(1, "jacket", _unit_vec(8, 0))
        from PIL import Image as PILImage
        img = PILImage.new("RGB", (64, 64))
        result = match_inspiration(img, classifier, [outer_item])
        assert result.outer.best is None, "Outer should be omitted when reference has no outer"

    def test_outer_suggested_when_coat_detected(self, monkeypatch):
        from app.services.inspiration import search as search_mod
        from app.services.outfits.slots import OutfitSlot as Slot

        embed = _unit_vec(8, 0)

        pred = MagicMock()
        pred.label = "jacket or coat"
        pred.confidence = 0.8

        classifier = MagicMock()
        classifier.encode_image.return_value = _make_tensor(embed)
        classifier.score_labels.return_value = pred

        monkeypatch.setattr(search_mod._segmenter, "segment", lambda img: {
            Slot.TOP: MagicMock(),
        })

        outer_item = _make_item(1, "jacket", embed)
        from PIL import Image as PILImage
        img = PILImage.new("RGB", (64, 64))
        result = match_inspiration(img, classifier, [outer_item])
        assert result.outer.best is not None, "Outer should be present when coat is detected"
        assert result.outer.best.id == 1

    def test_dress_suppresses_bottom(self, monkeypatch):
        from app.services.inspiration import search as search_mod

        embed = _unit_vec(8, 0)
        classifier = _make_classifier(embed)

        monkeypatch.setattr(search_mod._segmenter, "segment", lambda img: {})

        dress_item = _make_item(1, "dress", embed)
        bottom_item = _make_item(2, "jeans", embed)
        from PIL import Image as PILImage
        img = PILImage.new("RGB", (64, 64))
        result = match_inspiration(img, classifier, [dress_item, bottom_item])
        if result.top.best is not None and result.top.best.category == "dress":
            assert result.bottom.best is None, "Bottom should be suppressed when top is a dress"

    def test_cross_slot_dedup(self, monkeypatch):
        from app.services.inspiration import search as search_mod

        embed = _unit_vec(8, 0)
        classifier = _make_classifier(embed)

        monkeypatch.setattr(search_mod._segmenter, "segment", lambda img: {})

        # One item that falls into both TOP (via dress) and OUTER slot won't happen via category
        # filtering, but test that the same id doesn't appear as best in two slots.
        items = [
            _make_item(1, "t-shirt", embed),
            _make_item(2, "jacket", embed),
            _make_item(3, "jeans", embed),
            _make_item(4, "sneakers", embed),
        ]
        from PIL import Image as PILImage
        img = PILImage.new("RGB", (64, 64))
        result = match_inspiration(img, classifier, items)
        bests = [
            m.best.id for m in (result.top, result.bottom, result.outer, result.shoes)
            if m.best is not None
        ]
        assert len(bests) == len(set(bests)), "Same item should not be best in multiple slots"
