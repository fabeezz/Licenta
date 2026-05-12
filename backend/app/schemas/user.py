from datetime import datetime

from pydantic import BaseModel, EmailStr, field_validator

_VALID_GENDERS = {"female", "male"}
_VALID_STYLES = {"casual", "formal", "sporty"}


class UserCreate(BaseModel):
    """Body la înregistrare."""
    username: str
    email: EmailStr
    password: str


class UserLogin(BaseModel):
    """Body la login (username sau email + parolă)."""
    username: str
    password: str


class UserOut(BaseModel):
    """Răspuns: user fără parolă."""
    id: int
    username: str
    email: str
    display_name: str | None = None
    gender: str | None = None
    preferred_styles: list[str] | None = None
    home_location_label: str | None = None
    home_location_lat: float | None = None
    home_location_lon: float | None = None
    onboarded_at: datetime | None = None

    model_config = {"from_attributes": True}


class TokenOut(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserOut


class PasswordResetRequest(BaseModel):
    username: str
    email: EmailStr
    new_password: str


class OnboardingIn(BaseModel):
    """Payload for completing the onboarding wizard."""
    display_name: str
    gender: str
    preferred_styles: list[str]
    home_location_label: str
    home_location_lat: float
    home_location_lon: float

    @field_validator("gender")
    @classmethod
    def validate_gender(cls, v: str) -> str:
        if v not in _VALID_GENDERS:
            raise ValueError(f"gender must be one of {_VALID_GENDERS}")
        return v

    @field_validator("preferred_styles")
    @classmethod
    def validate_styles(cls, v: list[str]) -> list[str]:
        invalid = set(v) - _VALID_STYLES
        if invalid:
            raise ValueError(f"invalid styles: {invalid}. Must be subset of {_VALID_STYLES}")
        if not v:
            raise ValueError("preferred_styles must not be empty")
        return v

    @field_validator("display_name")
    @classmethod
    def validate_display_name(cls, v: str) -> str:
        if len(v.strip()) < 1:
            raise ValueError("display_name must not be blank")
        return v.strip()


class ProfileUpdate(BaseModel):
    """Partial update for profile fields (all optional)."""
    display_name: str | None = None
    gender: str | None = None
    preferred_styles: list[str] | None = None
    home_location_label: str | None = None
    home_location_lat: float | None = None
    home_location_lon: float | None = None

    @field_validator("gender")
    @classmethod
    def validate_gender(cls, v: str | None) -> str | None:
        if v is not None and v not in _VALID_GENDERS:
            raise ValueError(f"gender must be one of {_VALID_GENDERS}")
        return v

    @field_validator("preferred_styles")
    @classmethod
    def validate_styles(cls, v: list[str] | None) -> list[str] | None:
        if v is not None:
            invalid = set(v) - _VALID_STYLES
            if invalid:
                raise ValueError(f"invalid styles: {invalid}")
        return v
