from dataclasses import dataclass
from PIL import Image
from app.services.storage import media_path

@dataclass(frozen=True)
class ImagePrepConfig:
    max_size: int = 400          
    crop_to_alpha: bool = True   

def load_pil(filename: str) -> Image.Image:
    return Image.open(media_path(filename))

def prepare_rgba(
    img: Image.Image,
    cfg: ImagePrepConfig = ImagePrepConfig()
) -> Image.Image:
    
    # Ensure RGBA so we have an alpha channel for transparency
    if img.mode != "RGBA":
        img = img.convert("RGBA")

    # Crop to visible area
    if cfg.crop_to_alpha:
        alpha = img.split()[-1]
        bbox = alpha.getbbox()
        if bbox:
            img = img.crop(bbox)

    # Resize 
    img = img.copy()
    img.thumbnail((cfg.max_size, cfg.max_size), Image.LANCZOS)

    return img

def load_prepared_rgb(filename: str, cfg: ImagePrepConfig = ImagePrepConfig()) -> Image.Image:
    img = load_pil(filename)
    return prepare_rgba(img, cfg) # Returns RGBA now
