from __future__ import annotations

from collections.abc import Sequence

import numpy as np

from app.models.item import Item


def cosine_search(
    query_embed: np.ndarray,
    items: Sequence[Item],
    *,
    limit: int = 50,
    min_score: float = 0.03,
) -> list[tuple[Item, float]]:
    """Rank *items* by cosine similarity against *query_embed*.

    Items missing an embedding are skipped. Only results with score >=
    *min_score* are returned (keeps clearly-unrelated items out of results).
    """
    scored: list[tuple[Item, float]] = []
    for item in items:
        if item.embedding is None:
            continue
        emb = np.frombuffer(item.embedding, dtype=np.float32)
        score = float(np.dot(query_embed, emb))
        if score >= min_score:
            scored.append((item, score))
    scored.sort(key=lambda x: x[1], reverse=True)
    return scored[:limit]
