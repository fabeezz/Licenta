# app/services/preprocess.py
from dataclasses import dataclass
from PIL import Image
from app.services.storage import media_path

@dataclass(frozen=True)
class ImagePrepConfig:
    max_size: int = 400          # resize for speed
    crop_to_alpha: bool = True   # crop bounding box where alpha>0

def load_pil(filename: str) -> Image.Image:
    return Image.open(media_path(filename))

def prepare_rgb(
    img: Image.Image,
    cfg: ImagePrepConfig = ImagePrepConfig()
) -> Image.Image:
    # Crop to visible area if RGBA + requested
    if cfg.crop_to_alpha and img.mode == "RGBA":
        alpha = img.split()[-1]
        bbox = alpha.getbbox()
        if bbox:
            img = img.crop(bbox)

    # Resize (thumbnail keeps aspect ratio)
    img = img.copy()
    img.thumbnail((cfg.max_size, cfg.max_size), Image.LANCZOS)

    # Ensure RGB
    if img.mode != "RGB":
        img = img.convert("RGB")

    return img

def load_prepared_rgb(filename: str, cfg: ImagePrepConfig = ImagePrepConfig()) -> Image.Image:
    img = load_pil(filename)
    return prepare_rgb(img, cfg)
