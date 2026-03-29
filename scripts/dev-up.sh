#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

ensure_docker_compose

compose up -d --build

cat <<EOF

done.

- frontend: http://127.0.0.1:5173
- backend: http://127.0.0.1:3000

next:
  1. open frontend: http://127.0.0.1:5173
  2. check status: ./scripts/dev-status.sh
  3. stop services: ./scripts/dev-down.sh
EOF
