from __future__ import annotations

from functools import cached_property
from typing import TYPE_CHECKING

import numpy as np
import torch
from PIL import Image

from app.services.outfits.slots import OutfitSlot

if TYPE_CHECKING:
    pass

# Labels emitted by mattmdjaga/segformer_b2_clothes mapped to outfit slots.
# "Left-shoe" and "Right-shoe" are merged into a single SHOES crop.
# OUTER maps to "Upper-clothes" when wearing a coat-style top — not directly
# distinguishable from this model, so we skip it (outfit OUTER is nullable).
_LABEL_TO_SLOT: dict[str, OutfitSlot] = {
    "Upper-clothes": OutfitSlot.TOP,
    "Dress": OutfitSlot.TOP,
    "Skirt": OutfitSlot.BOTTOM,
    "Pants": OutfitSlot.BOTTOM,
    "Left-shoe": OutfitSlot.SHOES,
    "Right-shoe": OutfitSlot.SHOES,
}


def _mask_bbox(mask: Image.Image) -> tuple[int, int, int, int] | None:
    """Return (left, upper, right, lower) bounding box of non-zero mask pixels."""
    arr = np.array(mask)
    rows = np.any(arr, axis=1)
    cols = np.any(arr, axis=0)
    if not rows.any():
        return None
    top_row, bottom_row = int(np.argmax(rows)), int(len(rows) - 1 - np.argmax(rows[::-1]))
    left_col, right_col = int(np.argmax(cols)), int(len(cols) - 1 - np.argmax(cols[::-1]))
    return left_col, top_row, right_col + 1, bottom_row + 1


def _union_bbox(
    a: tuple[int, int, int, int], b: tuple[int, int, int, int]
) -> tuple[int, int, int, int]:
    return min(a[0], b[0]), min(a[1], b[1]), max(a[2], b[2]), max(a[3], b[3])


class GarmentSegmenter:
    """Lazily loaded SegFormer-B2 clothing segmenter.

    The transformers pipeline is only instantiated on first use so that
    importing this module doesn't extend cold-start time when the feature
    isn't invoked.
    """

    @cached_property
    def _pipe(self):
        from transformers import pipeline as hf_pipeline

        device = 0 if torch.cuda.is_available() else -1
        return hf_pipeline(
            "image-segmentation",
            model="mattmdjaga/segformer_b2_clothes",
            device=device,
        )

    def prewarm(self) -> None:
        """Force the lazy pipeline load so the first real request doesn't pay the cold-start cost."""
        _ = self._pipe

    def segment(self, rgb_img: Image.Image) -> dict[OutfitSlot, Image.Image]:
        """Return tightly cropped garment images keyed by slot.

        If a slot's garment is not detected, it is omitted from the result.
        Left-shoe and Right-shoe masks are merged into a single SHOES crop.
        """
        segments = self._pipe(rgb_img)

        # Collect bboxes per slot (merge Left/Right shoe).
        slot_bboxes: dict[OutfitSlot, tuple[int, int, int, int]] = {}
        for seg in segments:
            label: str = seg["label"]
            slot = _LABEL_TO_SLOT.get(label)
            if slot is None:
                continue
            bbox = _mask_bbox(seg["mask"])
            if bbox is None:
                continue
            if slot in slot_bboxes:
                slot_bboxes[slot] = _union_bbox(slot_bboxes[slot], bbox)
            else:
                slot_bboxes[slot] = bbox

        crops: dict[OutfitSlot, Image.Image] = {}
        for slot, bbox in slot_bboxes.items():
            crop = rgb_img.crop(bbox).convert("RGB")
            if crop.width > 0 and crop.height > 0:
                crops[slot] = crop
        return crops
