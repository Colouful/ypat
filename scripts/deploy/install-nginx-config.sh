#!/usr/bin/env bash
set -euo pipefail

# 主机 Nginx 配置安装脚本
# 用途：原子化部署主机 Nginx 配置，支持自动回滚

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

if [ "$EUID" -ne 0 ]; then
    log_error "需要 root 权限"
    exit 1
fi

SOURCE_CONFIG="docker/nginx/panghu.work.conf"
DEST_CONFIG="/etc/nginx/conf.d/panghu.work.conf"
BACKUP_CONFIG="/etc/nginx/conf.d/panghu.work.conf.bak.$(date +%Y%m%d_%H%M%S)"

if [ ! -f "${SOURCE_CONFIG}" ]; then
    log_error "源配置文件不存在: ${SOURCE_CONFIG}"
    exit 1
fi

log_info "备份当前配置: ${BACKUP_CONFIG}"
cp -p "${DEST_CONFIG}" "${BACKUP_CONFIG}" 2>/dev/null || log_warn "当前配置不存在，跳过备份"

# 复制到临时文件
TEMP_CONFIG=$(mktemp)
cp "${SOURCE_CONFIG}" "${TEMP_CONFIG}"
chmod 644 "${TEMP_CONFIG}"

log_info "测试新配置..."
if nginx -t 2>&1 | tee /tmp/nginx_test.log; then
    log_info "✓ 配置测试通过"

    # 原子替换
    log_info "替换配置..."
    mv "${TEMP_CONFIG}" "${DEST_CONFIG}"

    # Reload nginx
    log_info "Reload nginx..."
    systemctl reload nginx

    # 健康检查
    sleep 2
    if curl -sf -o /dev/null -w "%{http_code}" https://panghu.work/ 2>/dev/null | grep -q "200\|301\|302"; then
        log_info "✓ 健康检查通过"
        exit 0
    else
        log_error "健康检查失败，尝试回滚"
        if [ -f "${BACKUP_CONFIG}" ]; then
            cp "${BACKUP_CONFIG}" "${DEST_CONFIG}"
            systemctl reload nginx
            log_info "已回滚到备份配置"
        fi
        exit 1
    fi
else
    log_error "配置测试失败，不替换"
    rm -f "${TEMP_CONFIG}"
    exit 1
fi