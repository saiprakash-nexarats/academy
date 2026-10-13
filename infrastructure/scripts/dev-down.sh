#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/../docker/docker-compose.yml"

echo "Stopping Nexarats Academy Local Infrastructure..."
docker compose -f "$COMPOSE_FILE" down
echo "Infrastructure stopped."
