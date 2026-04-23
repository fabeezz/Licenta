# app/services/bg_removal.py
from uuid import uuid4
from PIL import Image
from rembg import remove
from app.services.storage import ensure_media_dir, media_path

def remove_background(filename: str) -> str:
    ensure_media_dir()
    input_path = media_path(filename)

    img = Image.open(input_path).convert("RGBA")
    # 1. Remove background
    out = remove(img)

    # 2. Crop to visible content (alpha channel)
    # This ensures the clothing item takes up as much space as possible in the UI.
    alpha = out.split()[-1]
    bbox = alpha.getbbox()
    if bbox:
        out = out.crop(bbox)
        
        # 3. Add small margin (5%) so it doesn't touch the borders tightly
        w, h = out.size
        margin_w = int(w * 0.05)
        margin_h = int(h * 0.05)
        new_w = w + (margin_w * 2)
        new_h = h + (margin_h * 2)
        
        final = Image.new("RGBA", (new_w, new_h), (0, 0, 0, 0))
        final.paste(out, (margin_w, margin_h))
        out = final

    out_name = f"{uuid4().hex}_nobg.png"
    out.save(media_path(out_name))
    return out_name
