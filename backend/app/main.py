from __future__ import annotations

import os
from contextlib import asynccontextmanager
from typing import AsyncGenerator

import httpx
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from app.core.config import settings
from app.core.exception_handlers import register_exception_handlers
from app.core.logging import configure_logging
from app.db.base import Base
from app.db.session import engine
from app.routers import api_router
from app.services.ai.category_classifier import ClipCategoryClassifier
from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier
from app.services.ai.config import CATEGORIES_EN, CLIP_MODEL_ID
from app.services.ai.material_classifier import ClipMaterialClassifier
from app.services.ai.style_classifier import ClipStyleClassifier
from app.services.image.color_extractor_colorthief import ColorThiefExtractor
from app.services.pipeline import ItemPipeline


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None, None]:
    configure_logging()
    base = ClipAttributeClassifier(CLIP_MODEL_ID)
    app.state.pipeline = ItemPipeline(
        color_extractor=ColorThiefExtractor(palette_size=5, quality=2),
        base_classifier=base,
        category_classifier=ClipCategoryClassifier(base),
        material_classifier=ClipMaterialClassifier(base),
        style_classifier=ClipStyleClassifier(base),
        categories_en=CATEGORIES_EN,
    )
    import logging
    logging.getLogger(__name__).info("CLIP pipeline loaded and ready")
    async with httpx.AsyncClient() as client:
        app.state.http_client = client
        yield


app = FastAPI(title="Wardrobe API", lifespan=lifespan)

register_exception_handlers(app)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

os.makedirs(settings.MEDIA_ROOT, exist_ok=True)
app.mount("/media", StaticFiles(directory=settings.MEDIA_ROOT), name="media")

app.include_router(api_router)
