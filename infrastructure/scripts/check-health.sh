#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/../docker/docker-compose.yml"

echo "Checking Nexarats Academy Local Services..."

if command -v docker &> /dev/null; then
    echo -e "\n[Docker Containers]"
    docker compose -f "$COMPOSE_FILE" ps
else
    echo -e "\nDocker not found in PATH."
fi

PORT="${SERVER_PORT:-8085}"

echo -e "\n[Backend Health (/api/v1/health on :$PORT)]"
curl -sSf "http://localhost:$PORT/api/v1/health" || echo "Backend unreachable on :$PORT"

echo -e "\n\n[Actuator Health (/actuator/health on :$PORT)]"
curl -sSf "http://localhost:$PORT/actuator/health" || echo "Actuator unreachable on :$PORT"
echo ""
