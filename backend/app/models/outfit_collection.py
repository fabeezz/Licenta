from __future__ import annotations

from sqlalchemy import Column, ForeignKey, Integer, String, Table
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base

outfit_collection_outfits = Table(
    "outfit_collection_outfits",
    Base.metadata,
    Column("collection_id", Integer, ForeignKey("outfit_collections.id", ondelete="CASCADE"), primary_key=True),
    Column("outfit_id", Integer, ForeignKey("outfits.id", ondelete="CASCADE"), primary_key=True),
)


class OutfitCollection(Base):
    __tablename__ = "outfit_collections"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    name: Mapped[str] = mapped_column(String, nullable=False)
    user_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)

    user: Mapped["User"] = relationship("User", back_populates="collections")
    outfits: Mapped[list["Outfit"]] = relationship(
        "Outfit",
        secondary=outfit_collection_outfits,
        lazy="selectin",
    )
