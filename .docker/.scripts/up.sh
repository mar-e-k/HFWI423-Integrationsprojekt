#!/usr/bin/env bash
set -e

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "Starting Vendix platform..."

# -------------------------
# 1. Validate .env
# -------------------------
if [ ! -f "$BASE_DIR/.env" ]; then
  echo "ERROR: .env file missing at $BASE_DIR/.env"
  exit 1
fi

# -------------------------
# 2. Ensure networks exist
# -------------------------
echo "Ensuring Docker networks exist..."

docker network create app 2>/dev/null || echo "app network already exists"
docker network create monitor-net 2>/dev/null || echo "monitor-net network already exists"

# -------------------------
# 3. Start app stack
# -------------------------
echo "Starting app stack..."
docker compose \
  --env-file "$BASE_DIR/.env" \
  -f "$BASE_DIR/app/docker-compose.yaml" \
  up -d

# -------------------------
# 4. Start monitoring stack
# -------------------------
echo "Starting monitoring stack..."
docker compose \
  --env-file "$BASE_DIR/.env" \
  -f "$BASE_DIR/monitor/docker-compose.yaml" \
  up -d

# -------------------------
# 5. Start testing stack
# -------------------------
echo "Starting testing stack..."
docker compose \
  --env-file "$BASE_DIR/.env" \
  -f "$BASE_DIR/testing/docker-compose.yaml" \
  up -d

echo "All stacks started."