#!/usr/bin/env bash
set -euo pipefail

# YPAT staging 回滚脚本
#
# 用法:
#   ./rollback-staging.sh                # 回滚到 previous
#   ./rollback-staging.sh --list         # 列出可用 release
#   ./rollback-staging.sh --to <sha>     # 回滚到指定 SHA
#
# 严禁自动回滚数据库。DB 必须手动处理。

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${ROOT_DIR}"

DEPLOY_DIR="/opt/ypat-staging"
CURRENT_LINK="${DEPLOY_DIR}/current"
PREVIOUS_LINK="${DEPLOY_DIR}/previous"
RELEASES_DIR="${DEPLOY_DIR}/releases"

TARGET=""
LIST_ONLY=0

while [ $# -gt 0 ]; do
  case "$1" in
    --list) LIST_ONLY=1; shift ;;
    --to) TARGET="${2:?}"; shift 2 ;;
    -h|--help) grep "^# " "${BASH_SOURCE[0]}" | head -5; exit 0 ;;
    *) log_error "未知选项: $1"; exit 1 ;;
  esac
done

# 列出 release
if [ "${LIST_ONLY}" -eq 1 ]; then
  log_info "可用 release："
  ls -1 "${RELEASES_DIR}" 2>/dev/null | sort -r
  exit 0
fi

# 决定目标 release
if [ -z "${TARGET}" ]; then
  if [ ! -L "${PREVIOUS_LINK}" ]; then
    log_error "没有 previous release 可回滚"
    exit 1
  fi
  TARGET=$(readlink "${PREVIOUS_LINK}")
  log_info "目标: ${TARGET}"
fi

# 验证目标存在
if [ ! -d "${TARGET}" ]; then
  log_error "目标 release 不存在: ${TARGET}"
  exit 1
fi

# 检查当前不是同一版本
CURRENT=$(readlink "${CURRENT_LINK}" 2>/dev/null || echo "")
if [ "${CURRENT}" = "${TARGET}" ]; then
  log_warn "当前已是目标版本"
  exit 0
fi

# 前端：原子切换 current 符号链接
log_info "切换前端符号链接..."
if [ -L "${CURRENT_LINK}" ]; then
  rm -f "${CURRENT_LINK}"
fi
ln -sfn "${TARGET}" "${CURRENT_LINK}"

# 后端：重启容器（旧镜像保留在 docker image list）
if [ -d "${TARGET}/backend" ]; then
  log_info "回滚后端容器..."
  cd "${ROOT_DIR}"
  docker compose -f docker-compose.staging.yml up -d --no-deps wap restapi system-web
fi

# 健康检查
log_info "健康检查..."
sleep 10
HEALTH_OK=1
for ep in "https://panghu.work/" "https://panghu.work/api/banner/list"; do
  if ! curl -sk -o /dev/null -w "%{http_code}" "${ep}" 2>&1 | grep -qE "200|301|302"; then
    log_error "健康检查失败: ${ep}"
    HEALTH_OK=0
  fi
done

if [ "${HEALTH_OK}" -eq 0 ]; then
  log_error "回滚后健康检查失败"
  exit 1
fi

log_info "回滚成功: ${TARGET}"