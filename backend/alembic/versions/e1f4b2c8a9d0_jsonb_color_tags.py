"""jsonb color tags

Revision ID: e1f4b2c8a9d0
Revises: adbedd8d6352
Create Date: 2026-04-25 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

revision: str = "e1f4b2c8a9d0"
down_revision: Union[str, Sequence[str], None] = "67b1b99a6a1c"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.alter_column(
        "items",
        "color_tags",
        type_=postgresql.JSONB(astext_type=sa.Text()),
        existing_type=sa.JSON(),
        postgresql_using="color_tags::jsonb",
    )
    op.execute(
        "CREATE INDEX IF NOT EXISTS ix_items_dominant_color "
        "ON items ((color_tags->'dominant'->>0));"
    )


def downgrade() -> None:
    op.execute("DROP INDEX IF EXISTS ix_items_dominant_color;")
    op.alter_column(
        "items",
        "color_tags",
        type_=sa.JSON(),
        existing_type=postgresql.JSONB(astext_type=sa.Text()),
        postgresql_using="color_tags::json",
    )
