from fastapi import APIRouter

from app.routers.auth import router as auth_router
from app.routers.items import router as items_router
from app.routers.outfits import router as outfits_router
from app.routers.weather import router as weather_router

api_router = APIRouter()
api_router.include_router(auth_router)
api_router.include_router(items_router)
api_router.include_router(outfits_router)
api_router.include_router(weather_router)
