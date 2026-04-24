from __future__ import annotations

import logging
import time
from contextlib import contextmanager
from typing import Generator


def configure_logging(level: int = logging.INFO) -> None:
    """Configure root logger with a structured, human-readable format."""
    logging.basicConfig(
        level=level,
        format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
        datefmt="%Y-%m-%dT%H:%M:%S",
    )


@contextmanager
def log_latency(name: str, logger: logging.Logger | None = None) -> Generator[None, None, None]:
    """Context manager that logs elapsed time for the enclosed block.

    Args:
        name: Label used in the log message.
        logger: Logger instance; falls back to the root logger.
    """
    _log = logger or logging.getLogger(__name__)
    start = time.perf_counter()
    try:
        yield
    finally:
        elapsed_ms = (time.perf_counter() - start) * 1000
        _log.debug("%s completed in %.1f ms", name, elapsed_ms)
