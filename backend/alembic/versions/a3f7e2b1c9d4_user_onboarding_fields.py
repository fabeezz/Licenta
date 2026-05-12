"""user onboarding fields

Revision ID: a3f7e2b1c9d4
Revises: 5f01ad15974e
Create Date: 2026-05-12 00:00:00.000000

"""
from typing import Sequence, Union

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects import postgresql

revision: str = "a3f7e2b1c9d4"
down_revision: Union[str, Sequence[str], None] = "5f01ad15974e"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.add_column("users", sa.Column("display_name", sa.String(255), nullable=True))
    op.add_column("users", sa.Column("gender", sa.String(20), nullable=True))
    op.add_column("users", sa.Column("preferred_styles", postgresql.JSONB(astext_type=sa.Text()), nullable=True))
    op.add_column("users", sa.Column("home_location_label", sa.String(255), nullable=True))
    op.add_column("users", sa.Column("home_location_lat", sa.Float(), nullable=True))
    op.add_column("users", sa.Column("home_location_lon", sa.Float(), nullable=True))
    op.add_column("users", sa.Column("onboarded_at", sa.DateTime(timezone=True), nullable=True))


def downgrade() -> None:
    op.drop_column("users", "onboarded_at")
    op.drop_column("users", "home_location_lon")
    op.drop_column("users", "home_location_lat")
    op.drop_column("users", "home_location_label")
    op.drop_column("users", "preferred_styles")
    op.drop_column("users", "gender")
    op.drop_column("users", "display_name")
