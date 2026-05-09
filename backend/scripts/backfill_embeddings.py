"""Backfill CLIP embeddings for items that have none.

Run from the backend/ directory after applying the migration:

    alembic upgrade head
    python -m scripts.backfill_embeddings

The script loads the same CLIP model used at upload time and writes the
embedding bytes to each item row that currently has embedding IS NULL.
Items are processed in batches of 50 to bound memory usage.

SegFormer is NOT used here — wardrobe items are already background-removed,
so embedding the no-bg image directly matches the production upload path.
"""
from __future__ import annotations

import logging
import os
import sys

import numpy as np
from PIL import Image
from sqlalchemy import select

logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
logger = logging.getLogger(__name__)

# Ensure the app package is importable.
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))

from app.core.config import settings
from app.db.session import SessionLocal
# Import all models so SQLAlchemy can resolve every relationship before the
# mapper is configured (User → Outfit, Item → User, etc.).
import app.models.user  # noqa: F401
import app.models.outfit  # noqa: F401
import app.models.outfit_collection  # noqa: F401
import app.models.trip  # noqa: F401
from app.models.item import Item
from app.services.ai.clip_attribute_classifier import ClipAttributeClassifier
from app.services.ai.config import CLIP_MODEL_ID
from app.services.image.preprocess import ImagePrepConfig, load_prepared_rgb

BATCH_SIZE = 50


def main() -> None:
    logger.info("Loading CLIP model %s …", CLIP_MODEL_ID)
    classifier = ClipAttributeClassifier(CLIP_MODEL_ID)
    prep_cfg = ImagePrepConfig(max_size=400, crop_to_alpha=True)

    db = SessionLocal()
    try:
        stmt = select(Item).where(Item.embedding.is_(None))
        items = list(db.scalars(stmt).all())
        logger.info("Found %d items without embeddings", len(items))

        processed = 0
        for i in range(0, len(items), BATCH_SIZE):
            batch = items[i : i + BATCH_SIZE]
            for item in batch:
                if not item.image_no_bg_name:
                    logger.warning("Item %d has no no-bg image, skipping", item.id)
                    continue
                img_path = os.path.join(settings.MEDIA_ROOT, item.image_no_bg_name)
                if not os.path.exists(img_path):
                    logger.warning("Image file not found for item %d: %s", item.id, img_path)
                    continue
                try:
                    rgb = load_prepared_rgb(item.image_no_bg_name, prep_cfg)
                    embed = classifier.encode_image(rgb)
                    item.embedding = embed.cpu().numpy().astype(np.float32).tobytes()
                    processed += 1
                except Exception as exc:
                    logger.error("Failed to embed item %d: %s", item.id, exc)

            db.commit()
            logger.info("Committed batch %d–%d (%d/%d done)", i + 1, i + len(batch), processed, len(items))

    finally:
        db.close()

    logger.info("Done. Embedded %d items.", processed)


if __name__ == "__main__":
    main()
