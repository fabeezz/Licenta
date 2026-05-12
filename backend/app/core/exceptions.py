from __future__ import annotations


class DomainError(Exception):
    """Base class for all domain-layer exceptions."""


class NotFoundError(DomainError):
    """Raised when a requested entity does not exist or is not visible to the caller."""

    def __init__(self, entity: str, entity_id: int | str | None = None) -> None:
        self.entity = entity
        self.entity_id = entity_id
        msg = f"{entity} with id={entity_id} not found" if entity_id is not None else entity
        super().__init__(msg)


class InvalidItemsError(DomainError):
    """Raised when an outfit references items that don't exist or aren't owned by the user."""

    def __init__(self) -> None:
        super().__init__("One or more items are invalid or not owned by user")


class InvalidOutfitsError(DomainError):
    """Raised when a collection references outfits that don't exist or aren't owned by the user."""

    def __init__(self) -> None:
        super().__init__("One or more outfits are invalid or not owned by user")


class WeatherUnavailableError(DomainError):
    """Raised when the external weather provider cannot be reached."""

    def __init__(self, reason: str = "") -> None:
        super().__init__(f"Weather service unavailable{': ' + reason if reason else ''}")
