from collections import Counter
from PIL import Image
from typing import Dict, List
from app.services.color_utils import rgb_to_color_name

class ColorThiefExtractor:
    def __init__(self, palette_size: int = 5, quality: int = 2):
        self.palette_size = palette_size
        # quality is ignored now as this new method is instant

    def extract(self, img: Image.Image) -> Dict[str, List[str]]:
        """
        img: PIL.Image in RGBA format to respect transparent backgrounds.
        """
        # 1. Shrink image drastically so pixel counting is instantaneous
        small_img = img.copy()
        small_img.thumbnail((100, 100), Image.NEAREST)
        
        # 2. Ensure we have an alpha channel
        if small_img.mode != "RGBA":
            small_img = small_img.convert("RGBA")
            
        pixels = small_img.getdata()
        color_counts = Counter()
        
        # 3. Count only visible pixels using your custom color names
        for r, g, b, a in pixels:
            if a > 20:  # Ignore fully transparent or nearly transparent edge pixels
                name = rgb_to_color_name(r, g, b)
                color_counts[name] += 1
                
        # Fallback if image is completely transparent
        if not color_counts:
            return {"dominant": ["unknown"], "accent": []}
            
        # 4. Sort and separate dominant from accents
        most_common = color_counts.most_common(self.palette_size)
        
        dominant_name = most_common[0][0]
        
        accent_names = []
        for name, count in most_common:
            if name != dominant_name and name not in accent_names:
                # Optional MVP rule: only count an accent if it makes up at least ~5% of the clothing
                total_visible_pixels = sum(color_counts.values())
                if count / total_visible_pixels > 0.05: 
                    accent_names.append(name)
        
        return {"dominant": [dominant_name], "accent": accent_names}
