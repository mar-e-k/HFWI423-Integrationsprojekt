#!/usr/bin/env bash
set -e

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "Stopping Vendix platform..."

# -------------------------
# 1. Validate docker.env (non-fatal)
# -------------------------
if [ ! -f "$BASE_DIR/.env" ]; then
  echo "Warning: .env file missing at $BASE_DIR/.env"
fi

# -------------------------
# 2. Stop all stacks
# -------------------------
echo "Stopping all stacks..."
docker compose \
  --project-name vendix \
  --project-directory "$BASE_DIR" \
  --env-file "$BASE_DIR/.env" \
  -f "$BASE_DIR/app/docker-compose.yaml" \
  -f "$BASE_DIR/monitor/docker-compose.yaml" \
  -f "$BASE_DIR/testing/docker-compose.yaml" \
  down -v

echo "All stacks stopped."