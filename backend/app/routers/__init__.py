from fastapi import APIRouter

from app.routers.auth import router as auth_router
from app.routers.items import router as items_router
from app.routers.wardrobes import router as wardrobes_router
from app.routers.outfits import router as outfits_router

api_router = APIRouter()
api_router.include_router(auth_router)
api_router.include_router(items_router)
api_router.include_router(wardrobes_router)
api_router.include_router(outfits_router)