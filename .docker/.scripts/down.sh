#!/usr/bin/env bash
set -e

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "Stopping Vendix platform..."

# -------------------------
# 1. Validate .env (non-fatal)
# -------------------------
if [ ! -f "$BASE_DIR/.env" ]; then
  echo "Warning: .env file missing at $BASE_DIR/.env"
fi

# -------------------------
# 2. Stop testing stack first
# -------------------------
echo "Stopping testing stack..."
docker compose \
  --env-file "$BASE_DIR/.env" \
  -f "$BASE_DIR/testing/docker-compose.yaml" \
  down

# -------------------------
# 3. Stop monitoring stack
# -------------------------
echo "Stopping monitoring stack..."
docker compose \
  --env-file "$BASE_DIR/.env" \
  -f "$BASE_DIR/monitor/docker-compose.yaml" \
  down

# -------------------------
# 4. Stop app stack
# -------------------------
echo "Stopping app stack..."
docker compose \
  --env-file "$BASE_DIR/.env" \
  -f "$BASE_DIR/app/docker-compose.yaml" \
  down

echo "All stacks stopped."