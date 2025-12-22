from sqlalchemy import String, Integer, DateTime, JSON
from sqlalchemy.orm import Mapped, mapped_column
from datetime import datetime

from app.db.base import Base

class WardrobeItem(Base):
    __tablename__ = "wardrobe_items"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)

    image_original_name: Mapped[str] = mapped_column(String, nullable=False)
    image_no_bg_name: Mapped[str | None] = mapped_column(String, nullable=True)

    category: Mapped[str | None] = mapped_column(String, nullable=True)
    color_tags: Mapped[dict | None] = mapped_column(JSON, nullable=True)

    # meta noi
    brand: Mapped[str | None] = mapped_column(String, nullable=True)
    material: Mapped[str | None] = mapped_column(String, nullable=True)
    season: Mapped[str | None] = mapped_column(String, nullable=True)
    occasion: Mapped[str | None] = mapped_column(String, nullable=True)

    wear_count: Mapped[int] = mapped_column(
        Integer,
        nullable=False,
        default=0,
        server_default="0"
    )
    last_worn_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)

    created_at: Mapped[datetime] = mapped_column(
        DateTime,
        default=datetime.utcnow,
    )
