# app/services/color_utils.py
from colorsys import rgb_to_hls
from typing import Tuple

def rgb_to_hsl_normalized(r: int, g: int, b: int) -> Tuple[float, float, float]:
    """
    Converts RGB [0-255] into normalized H, S, L.
    H: 0-360, S: 0.0-1.0, L: 0.0-1.0
    """
    r_f, g_f, b_f = r / 255.0, g / 255.0, b / 255.0
    h, l, s = rgb_to_hls(r_f, g_f, b_f)
    return h * 360.0, s, l

def rgb_to_color_name(r: int, g: int, b: int) -> str:
    """
    Maps an RGB triplet to a clothing-relevant color name.
    """
    h, s, l = rgb_to_hsl_normalized(r, g, b)
    
    # 1. Absolute Neutrals (Black, White, Gray)
    if l < 0.15:
        return "black"
    if l > 0.75 and s < 0.25:
        return "white"
    if s < 0.15:
        return "gray"

    # 2. Beige (Warm, desaturated, light colors)
    if s < 0.4 and 20 <= h <= 60 and l > 0.5:
        return "beige"

    # 3. Colors by Hue
    if h < 15 or h >= 345:
        if l < 0.4:
            return "burgundy"  # Dark red
        if l > 0.65:           # Light red is Pink!
            return "pink"
        return "red"
        
    if 15 <= h < 45:
        if l < 0.5:
            return "brown"     
        return "orange"
        
    if 45 <= h < 75:
        if l < 0.4:
            return "olive"     
        return "yellow"
        
    if 75 <= h < 165:
        if l < 0.35:
            return "dark green"
        return "green"
        
    if 165 <= h < 210:
        return "cyan"          
        
    if 210 <= h < 260:
        if l < 0.4:
            return "navy"      
        return "blue"
        
    if 260 <= h < 290:
        return "purple"
        
    if 290 <= h < 345:
        return "pink"          # Catches magenta/fuchsia pinks

    return "unknown"
