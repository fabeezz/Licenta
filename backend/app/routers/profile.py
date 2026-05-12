from __future__ import annotations

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user, get_profile_service
from app.db.session import get_db
from app.models.user import User
from app.schemas.user import OnboardingIn, ProfileUpdate, UserOut
from app.services.profile_service import ProfileService

router = APIRouter(prefix="/profile", tags=["profile"])


@router.post("/onboarding", response_model=UserOut)
def complete_onboarding(
    payload: OnboardingIn,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: ProfileService = Depends(get_profile_service),
):
    """Complete the onboarding wizard and persist profile data."""
    user = svc.complete_onboarding(current_user, payload)
    db.commit()
    db.refresh(user)
    return UserOut.model_validate(user)


@router.patch("", response_model=UserOut)
def update_profile(
    payload: ProfileUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    svc: ProfileService = Depends(get_profile_service),
):
    """Partial update of profile fields (used from the Profile screen)."""
    user = svc.update_profile(current_user, payload)
    db.commit()
    db.refresh(user)
    return UserOut.model_validate(user)
