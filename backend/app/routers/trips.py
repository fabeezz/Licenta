from __future__ import annotations

from fastapi import APIRouter, Depends

from app.core.dependencies import get_current_user, get_trip_service
from app.models.user import User
from app.schemas.trip import (
    DestinationOut,
    TripGenerateRequest,
    TripPlanResponse,
    TripRead,
    TripSaveRequest,
)
from app.services.trip_service import TripService

router = APIRouter(prefix="/trips", tags=["trips"])


@router.get("/destinations", response_model=list[DestinationOut])
async def list_destinations(svc: TripService = Depends(get_trip_service)) -> list[DestinationOut]:
    return await svc.list_destinations()


@router.post("/generate", response_model=TripPlanResponse)
async def generate_plan(
    body: TripGenerateRequest,
    user: User = Depends(get_current_user),
    svc: TripService = Depends(get_trip_service),
) -> TripPlanResponse:
    return await svc.generate_plan(user, body)


@router.post("/save", response_model=TripRead)
def save_plan(
    body: TripSaveRequest,
    user: User = Depends(get_current_user),
    svc: TripService = Depends(get_trip_service),
) -> TripRead:
    return svc.save_plan(user, body)
