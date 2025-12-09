from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    DATABASE_URL: str
    MEDIA_ROOT: str = "media"

    class Config:
        env_file = ".env"

settings = Settings()
