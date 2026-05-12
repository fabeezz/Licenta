from __future__ import annotations

import io
import os
from contextlib import asynccontextmanager
from typing import AsyncGenerator

import httpx
import rembg
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from PIL import Image

from app.core.config import settings
from app.core.exception_handlers import register_exception_handlers
from app.core.logging import configure_logging
from app.routers import api_router
from app.services.ai.category_classifier import ClipCategoryClassifier
from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier
from app.services.ai.config import CATEGORIES_EN, CLIP_MODEL_ID
from app.services.ai.material_classifier import ClipMaterialClassifier
from app.services.ai.style_classifier import ClipStyleClassifier
from app.services.image.pixel_color_extractor import PixelColorExtractor
from app.services.inspiration.search import prewarm_segmenter
from app.services.pipeline import ItemPipeline


def _make_dummy_png() -> bytes:
    buf = io.BytesIO()
    Image.new("RGBA", (16, 16), (255, 255, 255, 255)).save(buf, format="PNG")
    return buf.getvalue()


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None, None]:
    configure_logging()
    import logging as _logging
    _log = _logging.getLogger(__name__)

    base = ClipAttributeClassifier(CLIP_MODEL_ID)
    cat_clf = ClipCategoryClassifier(base)
    mat_clf = ClipMaterialClassifier(base)
    sty_clf = ClipStyleClassifier(base)

    _log.info("Prewarming label embeddings...")
    cat_clf.prewarm(CATEGORIES_EN)
    mat_clf.prewarm()
    sty_clf.prewarm()
    _log.info("Label embeddings cached and ready")

    app.state.pipeline = ItemPipeline(
        color_extractor=PixelColorExtractor(palette_size=5),
        base_classifier=base,
        category_classifier=cat_clf,
        material_classifier=mat_clf,
        style_classifier=sty_clf,
        categories_en=CATEGORIES_EN,
    )
    _log.info("CLIP pipeline loaded and ready")

    try:
        rembg.remove(_make_dummy_png())
        _log.info("rembg warmup complete")
    except Exception:
        _log.warning("rembg warmup failed (non-fatal)")

    try:
        prewarm_segmenter()
        _log.info("SegFormer warmup complete")
    except Exception:
        _log.warning("SegFormer warmup failed (non-fatal)")

    async with httpx.AsyncClient() as client:
        app.state.http_client = client
        yield


app = FastAPI(title="Wardrobe API", lifespan=lifespan)

register_exception_handlers(app)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ALLOW_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

os.makedirs(settings.MEDIA_ROOT, exist_ok=True)
app.mount("/media", StaticFiles(directory=settings.MEDIA_ROOT), name="media")

app.include_router(api_router)
