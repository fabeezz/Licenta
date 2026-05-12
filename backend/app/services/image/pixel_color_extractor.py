from __future__ import annotations

from collections import Counter
from typing import Dict, List

from PIL import Image

from app.services.image.color_utils import rgb_to_color_name

_THUMBNAIL_SIZE = (100, 100)
_ALPHA_THRESHOLD = 20       # pixels with alpha <= this are treated as transparent
_ACCENT_MIN_FRACTION = 0.05  # a color must cover at least 5% of visible pixels to be an accent


class PixelColorExtractor:
    def __init__(self, palette_size: int = 5, quality: int = 2) -> None:
        self.palette_size = palette_size
        # quality is unused — pixel counting is instantaneous at thumbnail size

    def extract(self, img: Image.Image) -> Dict[str, List[str]]:
        """Return dominant and accent color names for *img* (RGBA expected)."""
        small = img.copy()
        small.thumbnail(_THUMBNAIL_SIZE, Image.NEAREST)
        if small.mode != "RGBA":
            small = small.convert("RGBA")

        color_counts: Counter[str] = Counter()
        for r, g, b, a in small.getdata():
            if a > _ALPHA_THRESHOLD:
                color_counts[rgb_to_color_name(r, g, b)] += 1

        if not color_counts:
            return {"dominant": ["unknown"], "accent": []}

        most_common = color_counts.most_common(self.palette_size)
        dominant_name = most_common[0][0]
        total = sum(color_counts.values())

        accents = [
            name for name, count in most_common
            if name != dominant_name and count / total > _ACCENT_MIN_FRACTION
        ]
        return {"dominant": [dominant_name], "accent": accents}
