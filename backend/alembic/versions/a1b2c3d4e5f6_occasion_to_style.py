"""occasion to style

Revision ID: a1b2c3d4e5f6
Revises: f3a9b2c1d4e5
Create Date: 2026-04-27 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op

revision: str = "a1b2c3d4e5f6"
down_revision: Union[str, Sequence[str], None] = "f3a9b2c1d4e5"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

_ITEMS_UP = """
ALTER TABLE items ADD COLUMN style jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE items SET style =
    CASE occasion
        WHEN 'casual'     THEN '["casual"]'::jsonb
        WHEN 'formal'     THEN '["formal"]'::jsonb
        WHEN 'sportswear' THEN '["sporty"]'::jsonb
        ELSE                   '[]'::jsonb
    END;
ALTER TABLE items DROP COLUMN occasion;
"""

_OUTFITS_UP = """
ALTER TABLE outfits RENAME COLUMN occasion TO style;
UPDATE outfits SET style = 'sporty' WHERE style = 'sportswear';
"""

_ITEMS_DOWN = """
ALTER TABLE items ADD COLUMN occasion varchar;
UPDATE items SET occasion =
    CASE
        WHEN style @> '["formal"]'  THEN 'formal'
        WHEN style @> '["sporty"]'  THEN 'sportswear'
        WHEN style @> '["casual"]'  THEN 'casual'
        ELSE NULL
    END;
ALTER TABLE items DROP COLUMN style;
"""

_OUTFITS_DOWN = """
UPDATE outfits SET style = 'sportswear' WHERE style = 'sporty';
ALTER TABLE outfits RENAME COLUMN style TO occasion;
"""


def upgrade() -> None:
    op.execute(_ITEMS_UP)
    op.execute(_OUTFITS_UP)


def downgrade() -> None:
    op.execute(_ITEMS_DOWN)
    op.execute(_OUTFITS_DOWN)
