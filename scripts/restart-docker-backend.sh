#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"

usage() {
  cat <<'USAGE'
用法:
  bash scripts/restart-docker-backend.sh restapi
  bash scripts/restart-docker-backend.sh wap
  bash scripts/restart-docker-backend.sh all

说明:
  先在本机 mvn 打 jar,再用本地 Dockerfile.local 重建并重启对应容器。
  不重启 eureka/mysql/redis/nginx/admin-web。
USAGE
}

TARGET="${1:-all}"

case "$TARGET" in
  restapi)
    MAVEN_MODULES="system-restapi"
    COMPOSE_SERVICES=(restapi)
    ;;
  wap)
    MAVEN_MODULES="system-wap"
    COMPOSE_SERVICES=(wap)
    ;;
  all|backend)
    MAVEN_MODULES="system-restapi,system-wap"
    COMPOSE_SERVICES=(restapi wap)
    ;;
  -h|--help|help)
    usage
    exit 0
    ;;
  *)
    usage
    echo
    echo "未知服务: $TARGET" >&2
    exit 1
    ;;
esac

echo "==> 本机打包: $MAVEN_MODULES"
cd "$BACKEND_DIR"
mvn -pl "$MAVEN_MODULES" -am package -DskipTests -B

echo "==> 重建并重启 Docker 服务: ${COMPOSE_SERVICES[*]}"
cd "$PROJECT_ROOT"
docker compose up -d --build --no-deps "${COMPOSE_SERVICES[@]}"

echo "==> 当前状态"
docker compose ps "${COMPOSE_SERVICES[@]}"
