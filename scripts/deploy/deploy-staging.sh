#!/usr/bin/env bash
set -euo pipefail

# YPAT staging 部署脚本（不可变 release + 原子切换 + 自动回滚）
#
# 用法:
#   ./deploy-staging.sh                         # 完整部署
#   ./deploy-staging.sh --skip-build            # 跳过构建（仅重启容器）
#   ./deploy-staging.sh --frontend-only         # 仅部署前端
#   ./deploy-staging.sh --backend-only          # 仅部署后端
#
# 必须从仓库根目录运行（cd 到仓库根再执行）

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 强制从仓库根目录解析所有路径
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${ROOT_DIR}"

DEPLOY_DIR="/opt/ypat-staging"
RELEASES_DIR="${DEPLOY_DIR}/releases"
CURRENT_LINK="${DEPLOY_DIR}/current"
PREVIOUS_LINK="${DEPLOY_DIR}/previous"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
DEPLOY_LOCK="${DEPLOY_DIR}/.deploy.lock"

# 选项
SKIP_BUILD=0
FRONTEND_ONLY=0
BACKEND_ONLY=0

while [ $# -gt 0 ]; do
  case "$1" in
    --skip-build) SKIP_BUILD=1; shift ;;
    --frontend-only) FRONTEND_ONLY=1; shift ;;
    --backend-only) BACKEND_ONLY=1; shift ;;
    -h|--help)
      grep "^# " "${BASH_SOURCE[0]}" | head -10
      exit 0
      ;;
    *) log_error "未知选项: $1"; exit 1 ;;
  esac
done

# 1. 部署锁（防止并发）
if [ -e "${DEPLOY_LOCK}" ]; then
  log_error "已有部署正在进行：${DEPLOY_LOCK}"
  exit 1
fi
trap "rm -f ${DEPLOY_LOCK}" EXIT
mkdir -p "${DEPLOY_DIR}"
touch "${DEPLOY_LOCK}"

# 2. Git SHA 记录
CURRENT_SHA=$(git rev-parse HEAD)
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "DETACHED")
log_info "Git SHA: ${CURRENT_SHA}"
log_info "Git branch: ${CURRENT_BRANCH}"

# 3. 预检（强制）
log_info "运行预检..."
"${ROOT_DIR}/scripts/deploy/preflight.sh" --env staging --work-dir "${ROOT_DIR}" || {
  log_error "预检失败，部署中止"
  exit 1
}

# 4. 创建不可变 release 目录
RELEASE_DIR="${RELEASES_DIR}/${TIMESTAMP}-${CURRENT_SHA:0:8}"
mkdir -p "${RELEASE_DIR}"
log_info "Release 目录: ${RELEASE_DIR}"

# 5. 镜像标签使用 Git SHA
GIT_TAG="${CURRENT_SHA:0:8}"

# 6. 构建前端（如果需要）
if [ "${BACKEND_ONLY}" -eq 0 ]; then
  log_info "构建前端 staging..."
  cd frontend
  if [ "${SKIP_BUILD}" -eq 0 ]; then
    pnpm install --frozen-lockfile
    pnpm run build:h5:staging
  else
    log_warn "跳过前端构建"
  fi
  cd "${ROOT_DIR}"

  # 复制到 release 目录
  cp -r dist/build/h5 "${RELEASE_DIR}/frontend"
fi

# 7. 构建后端（如果需要）
if [ "${FRONTEND_ONLY}" -eq 0 ]; then
  log_info "构建后端 -Ppre..."
  if [ "${SKIP_BUILD}" -eq 0 ]; then
    (cd backend && mvn clean package -Ppre -DskipTests=false) || {
      log_error "后端构建失败"
      exit 1
    }
  fi
  # 复制 jar
  mkdir -p "${RELEASE_DIR}/backend"
  for module in system-restapi system-wap system-web; do
    jar="backend/${module}/target/${module}-1.0-SNAPSHOT.jar"
    [ -f "${jar}" ] && cp "${jar}" "${RELEASE_DIR}/backend/"
  done
fi

# 8. 备份当前 release → previous
if [ -L "${CURRENT_LINK}" ]; then
  PREV_TARGET=$(readlink "${CURRENT_LINK}")
  rm -f "${PREVIOUS_LINK}"
  ln -s "${PREV_TARGET}" "${PREVIOUS_LINK}"
  log_info "Previous link: ${PREV_TARGET}"
fi

# 9. 原子切换 current
rm -f "${CURRENT_LINK}"
ln -s "${RELEASE_DIR}" "${CURRENT_LINK}"
log_info "Current link: ${RELEASE_DIR}"

# 10. Docker 镜像构建（带 Git SHA 标签）
if [ "${FRONTEND_ONLY}" -eq 0 ]; then
  log_info "构建后端 Docker 镜像 (tag: ${GIT_TAG})..."
  cd "${ROOT_DIR}"
  docker compose -f docker-compose.staging.yml build \
    --build-arg GIT_SHA="${CURRENT_SHA}"
fi

# 11. 更新应用容器（不重启状态服务）
log_info "更新应用容器..."
cd "${ROOT_DIR}"
COMPOSE_ARGS="-f docker-compose.staging.yml"
if [ "${FRONTEND_ONLY}" -eq 0 ]; then
  docker compose ${COMPOSE_ARGS} up -d --no-deps wap restapi system-web eureka
else
  log_info "仅前端更新，重启 Nginx（如果有）"
fi

# 12. 健康检查
log_info "等待健康检查..."
sleep 15
ENDPOINTS=("https://panghu.work/" "https://panghu.work/api/banner/list")
HEALTH_OK=1
for ep in "${ENDPOINTS[@]}"; do
  if ! curl -sk -o /dev/null -w "%{http_code}" "${ep}" 2>&1 | grep -qE "200|301|302"; then
    log_error "健康检查失败: ${ep}"
    HEALTH_OK=0
  fi
done

if [ "${HEALTH_OK}" -eq 0 ]; then
  log_warn "健康检查失败，自动回滚..."
  if [ -L "${PREVIOUS_LINK}" ]; then
    rm -f "${CURRENT_LINK}"
    ln -s "$(readlink "${PREVIOUS_LINK}")" "${CURRENT_LINK}"
    docker compose ${COMPOSE_ARGS} up -d --no-deps wap restapi system-web
    log_info "已回滚到 previous"
  fi
  exit 1
fi

# 13. 记录 deployment
cat > "${RELEASE_DIR}/deployment-info.txt" <<EOF
Git SHA: ${CURRENT_SHA}
Git Branch: ${CURRENT_BRANCH}
Timestamp: ${TIMESTAMP}
End-to-end OK: YES
EOF

log_info "部署完成: ${RELEASE_DIR}"