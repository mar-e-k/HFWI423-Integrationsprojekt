#!/usr/bin/env bash
set -e

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "Starting Vendix platform..."

if [ ! -f "$BASE_DIR/.env" ]; then
  echo "ERROR: .env file missing at $BASE_DIR/.env"
  exit 1
fi

echo "Ensuring Docker network exists..."

docker network create vendix-network 2>/dev/null || echo "vendix-network network already exists"

echo "Starting core stacks..."
docker compose \
  --project-name vendix \
  --project-directory "$BASE_DIR" \
  --env-file "$BASE_DIR/.env" \
  -f "$BASE_DIR/app/docker-compose.yaml" \
  -f "$BASE_DIR/monitor/docker-compose.yaml" \
  -f "$BASE_DIR/testing/docker-compose.yaml" \
  up -d

# --profile testing \

echo "Core stacks started."