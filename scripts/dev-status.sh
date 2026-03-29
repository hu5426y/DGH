#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

ensure_docker_compose

echo "[docker]"
compose ps

echo
echo "[urls]"
echo "frontend: http://127.0.0.1:5173"
echo "backend:  http://127.0.0.1:3000"
