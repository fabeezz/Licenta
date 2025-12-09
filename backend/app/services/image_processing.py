import os
from io import BytesIO
from uuid import uuid4
from typing import Dict, List, Tuple

from PIL import Image
from rembg import remove
from colorthief import ColorThief

from app.core.config import settings
from app.services.color_utils import rgb_to_color_name

def ensure_media_dir() -> None:
    os.makedirs(settings.MEDIA_ROOT, exist_ok=True)

def save_upload(file_bytes: bytes, ext: str) -> str:
    ensure_media_dir()
    filename = f"{uuid4().hex}.{ext}"
    path = os.path.join(settings.MEDIA_ROOT, filename)
    with open(path, "wb") as f:
        f.write(file_bytes)
    return filename

def remove_background(filename: str) -> str:
    ensure_media_dir()
    input_path = os.path.join(settings.MEDIA_ROOT, filename)
    img = Image.open(input_path).convert("RGBA")
    out = remove(img)

    out_name = f"{uuid4().hex}_nobg.png"
    out_path = os.path.join(settings.MEDIA_ROOT, out_name)
    out.save(out_path)
    return out_name


def _prepare_image_for_colorthief(filename: str) -> BytesIO:
    """
    Încarcă imaginea din MEDIA_ROOT, taie fundalul transparent,
    o redimensionează la max 400x400 pt viteză și o întoarce
    ca un buffer in-memory pentru ColorThief.
    """
    path = os.path.join(settings.MEDIA_ROOT, filename)
    img = Image.open(path)

    # Dacă are alpha, facem crop pe bounding-box-ul pixelilor vizibili
    if img.mode == "RGBA":
        alpha = img.split()[-1]
        bbox = alpha.getbbox()
        if bbox:
            img = img.crop(bbox)

    # Redimensionăm la ceva rezonabil ca să nu fie prea mare
    img.thumbnail((400, 400), Image.LANCZOS)

    # ColorThief merge pe RGB
    rgb_img = img.convert("RGB")

    buffer = BytesIO()
    rgb_img.save(buffer, format="PNG")
    buffer.seek(0)
    return buffer


def extract_colors_colorthief(filename: str, palette_size: int = 5) -> Dict[str, List[str]]:
    """
    Folosește ColorThief pentru a extrage o paletă de culori
    și mapează rezultatul la nume simple (black, green, red etc.).
    """
    buffer = _prepare_image_for_colorthief(filename)
    ct = ColorThief(buffer)

    # culoarea dominantă (RGB)
    dom_rgb: Tuple[int, int, int] = ct.get_color(quality=2)

    # paletă de culori
    raw_palette: List[Tuple[int, int, int]] = ct.get_palette(color_count=palette_size, quality=2)

    # mapăm la nume
    dominant_name = rgb_to_color_name(*dom_rgb)

    accent_names: List[str] = []
    for rgb in raw_palette:
        name = rgb_to_color_name(*rgb)
        # scoatem duplicările și nu repetăm culoarea dominantă
        if name != dominant_name and name not in accent_names:
            accent_names.append(name)

    return {
        "dominant": [dominant_name],
        "accent": accent_names,
    }

def classify_category_stub(_filename: str) -> str:
    """
    Deocamdată: întoarce 'unknown'.
    ulterior: model de clasificare (timm, torchvision, etc.).
    """
    return "unknown"
