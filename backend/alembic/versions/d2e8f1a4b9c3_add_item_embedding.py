"""add item embedding

Revision ID: d2e8f1a4b9c3
Revises: b1c2d3e4f5a6
Create Date: 2026-05-09 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = "d2e8f1a4b9c3"
down_revision: Union[str, Sequence[str], None] = "b1c2d3e4f5a6"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.add_column("items", sa.Column("embedding", sa.LargeBinary(), nullable=True))


def downgrade() -> None:
    op.drop_column("items", "embedding")
