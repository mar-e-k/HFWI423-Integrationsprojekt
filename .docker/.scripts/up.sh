#!/usr/bin/env bash
set -e

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
START_APPS=false
START_AUTH=false

for arg in "$@"; do
  case "$arg" in
    --apps)
      START_APPS=true
      ;;
    --auth)
      START_AUTH=true
      ;;
    *)
      echo "ERROR: Unknown option '$arg'. Supported: --apps, --auth"
      exit 1
      ;;
  esac
done

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
APP_PROFILE_ARGS=()
APP_UP_ARGS=(up -d)

if [ "$START_APPS" = true ]; then
  APP_PROFILE_ARGS+=(--profile apps)
  APP_UP_ARGS+=(--build)
  export K6_ORCHESTRATOR_URL="http://orchestrator:8080"
  export K6_STORE_URL="http://store:8081"
  echo "App services enabled. k6 will target Docker service names."
else
  export K6_ORCHESTRATOR_URL="http://host.docker.internal:8080"
  export K6_STORE_URL="http://host.docker.internal:8081"
  echo "App services disabled. k6 will target locally running apps via host.docker.internal."
fi

if [ "$START_AUTH" = true ]; then
  APP_PROFILE_ARGS+=(--profile auth)
fi

docker compose \
  --env-file "$BASE_DIR/.env" \
  -f "$BASE_DIR/app/docker-compose.yaml" \
  "${APP_PROFILE_ARGS[@]}" \
  "${APP_UP_ARGS[@]}"

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
TESTING_UP_ARGS=(up -d)
if [ "$START_APPS" = true ]; then
  TESTING_UP_ARGS+=(--force-recreate k6)
fi

docker compose \
  --env-file "$BASE_DIR/.env" \
  -f "$BASE_DIR/testing/docker-compose.yaml" \
  "${TESTING_UP_ARGS[@]}"

echo "All stacks started."
