from __future__ import annotations

NEUTRALS = {"black", "white", "gray", "beige", "navy", "brown"}
SOFT_NEUTRALS = {"olive", "burgundy", "dark green"}  # le poți ajusta


def norm_color(c: str | None) -> str | None:
    if not c:
        return None
    x = c.strip().lower()
    if not x or x == "unknown":
        return None
    return x


def is_neutral(c: str | None) -> bool:
    c = norm_color(c)
    return bool(c) and (c in NEUTRALS or c in SOFT_NEUTRALS)


def is_statement(c: str | None) -> bool:
    c = norm_color(c)
    return bool(c) and not is_neutral(c)


def score_outfit(
    *,
    top_color: str | None,
    bottom_color: str | None,   # 60%
    outer_color: str | None,    # 30%
    shoes_color: str | None,    # 10%
) -> float:
    score = 0.0

    # Base stability for 60-30-10 roles
    if is_neutral(bottom_color):
        score += 3.0
    if is_neutral(outer_color):
        score += 2.0
    if is_neutral(shoes_color):
        score += 1.0

    # Allow exactly 1 statement piece in big roles (bottom/outer)
    big_statement = int(is_statement(bottom_color)) + int(is_statement(outer_color))
    if big_statement == 1:
        score += 2.0
        if is_neutral(top_color):
            score += 1.0  # top as "canvas"
    elif big_statement == 2:
        score -= 3.0

    # 10% accent: shoes can be statement, but don't overdo
    if is_statement(shoes_color) and big_statement <= 1:
        score += 1.0

    # Penalize too many different statement colors
    statement_colors = {
        norm_color(top_color),
        norm_color(bottom_color),
        norm_color(outer_color),
        norm_color(shoes_color),
    }
    statement_colors = {c for c in statement_colors if c and is_statement(c)}
    if len(statement_colors) > 2:
        score -= 2.0 * (len(statement_colors) - 2)

    return score
