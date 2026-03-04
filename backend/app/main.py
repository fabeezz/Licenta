import os
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from app.core.config import settings
from app.db.base import Base
from app.db.session import engine
from app.routers import api_router

app = FastAPI(title="Wardrobe API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# asigurăm MEDIA_ROOT
os.makedirs(settings.MEDIA_ROOT, exist_ok=True)

# servim fișierele din MEDIA_ROOT la /media
# ex: http://localhost:8000/media/<filename>
app.mount("/media", StaticFiles(directory=settings.MEDIA_ROOT), name="media")

app.include_router(api_router)
