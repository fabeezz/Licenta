# app/services/bg_removal.py
from uuid import uuid4
from PIL import Image
from rembg import remove
from app.services.storage import ensure_media_dir, media_path

def remove_background(filename: str) -> str:
    ensure_media_dir()
    input_path = media_path(filename)

    img = Image.open(input_path).convert("RGBA")
    out = remove(img)

    out_name = f"{uuid4().hex}_nobg.png"
    out.save(media_path(out_name))
    return out_name
