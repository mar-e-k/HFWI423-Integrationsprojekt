#!/usr/bin/env bash
set -e

# 1. Path Setup
DOT_DOCKER_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT_ROOT="$(cd "$DOT_DOCKER_DIR/.." && pwd)"
EXPORT_PATH="$PROJECT_ROOT/.docker/app/keycloak/vendix-realm.json"
TEMP_NAME="kc-export-$(date +%s)"

# 2. Load Environment (WSL Safe)
if [ -f "$DOT_DOCKER_DIR/.env" ]; then
    set -a
    source <(tr -d '\r' < "$DOT_DOCKER_DIR/.env")
    set +a
fi

echo "--- Exporting Realm: vendix ---"

# 3. Export & Extract
# We run the export internally, then sudo cp to force the write to /mnt/c/
docker run --name "$TEMP_NAME" \
  --network vendix-network \
  -e KC_DB=postgres \
  -e KC_DB_URL="jdbc:postgresql://postgres:5432/${POSTGRES_DB}" \
  -e KC_DB_USERNAME="${POSTGRES_USER}" \
  -e KC_DB_PASSWORD="${POSTGRES_PASSWORD}" \
  keycloak/keycloak:26.6 \
  export --file /tmp/export.json --realm vendix --users same_file

sudo mkdir -p "$(dirname "$EXPORT_PATH")"
sudo docker cp "$TEMP_NAME:/tmp/export.json" "$EXPORT_PATH"
docker rm "$TEMP_NAME"

echo "--- Success: $EXPORT_PATH ---"