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
    return item


def _unit_vec(d: int, idx: int) -> np.ndarray:
    v = np.zeros(d, dtype=np.float32)
    v[idx] = 1.0
    return v


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


class TestMatchInspiration:
    def _make_classifier(self, embed: np.ndarray) -> MagicMock:
        classifier = MagicMock()
        tensor = MagicMock()
        tensor.cpu.return_value.numpy.return_value.squeeze.return_value = embed
        classifier.encode_image.return_value = tensor
        return classifier

    def test_empty_wardrobe_returns_none_best(self, monkeypatch):
        from app.services.inspiration import search as search_mod

        monkeypatch.setattr(search_mod._segmenter, "segment", lambda img: {})
        embed = _unit_vec(8, 0)
        classifier = self._make_classifier(embed)
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

        monkeypatch.setattr(search_mod._segmenter, "segment", lambda img: {
            Slot.TOP: MagicMock(),
            Slot.SHOES: MagicMock(),
        })

        call_count = [0]
        def _encode(img):
            tensor = MagicMock()
            idx = call_count[0] % 2
            call_count[0] += 1
            vec = top_embed if idx == 0 else shoe_embed
            tensor.cpu.return_value.numpy.return_value.squeeze.return_value = vec
            return tensor

        classifier = MagicMock()
        classifier.encode_image.side_effect = _encode

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
