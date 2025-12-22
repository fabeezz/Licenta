import os
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from app.core.config import settings
from app.db.base import Base
from app.db.session import engine
from app.api.wardrobe import router as wardrobe_router

app = FastAPI(title="Wardrobe API")

# CORS – pentru Next.js (sau orice alt frontend) pe alt port
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],   # în dev e ok să fie permissive
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# asigurăm MEDIA_ROOT
os.makedirs(settings.MEDIA_ROOT, exist_ok=True)

# servim fișierele din MEDIA_ROOT la /media
# ex: http://localhost:8000/media/<filename>
app.mount("/media", StaticFiles(directory=settings.MEDIA_ROOT), name="media")

# creăm tabelele pe baza modelelor ORM (MVP; poți trece la Alembic ulterior)
# Base.metadata.create_all(bind=engine)

# înregistrăm routerul pentru garderobă
app.include_router(wardrobe_router)
