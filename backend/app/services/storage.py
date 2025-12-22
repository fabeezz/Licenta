# app/services/storage.py
import os
from uuid import uuid4
from app.core.config import settings

def ensure_media_dir() -> None:
    os.makedirs(settings.MEDIA_ROOT, exist_ok=True)

def save_upload(file_bytes: bytes, ext: str) -> str:
    ensure_media_dir()
    filename = f"{uuid4().hex}.{ext}"
    path = os.path.join(settings.MEDIA_ROOT, filename)
    with open(path, "wb") as f:
        f.write(file_bytes)
    return filename

def media_path(filename: str) -> str:
    return os.path.join(settings.MEDIA_ROOT, filename)
