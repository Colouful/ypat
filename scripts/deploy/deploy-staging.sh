#!/usr/bin/env bash
set -euo pipefail

# 预发环境部署脚本

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

DEPLOY_DIR="/opt/ypat"
RELEASES_DIR="${DEPLOY_DIR}/releases"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
CURRENT_DIR="${DEPLOY_DIR}/current"
PREVIOUS_DIR="${DEPLOY_DIR}/previous"

# 获取当前版本
log_info "获取当前版本..."
CURRENT_VERSION=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
log_info "当前版本: ${CURRENT_VERSION}"

# 创建发布目录
log_info "创建发布目录: ${RELEASES_DIR}/${TIMESTAMP}"
mkdir -p "${RELEASES_DIR}/${TIMESTAMP}"

# 构建前端 staging
log_info "构建前端 staging..."
cd frontend
pnpm install --frozen-lockfile
pnpm run build:h5:staging

# 备份当前前端
log_info "备份当前前端..."
if [ -d "${CURRENT_DIR}/frontend" ]; then
    cp -r "${CURRENT_DIR}/frontend" "${RELEASES_DIR}/${TIMESTAMP}/frontend.bak"
    log_info "前端备份完成"
else
    log_warn "前端目录不存在，跳过备份"
fi

# 复制新前端
log_info "复制新前端..."
cp -r dist/build/h5 "${RELEASES_DIR}/${TIMESTAMP}/frontend"
cd ..

# 构建后端
log_info "构建后端 -Ppre..."
cd backend
mvn clean package -Ppre -DskipTests=false

# 保存部署记录
log_info "保存部署记录..."
cat > "${RELEASES_DIR}/${TIMESTAMP}/deployment-info.txt" << EOF
版本: ${CURRENT_VERSION}
时间: ${TIMESTAMP}
Git SHA: $(git rev-parse HEAD)
Git Branch: $(git branch --show-current)
EOF

# 更新符号链接
log_info "更新符号链接..."
if [ -L "${CURRENT_DIR}" ]; then
    rm "${CURRENT_DIR}"
elif [ -d "${CURRENT_DIR}" ]; then
    rm -rf "${CURRENT_DIR}"
fi
ln -s "${RELEASES_DIR}/${TIMESTAMP}" "${CURRENT_DIR}"

if [ -L "${PREVIOUS_DIR}" ]; then
    rm "${PREVIOUS_DIR}"
elif [ -d "${PREVIOUS_DIR}" ]; then
    rm -rf "${PREVIOUS_DIR}"
fi
PREVIOUS_TIMESTAMP=$(ls -t "${RELEASES_DIR}" | head -2 | tail -1)
if [ -n "${PREVIOUS_TIMESTAMP}" ]; then
    ln -s "${RELEASES_DIR}/${PREVIOUS_TIMESTAMP}" "${PREVIOUS_DIR}"
    log_info "上一个版本: ${PREVIOUS_TIMESTAMP}"
else
    log_warn "没有上一个版本"
fi

# Docker 部署
log_info "验证 Docker Compose 配置..."
docker compose -f docker-compose.staging.yml config > /dev/null
docker compose -f backend/dev/fastdfs/docker-compose.staging.yml config > /dev/null

log_info "构建 Docker 镜像..."
docker compose -f docker-compose.staging.yml build

log_info "启动服务..."
docker compose -f docker-compose.staging.yml up -d

log_info "部署完成"
log_info "当前版本: ${RELEASES_DIR}/${TIMESTAMP}"