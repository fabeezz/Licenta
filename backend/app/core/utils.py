from __future__ import annotations


def parse_csv_tags(value: str | None) -> list[str]:
    """Split a comma-separated tag string into a lowercased, stripped list."""
    if not value:
        return []
    return [t.strip().lower() for t in value.split(",") if t.strip()]
