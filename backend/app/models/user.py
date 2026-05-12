from datetime import datetime

from sqlalchemy import DateTime, Float, String, Integer
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base

class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    username: Mapped[str] = mapped_column(String(255), unique=True, index=True, nullable=False)
    email: Mapped[str] = mapped_column(String(255), unique=True, index=True, nullable=False)
    hashed_password: Mapped[str] = mapped_column(String(255), nullable=False)

    # Onboarding profile fields (nullable — NULL means not yet onboarded)
    display_name: Mapped[str | None] = mapped_column(String(255), nullable=True)
    gender: Mapped[str | None] = mapped_column(String(20), nullable=True)
    preferred_styles: Mapped[list[str] | None] = mapped_column(JSONB, nullable=True)
    home_location_label: Mapped[str | None] = mapped_column(String(255), nullable=True)
    home_location_lat: Mapped[float | None] = mapped_column(Float, nullable=True)
    home_location_lon: Mapped[float | None] = mapped_column(Float, nullable=True)
    onboarded_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)

    items: Mapped[list["Item"]] = relationship("Item", back_populates="user")  # noqa: F821
    outfits: Mapped[list["Outfit"]] = relationship("Outfit", back_populates="user", cascade="all, delete-orphan")  # noqa: F821
    collections: Mapped[list["OutfitCollection"]] = relationship("OutfitCollection", back_populates="user", cascade="all, delete-orphan")  # noqa: F821
