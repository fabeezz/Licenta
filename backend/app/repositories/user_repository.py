from __future__ import annotations

from sqlalchemy.orm import Session

from app.models.user import User


class UserRepository:
    def __init__(self, db: Session) -> None:
        self._db = db

    def update_profile(self, user: User, **fields: object) -> User:
        for key, value in fields.items():
            setattr(user, key, value)
        self._db.flush()
        self._db.refresh(user)
        return user
