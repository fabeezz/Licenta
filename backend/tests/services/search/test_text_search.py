"""Unit tests for text_search.cosine_search.

No models or DB required — uses synthetic embeddings and mock Item objects.
"""
from __future__ import annotations

from unittest.mock import MagicMock

import numpy as np
import pytest

from app.services.search.text_search import cosine_search


def _make_item(item_id: int, embedding: np.ndarray | None) -> MagicMock:
    item = MagicMock()
    item.id = item_id
    item.embedding = embedding.astype(np.float32).tobytes() if embedding is not None else None
    return item


def _unit_vec(d: int, idx: int) -> np.ndarray:
    v = np.zeros(d, dtype=np.float32)
    v[idx] = 1.0
    return v


D = 32  # embedding dimension for tests


def test_ranking_order():
    """Items closer to the query are returned first."""
    query = _unit_vec(D, 0)  # points along dimension 0

    # item_1 aligns perfectly, item_2 is orthogonal, item_3 partially aligns
    item_1 = _make_item(1, _unit_vec(D, 0))       # score = 1.0
    item_3 = _make_item(3, (_unit_vec(D, 0) + _unit_vec(D, 1)) / np.sqrt(2))  # score ≈ 0.707
    item_2 = _make_item(2, _unit_vec(D, 1))       # score = 0.0 (below min_score default)

    results = cosine_search(query, [item_2, item_3, item_1], min_score=0.0)

    ids = [item.id for item, _ in results]
    assert ids[0] == 1
    assert ids[1] == 3
    assert ids[2] == 2


def test_min_score_cutoff():
    """Items scoring below min_score are excluded."""
    query = _unit_vec(D, 0)
    high = _make_item(1, _unit_vec(D, 0))    # score = 1.0
    low = _make_item(2, _unit_vec(D, 1))     # score = 0.0

    results = cosine_search(query, [high, low], min_score=0.9)
    assert len(results) == 1
    assert results[0][0].id == 1


def test_items_without_embedding_skipped():
    """Items with embedding=None are not included in results."""
    query = _unit_vec(D, 0)
    no_embed = _make_item(1, None)
    has_embed = _make_item(2, _unit_vec(D, 0))

    results = cosine_search(query, [no_embed, has_embed], min_score=0.0)
    ids = [item.id for item, _ in results]
    assert 1 not in ids
    assert 2 in ids


def test_empty_items_returns_empty():
    query = _unit_vec(D, 0)
    assert cosine_search(query, []) == []


def test_limit_is_respected():
    """At most *limit* results are returned."""
    query = _unit_vec(D, 0)
    items = [_make_item(i, _unit_vec(D, 0)) for i in range(20)]
    results = cosine_search(query, items, limit=5, min_score=0.0)
    assert len(results) <= 5


def test_scores_are_attached():
    """Each result tuple carries a float score."""
    query = _unit_vec(D, 0)
    item = _make_item(1, _unit_vec(D, 0))
    results = cosine_search(query, [item], min_score=0.0)
    assert len(results) == 1
    _, score = results[0]
    assert isinstance(score, float)
    assert pytest.approx(score, abs=1e-5) == 1.0
