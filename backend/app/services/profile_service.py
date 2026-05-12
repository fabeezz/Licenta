from __future__ import annotations

from datetime import datetime, timezone

from app.models.user import User
from app.repositories.user_repository import UserRepository
from app.schemas.user import OnboardingIn, ProfileUpdate


class ProfileService:
    def __init__(self, repo: UserRepository) -> None:
        self._repo = repo

    def complete_onboarding(self, user: User, payload: OnboardingIn) -> User:
        return self._repo.update_profile(
            user,
            display_name=payload.display_name,
            gender=payload.gender,
            preferred_styles=payload.preferred_styles,
            home_location_label=payload.home_location_label,
            home_location_lat=payload.home_location_lat,
            home_location_lon=payload.home_location_lon,
            onboarded_at=datetime.now(timezone.utc),
        )

    def update_profile(self, user: User, payload: ProfileUpdate) -> User:
        fields = {k: v for k, v in payload.model_dump(exclude_unset=True).items() if v is not None}
        if not fields:
            return user
        return self._repo.update_profile(user, **fields)
