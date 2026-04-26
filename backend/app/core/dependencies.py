from __future__ import annotations

import httpx
from fastapi import Depends, HTTPException, Request, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.security import decode_access_token
from app.db.session import get_db
from app.models.user import User
from app.repositories.item_repository import ItemRepository
from app.repositories.outfit_repository import OutfitRepository
from app.services.item_service import ItemService
from app.services.outfit_service import OutfitService
from app.services.pipeline import ItemPipeline
from app.services.weather_service import WeatherService

security = HTTPBearer(auto_error=False)


def get_current_user(
    credentials: HTTPAuthorizationCredentials | None = Depends(security),
    db: Session = Depends(get_db),
) -> User:
    if not credentials:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
            headers={"WWW-Authenticate": "Bearer"},
        )
    subject = decode_access_token(credentials.credentials)
    if subject is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token",
            headers={"WWW-Authenticate": "Bearer"},
        )
    user = db.scalars(select(User).where(User.id == int(subject))).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User not found",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return user


def get_pipeline(request: Request) -> ItemPipeline:
    """Return the shared :class:`~app.services.pipeline.ItemPipeline` from app state."""
    return request.app.state.pipeline


def get_item_repository(db: Session = Depends(get_db)) -> ItemRepository:
    """Construct an :class:`~app.repositories.item_repository.ItemRepository` for the request."""
    return ItemRepository(db)


def get_outfit_repository(db: Session = Depends(get_db)) -> OutfitRepository:
    """Construct an :class:`~app.repositories.outfit_repository.OutfitRepository` for the request."""
    return OutfitRepository(db)


def get_item_service(
    repo: ItemRepository = Depends(get_item_repository),
    pipeline: ItemPipeline = Depends(get_pipeline),
) -> ItemService:
    """Construct an :class:`~app.services.item_service.ItemService` for the request."""
    return ItemService(repo, pipeline)


def get_outfit_service(
    repo: OutfitRepository = Depends(get_outfit_repository),
) -> OutfitService:
    """Construct an :class:`~app.services.outfit_service.OutfitService` for the request."""
    return OutfitService(repo)


def get_http_client(request: Request) -> httpx.AsyncClient:
    return request.app.state.http_client


def get_weather_service(
    client: httpx.AsyncClient = Depends(get_http_client),
) -> WeatherService:
    return WeatherService(client, settings.OPEN_METEO_BASE_URL)
