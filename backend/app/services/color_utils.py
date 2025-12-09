from colorsys import rgb_to_hls
from typing import Tuple

def rgb_to_hsl_normalized(r: int, g: int, b: int) -> Tuple[float, float, float]:
    """
    Convertește RGB [0-255] în H, S, L normalizate.
    Folosim hls de la colorsys (H, L, S) dar o tratăm ca HSL.
    """
    r_f, g_f, b_f = r / 255.0, g / 255.0, b / 255.0
    h, l, s = rgb_to_hls(r_f, g_f, b_f)
    return h * 360.0, s, l

def rgb_to_color_name(r: int, g: int, b: int) -> str:
    """
    Mapează un triplet RGB la un nume simplu de culoare.
    Poți ajusta pragurile după ce vezi rezultatele.
    """
    h, s, l = rgb_to_hsl_normalized(r, g, b)
    
    if s < 0.18:
        if l < 0.2:
            return "black"
        if l > 0.8:
            return "white"
        if r > b + 10 and g > b + 10:
            return "beige"
        return "gray"

    if h < 15 or h >= 345:
        return "red"
    if 15 <= h < 45:
        return "orange"
    if 45 <= h < 75:
        return "yellow"
    if 75 <= h < 165:
        if l < 0.4:
            return "olive"
        return "green"
    if 165 <= h < 210:
        return "cyan"
    if 210 <= h < 255:
        return "blue"
    if 255 <= h < 290:
        return "indigo"
    if 290 <= h < 330:
        return "purple"
    return "pink"
