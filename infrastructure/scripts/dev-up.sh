#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/../docker/docker-compose.yml"

echo "Starting Nexarats Academy Local Infrastructure..."
docker compose -f "$COMPOSE_FILE" up -d
sleep 3
docker compose -f "$COMPOSE_FILE" ps
echo "Infrastructure started successfully."
