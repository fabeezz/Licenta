from __future__ import annotations

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.core.exceptions import DomainError, InvalidItemsError, NotFoundError, WeatherUnavailableError


def register_exception_handlers(app: FastAPI) -> None:
    """Attach domain-exception → HTTP-response mappings to *app*."""

    @app.exception_handler(NotFoundError)
    async def _not_found(request: Request, exc: NotFoundError) -> JSONResponse:
        return JSONResponse(status_code=404, content={"detail": str(exc)})

    @app.exception_handler(InvalidItemsError)
    async def _invalid_items(request: Request, exc: InvalidItemsError) -> JSONResponse:
        return JSONResponse(status_code=400, content={"detail": str(exc)})

    @app.exception_handler(WeatherUnavailableError)
    async def _weather_unavailable(request: Request, exc: WeatherUnavailableError) -> JSONResponse:
        return JSONResponse(status_code=503, content={"detail": str(exc)})

    @app.exception_handler(DomainError)
    async def _domain_fallback(request: Request, exc: DomainError) -> JSONResponse:
        return JSONResponse(status_code=422, content={"detail": str(exc)})
