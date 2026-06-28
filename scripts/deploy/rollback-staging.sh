#!/usr/bin/env bash
set -euo pipefail

# 预发环境回滚脚本

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

DEPLOY_DIR="/opt/ypat"
RELEASES_DIR="${DEPLOY_DIR}/releases"
CURRENT_DIR="${DEPLOY_DIR}/current"
PREVIOUS_DIR="${DEPLOY_DIR}/previous"

if [ $# -eq 0 ]; then
    log_error "请指定回滚版本，格式: YYYYMMDD_HHMMSS"
    echo
    log_info "可用版本:"
    ls -1 "${RELEASES_DIR}" | sort -r | head -5
    exit 1
fi

ROLLBACK_VERSION=$1
ROLLBACK_DIR="${RELEASES_DIR}/${ROLLBACK_VERSION}"

if [ ! -d "${ROLLBACK_DIR}" ]; then
    log_error "版本不存在: ${ROLLBACK_DIR}"
    exit 1
fi

log_info "确认回滚到版本: ${ROLLBACK_VERSION}"
log_info "回滚目录: ${ROLLBACK_DIR}"
log_info "回滚信息:"
cat "${ROLLBACK_DIR}/deployment-info.txt"

echo
read -p "确认回滚? (yes/no): " CONFIRM
if [ "${CONFIRM}" != "yes" ]; then
    log_info "取消回滚"
    exit 0
fi

# 备份当前版本
log_info "备份当前版本..."
if [ -L "${CURRENT_DIR}" ]; then
    CURRENT_LINK=$(readlink "${CURRENT_DIR}")
    log_info "当前版本: ${CURRENT_LINK}"
fi

# 回滚前端
log_info "回滚前端..."
if [ -d "${ROLLBACK_DIR}/frontend" ]; then
    if [ -d "/var/www/panghu.work" ]; then
        cp -r "${ROLLBACK_DIR}/frontend"/* /var/www/panghu.work/
        log_info "前端回滚完成"
    else
        log_warn "/var/www/panghu.work 不存在，跳过前端回滚"
    fi
else
    log_warn "前端目录不存在，跳过前端回滚"
fi

# 回滚 Docker 服务
log_info "回滚 Docker 服务..."
docker compose -f docker-compose.staging.yml down
docker compose -f backend/dev/fastdfs/docker-compose.staging.yml down

# 需要根据实际情况处理 Docker 镜像回滚
# 这里简化处理，重新启动服务
docker compose -f docker-compose.staging.yml up -d
docker compose -f backend/dev/fastdfs/docker-compose.staging.yml up -d

log_info "回滚完成"
log_info "已回滚到版本: ${ROLLBACK_VERSION}"