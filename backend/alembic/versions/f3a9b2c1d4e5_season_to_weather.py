"""season to weather

Revision ID: f3a9b2c1d4e5
Revises: 60dd2477ee4d
Create Date: 2026-04-27 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

revision: str = "f3a9b2c1d4e5"
down_revision: Union[str, Sequence[str], None] = "60dd2477ee4d"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

_ITEMS_UP = """
ALTER TABLE items ADD COLUMN weather jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE items SET weather =
    CASE season
        WHEN 'summer'     THEN '["warm"]'::jsonb
        WHEN 'winter'     THEN '["cold"]'::jsonb
        WHEN 'autumn'     THEN '["cold"]'::jsonb
        WHEN 'spring'     THEN '["warm"]'::jsonb
        WHEN 'all-season' THEN '["all-weather"]'::jsonb
        ELSE                   '["all-weather"]'::jsonb
    END;
ALTER TABLE items DROP COLUMN season;
"""

_OUTFITS_UP = """
ALTER TABLE outfits ADD COLUMN weather jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE outfits SET weather =
    CASE season
        WHEN 'summer'     THEN '["warm"]'::jsonb
        WHEN 'winter'     THEN '["cold"]'::jsonb
        WHEN 'autumn'     THEN '["cold"]'::jsonb
        WHEN 'spring'     THEN '["warm"]'::jsonb
        WHEN 'all-season' THEN '["all-weather"]'::jsonb
        ELSE                   '["all-weather"]'::jsonb
    END;
ALTER TABLE outfits DROP COLUMN season;
"""

_ITEMS_DOWN = """
ALTER TABLE items ADD COLUMN season varchar;
UPDATE items SET season = weather->>0;
ALTER TABLE items DROP COLUMN weather;
"""

_OUTFITS_DOWN = """
ALTER TABLE outfits ADD COLUMN season varchar;
UPDATE outfits SET season = weather->>0;
ALTER TABLE outfits DROP COLUMN weather;
"""


def upgrade() -> None:
    op.execute(_ITEMS_UP)
    op.execute(_OUTFITS_UP)


def downgrade() -> None:
    op.execute(_ITEMS_DOWN)
    op.execute(_OUTFITS_DOWN)
