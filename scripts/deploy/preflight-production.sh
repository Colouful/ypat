#!/usr/bin/env bash
set -euo pipefail

# YPAT production 预检脚本（必须在 deploy-production.sh 前运行）
#
# 默认拒绝执行，除非显式 --confirm-production 标志
#
# 必填：
#   --confirm-production        用户已在生产变更审批单签字
#   --work-dir <abs path>       工作目录（绝对路径）
#   --release-tag <tag>         受保护的 release tag（不能用任意 commit）

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

CONFIRM_PRODUCTION=0
WORK_DIR=""
RELEASE_TAG=""
APPROVAL_TICKET=""

usage() {
  cat <<EOF
用法: $0 --confirm-production --work-dir <abs> --release-tag <tag> [--approval-ticket <id>]

本脚本是 production 部署前的强制门禁。
必须显式 --confirm-production 才允许继续。
EOF
  exit "${1:-0}"
}

while [ $# -gt 0 ]; do
  case "$1" in
    --confirm-production) CONFIRM_PRODUCTION=1; shift ;;
    --work-dir) WORK_DIR="${2:?}"; shift 2 ;;
    --release-tag) RELEASE_TAG="${2:?}"; shift 2 ;;
    --approval-ticket) APPROVAL_TICKET="${2:?}"; shift 2 ;;
    -h|--help) usage 0 ;;
    *) log_error "未知选项: $1"; usage 1 ;;
  esac
done

if [ "${CONFIRM_PRODUCTION}" -ne 1 ]; then
  log_error "必须 --confirm-production 显式确认才能执行 production 预检"
  usage 1
fi

if [ -z "${WORK_DIR}" ]; then
  log_error "必须 --work-dir <abs>"
  usage 1
fi

if [ -z "${RELEASE_TAG}" ]; then
  log_error "必须 --release-tag <tag> 明确受保护 release"
  usage 1
fi

if [ -z "${APPROVAL_TICKET}" ]; then
  log_warn "建议提供 --approval-ticket <id> 用于审计追溯"
fi

log_info "确认 production 部署"
log_info "工作目录: ${WORK_DIR}"
log_info "Release tag: ${RELEASE_TAG}"
log_info "审批工单: ${APPROVAL_TICKET:-NONE}"

# 验证 release tag 是受保护的（git tag 存在 + 当前分支能访问）
cd "${WORK_DIR}"
if ! git rev-parse "${RELEASE_TAG}" > /dev/null 2>&1; then
  log_error "release tag ${RELEASE_TAG} 不存在"
  exit 1
fi

# 必须 tag 是 annotated tag
TAG_TYPE=$(git cat-file -t "${RELEASE_TAG}" 2>/dev/null || echo "")
if [ "${TAG_TYPE}" != "tag" ]; then
  log_error "必须使用 annotated tag (git tag -a), 当前是 ${TAG_TYPE}"
  exit 1
fi

log_info "release tag ${RELEASE_TAG} 验证通过 (annotated tag)"

# 复用 staging 预检（更严格）
"${WORK_DIR}/scripts/deploy/preflight.sh" --env production --work-dir "${WORK_DIR}" || {
  log_error "production 预检失败"
  exit 1
}

# production 专属：必须没有 staging 域名/IP
log_info "production 互窜检查..."
PROD_FILES=(
  ".env"
  "docker-compose.production.yml"
  "backend/.env.production.example"
  "frontend/.env.production.example"
)
for f in "${PROD_FILES[@]}"; do
  [ -f "${f}" ] || continue
  if grep -E "panghu\.work|82\.156\.14\.216|www\.panghu\.work" "${f}" | grep -vE "^\s*#|example\.invalid|/\*" 2>/dev/null; then
    log_error "${f} 引用了 staging 域名/IP，production 部署禁止"
    exit 1
  fi
done

# production 专属：必须有业务账号（不能用 root）
log_info "production 数据库账号最小权限检查..."
if [ -f ".env" ]; then
  if grep -E "^YPAT_DB_USERNAME=" .env | grep -q "=root"; then
    log_error "production YPAT_DB_USERNAME 不能是 root"
    exit 1
  fi
  if ! grep -qE "^YPAT_DB_NAME=" .env || ! grep -qE "^YPAT_DB_PASSWORD=" .env; then
    log_error "production 必须设置 YPAT_DB_NAME/YPAT_DB_USERNAME/YPAT_DB_PASSWORD 业务账号"
    exit 1
  fi
fi

# 必须没有 SMS mock
log_info "production SMS Mock 检查..."
if [ -f ".env" ] && grep -E "^YPAT_SMS_MOCK_ENABLED=true" .env; then
  log_error "production YPAT_SMS_MOCK_ENABLED 必须为 false（禁止 SMS Mock）"
  exit 1
fi

log_info "✓ production 预检全部通过"
log_info "Release tag: ${RELEASE_TAG}"
log_info "审批工单: ${APPROVAL_TICKET:-NONE}"
log_info "可以执行 deploy-production.sh"