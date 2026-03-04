set -e

PROJECT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/backend"
cd "$PROJECT_DIR"

VENV_DIR="$PROJECT_DIR/venv"

echo "[INFO] Project dir: $PROJECT_DIR"

if [ ! -d "$VENV_DIR" ]; then
  echo "[INFO] No virtualenv found. Creating one in $VENV_DIR ..."
  python3 -m venv "$VENV_DIR"
else
  echo "[INFO] Using existing virtualenv at $VENV_DIR"
fi

source "$VENV_DIR/bin/activate"

if [ -f "requirements.txt" ]; then
  echo "[INFO] Installing/updating dependencies from requirements.txt ..."
  pip install --upgrade pip
  pip install -r requirements.txt
else
  echo "[WARN] requirements.txt not found. Skipping pip install."
fi

echo "[INFO] Starting backend with uvicorn ..."
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
