from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    DATABASE_URL: str
    MEDIA_ROOT: str = "media"

    # JWT
    SECRET_KEY: str
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7

    class Config:
        env_file = ".env"

settings = Settings()
