from sqlalchemy import Boolean, String, Integer, ForeignKey, text
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base


class Outfit(Base):
    __tablename__ = "outfits"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    name: Mapped[str] = mapped_column(String, nullable=False)
    weather: Mapped[list[str]] = mapped_column(JSONB, nullable=False, server_default=text("'[]'::jsonb"))
    style: Mapped[str | None] = mapped_column(String, nullable=True)
    is_trip: Mapped[bool] = mapped_column(Boolean, nullable=False, server_default=text("false"))

    user_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    shoe_id: Mapped[int] = mapped_column(Integer, ForeignKey("items.id", ondelete="CASCADE"), nullable=False)
    bottom_id: Mapped[int] = mapped_column(Integer, ForeignKey("items.id", ondelete="CASCADE"), nullable=False)
    top_id: Mapped[int] = mapped_column(Integer, ForeignKey("items.id", ondelete="CASCADE"), nullable=False)
    outer_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("items.id", ondelete="SET NULL"), nullable=True)

    user: Mapped["User"] = relationship("User", back_populates="outfits")
    shoe: Mapped["Item"] = relationship("Item", foreign_keys=[shoe_id])
    bottom: Mapped["Item"] = relationship("Item", foreign_keys=[bottom_id])
    top: Mapped["Item"] = relationship("Item", foreign_keys=[top_id])
    outer: Mapped["Item | None"] = relationship("Item", foreign_keys=[outer_id])
