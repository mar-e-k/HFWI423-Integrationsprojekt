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
# 2. Ensure network exists
# -------------------------
echo "Ensuring Docker network exists..."

docker network create vendix-network 2>/dev/null || echo "vendix-network network already exists"

# -------------------------
# 3. Start all stacks
# -------------------------
echo "Starting core stacks..."
docker compose \
  --project-name vendix \
  --env-file "$BASE_DIR/.env" \
  -f "$BASE_DIR/app/docker-compose.yaml" \
  -f "$BASE_DIR/monitor/docker-compose.yaml" \
  -f "$BASE_DIR/testing/docker-compose.yaml" \
  up -d

echo "Core stacks started."
echo "To start the optional testing stack, run 'docker compose --project-name vendix --profile testing up -d'"