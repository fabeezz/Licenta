## Flutter frontend (mobile)

This folder contains the Flutter app that talks to the FastAPI backend in `../backend`.

### Run

- **Start backend** (from `backend/`):

```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

- **Run Flutter** (from `frontend/`):

```bash
flutter pub get
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8000
```

Notes:
- On Android emulators, `10.0.2.2` points to the host machine.
- On Waydroid, the host is often reachable at `http://192.168.240.1:8000` (if `10.0.2.2` doesn't work).
- HTTP is enabled via `android:usesCleartextTraffic="true"` (useful for local dev).

