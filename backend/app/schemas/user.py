from datetime import datetime
from pydantic import BaseModel, EmailStr

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

    model_config = {"from_attributes": True}

class TokenOut(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserOut

class PasswordResetRequest(BaseModel):
    username: str
    email: EmailStr
    new_password: str
