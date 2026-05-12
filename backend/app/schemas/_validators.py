from __future__ import annotations


def lowercase_str(v: str | None) -> str | None:
    return v.lower() if isinstance(v, str) else v


def lowercase_str_list(v: list[str] | None) -> list[str] | None:
    if isinstance(v, list):
        return [t.lower() if isinstance(t, str) else t for t in v]
    return v
