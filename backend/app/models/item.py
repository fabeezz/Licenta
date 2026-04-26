from __future__ import annotations

from datetime import datetime

from sqlalchemy import Integer, String, DateTime, ForeignKey, func, text
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.ext.hybrid import hybrid_property
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base


class Item(Base):
    __tablename__ = "items"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    user_id: Mapped[int] = mapped_column(
        Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True
    )

    image_original_name: Mapped[str] = mapped_column(String, nullable=False)
    image_no_bg_name: Mapped[str | None] = mapped_column(String, nullable=True)
    category: Mapped[str | None] = mapped_column(String, nullable=True)
    color_tags: Mapped[dict | None] = mapped_column(JSONB, nullable=True)
    brand: Mapped[str | None] = mapped_column(String, nullable=True)
    material: Mapped[str | None] = mapped_column(String, nullable=True)
    weather: Mapped[list[str]] = mapped_column(JSONB, nullable=False, server_default=text("'[]'::jsonb"))
    occasion: Mapped[str | None] = mapped_column(String, nullable=True)
    wear_count: Mapped[int] = mapped_column(
        Integer, nullable=False, default=0, server_default="0"
    )
    last_worn_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

    user: Mapped["User"] = relationship("User", back_populates="items")

    @hybrid_property
    def dominant_color(self) -> str | None:
        """Return the first dominant color from *color_tags*, or ``None``."""
        if not self.color_tags:
            return None
        dominant = self.color_tags.get("dominant")
        if dominant and isinstance(dominant, list) and dominant:
            return dominant[0]
        return None

    @dominant_color.expression  # type: ignore[no-redef]
    @classmethod
    def dominant_color(cls) -> object:
        """SQL expression for the dominant color; enables server-side filtering."""
        return func.jsonb_extract_path_text(cls.color_tags, "dominant", "0")
