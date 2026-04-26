from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    DATABASE_URL: str
    MEDIA_ROOT: str = "media"

    # JWT
    SECRET_KEY: str
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7

    OPEN_METEO_BASE_URL: str = "https://api.open-meteo.com/v1/forecast"

    class Config:
        env_file = ".env"

settings = Settings()
