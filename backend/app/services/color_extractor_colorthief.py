# app/services/color_extractor_colorthief.py
from io import BytesIO
from typing import Dict, List, Tuple
from colorthief import ColorThief

from app.services.color_utils import rgb_to_color_name

class ColorThiefExtractor:
    def __init__(self, palette_size: int = 5, quality: int = 2):
        self.palette_size = palette_size
        self.quality = quality

    def _to_buffer(self, rgb_img) -> BytesIO:
        buf = BytesIO()
        rgb_img.save(buf, format="PNG")
        buf.seek(0)
        return buf

    def extract(self, rgb_img) -> Dict[str, List[str]]:
        """
        rgb_img: PIL.Image in RGB, already cropped/resized.
        """
        buf = self._to_buffer(rgb_img)
        ct = ColorThief(buf)

        dom_rgb: Tuple[int, int, int] = ct.get_color(quality=self.quality)
        raw_palette: List[Tuple[int, int, int]] = ct.get_palette(
            color_count=self.palette_size,
            quality=self.quality
        )

        dominant_name = rgb_to_color_name(*dom_rgb)

        accent_names: List[str] = []
        for rgb in raw_palette:
            name = rgb_to_color_name(*rgb)
            if name != dominant_name and name not in accent_names:
                accent_names.append(name)

        return {"dominant": [dominant_name], "accent": accent_names}
