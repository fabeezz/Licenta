import bcrypt
from datetime import datetime, timedelta

from jose import JWTError, jwt

from app.core.config import settings

def _prepare_password(password: str) -> bytes:
    """Trunchiază la 72 octeți (limita bcrypt)."""
    return password.encode("utf-8")[:72]

def hash_password(password: str) -> str:
    return bcrypt.hashpw(
        _prepare_password(password),
        bcrypt.gensalt(),
    ).decode("utf-8")

def verify_password(plain: str, hashed: str) -> bool:
    return bcrypt.checkpw(
        _prepare_password(plain),
        hashed.encode("utf-8"),
    )

def create_access_token(subject: str | int) -> str:
    expire = datetime.utcnow() + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode = {"sub": str(subject), "exp": expire}
    return jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)

def decode_access_token(token: str) -> str | None:
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
        sub = payload.get("sub")
        return sub
    except JWTError:
        return None
