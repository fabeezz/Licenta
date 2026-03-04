from __future__ import annotations

import random
import time

from collections import defaultdict
from dataclasses import dataclass
from itertools import product
from typing import Iterable

from sqlalchemy.orm import Session

from app.models.item import Item
from app.services.outfits.slots import OutfitSlot, category_to_slot
from app.services.outfits.color_rules import norm_color, score_outfit


def _dominant_color(item: Item) -> str | None:
    tags = item.color_tags or {}
    dom = tags.get("dominant")
    if isinstance(dom, list) and dom:
        return norm_color(dom[0])
    return None


def _season_ok(item_season: str | None, requested: str | None) -> bool:
    if not requested:
        return True
    if item_season is None:
        return True
    s = item_season.strip().lower()
    r = requested.strip().lower()
    return s == r or s == "all-season"


def _occasion_ok(item_occ: str | None, requested: str | None) -> bool:
    if not requested:
        return True
    if item_occ is None:
        return True
    o = item_occ.strip().lower()
    r = requested.strip().lower()
    return o == r


@dataclass(frozen=True)
class OutfitCandidate:
    top: Item
    bottom: Item
    outer: Item
    shoes: Item
    score: float


class OutfitService:
    def recommend_mvp1(
        self,
        db: Session,
        *,
        user_id: int,
        season: str | None = None,
        occasion: str | None = None,
        limit: int = 6,
        candidate_cap: int = 5000,
        diversity_lambda: float = 3.0,
        explore: bool = True,
        pool_size: int = 300,
        seed: int | None = None,
    ) -> list[OutfitCandidate]:
        items: list[Item] = (
            db.query(Item)
            .filter(Item.user_id == user_id)
            .all()
        )

        # slot them
        slots: dict[OutfitSlot, list[Item]] = {
            OutfitSlot.TOP: [],
            OutfitSlot.BOTTOM: [],
            OutfitSlot.OUTER: [],
            OutfitSlot.SHOES: [],
        }

        for it in items:
            if not _season_ok(it.season, season):
                continue
            if not _occasion_ok(it.occasion, occasion):
                continue
            slot = category_to_slot(it.category)
            if slot:
                slots[slot].append(it)

        tops = slots[OutfitSlot.TOP]
        bottoms = slots[OutfitSlot.BOTTOM]
        outers = slots[OutfitSlot.OUTER]
        shoes = slots[OutfitSlot.SHOES]

        if not (tops and bottoms and outers and shoes):
            return []

        # build candidates (cap if huge)
        all_prod = len(tops) * len(bottoms) * len(outers) * len(shoes)
        combos: Iterable[tuple[Item, Item, Item, Item]]
        if all_prod <= candidate_cap:
            combos = product(tops, bottoms, outers, shoes)
        else:
            # simple sampling without extra deps: take first N from product after slicing lists
            # (MVP simplu; dacă vrei sampling real random, îți dau variantă cu random)
            max_each = int(candidate_cap ** 0.25) + 1
            combos = product(tops[:max_each], bottoms[:max_each], outers[:max_each], shoes[:max_each])

        candidates: list[OutfitCandidate] = []
        for t, b, o, s in combos:
            sc = score_outfit(
                top_color=_dominant_color(t),
                bottom_color=_dominant_color(b),
                outer_color=_dominant_color(o),
                shoes_color=_dominant_color(s),
            )
            candidates.append(OutfitCandidate(top=t, bottom=b, outer=o, shoes=s, score=sc))

        if not candidates:
            return []

        candidates.sort(key=lambda x: x.score, reverse=True)

        # Diversitate simplă: greedy cu penalizare la reuse
        selected: list[OutfitCandidate] = []
        used_count: dict[int, int] = defaultdict(int)

        def adjusted_score(c: OutfitCandidate) -> float:
            penalty = diversity_lambda * (
                used_count[c.top.id]
                + used_count[c.bottom.id]
                + used_count[c.outer.id]
                + used_count[c.shoes.id]
            )
            return c.score - penalty

        # explore: randomizează ordinea în top pool, ca să nu fie mereu aceleași
        pool_n = min(pool_size, len(candidates))
        remaining = candidates[:pool_n].copy()

        if explore:
            # seed per-request (dacă nu trimiți seed), ca să varieze la fiecare call
            rnd = random.Random(seed if seed is not None else time.time_ns())
            rnd.shuffle(remaining)


        for _ in range(min(limit, len(remaining))):
            best = max(remaining, key=adjusted_score)
            selected.append(best)

            used_count[best.top.id] += 1
            used_count[best.bottom.id] += 1
            used_count[best.outer.id] += 1
            used_count[best.shoes.id] += 1

            remaining.remove(best)

        return selected


outfit_service = OutfitService()
