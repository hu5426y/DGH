#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

require_command() {
  local command_name="$1"
  if ! command -v "$command_name" >/dev/null 2>&1; then
    echo "缺少命令: $command_name"
    exit 1
  fi
}

ensure_docker_compose() {
  require_command docker
  if ! docker compose version >/dev/null 2>&1; then
    echo "当前环境不可用 docker compose，请先安装并启动 Docker Desktop / Docker Engine。"
    exit 1
  fi
}

compose() {
  (
    cd "$ROOT_DIR"
    DOCKER_BUILDKIT=0 COMPOSE_DOCKER_CLI_BUILD=0 docker compose "$@"
  )
}
