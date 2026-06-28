#!/usr/bin/env bash
set -euo pipefail

# 预发部署前检查脚本

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

check_failed=0

# 检查 Git 状态
log_info "检查 Git 状态..."
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    log_error "不在 Git 仓库中"
    exit 1
fi

CURRENT_BRANCH=$(git branch --show-current)
log_info "当前分支: ${CURRENT_BRANCH}"

if [ "${CURRENT_BRANCH}" != "codex/deploy-config-hardening" ]; then
    log_warn "当前分支不是 codex/deploy-config-hardening，请确认"
fi

if [ -n "$(git status --porcelain)" ]; then
    log_error "工作区不干净，请先提交或暂存更改"
    git status --short
    exit 1
fi

CURRENT_SHA=$(git rev-parse HEAD)
log_info "当前提交: ${CURRENT_SHA}"

# 检查环境文件
log_info "检查环境文件..."
if [ ! -f ".env.example" ]; then
    log_error ".env.example 不存在"
    ((check_failed++))
fi

if [ ! -f "frontend/.env.staging.example" ]; then
    log_error "frontend/.env.staging.example 不存在"
    ((check_failed++))
fi

if [ ! -f "frontend/.env.production.example" ]; then
    log_error "frontend/.env.production.example 不存在"
    ((check_failed++))
fi

# 检查敏感信息
log_info "检查敏感信息..."
if grep -r "CHANGE_ME" .env.example frontend/.env*.example docker-compose*.yml 2>/dev/null | grep -v "^Binary"; then
    log_warn "发现 CHANGE_ME 占位符，部署前需替换"
fi

# 检查 Docker
log_info "检查 Docker..."
if ! command -v docker &> /dev/null; then
    log_error "Docker 未安装"
    ((check_failed++))
else
    log_info "Docker 版本: $(docker --version)"
fi

# 检查 Docker Compose
if ! command -v docker compose &> /dev/null; then
    log_error "Docker Compose 未安装"
    ((check_failed++))
else
    log_info "Docker Compose 版本: $(docker compose version --short)"
fi

# 检查磁盘空间
log_info "检查磁盘空间..."
DISK_USAGE=$(df -h . | tail -1 | awk '{print $5}' | sed 's/%//')
if [ "${DISK_USAGE}" -gt 80 ]; then
    log_warn "磁盘使用率 ${DISK_USAGE}%，建议清理"
fi

# 检查端口占用
log_info "检查端口占用..."
PORTS=(3306 6379 8761 8081 8082 9081 8888)
for port in "${PORTS[@]}"; do
    if lsof -i ":${port}" &> /dev/null; then
        log_warn "端口 ${port} 已被占用"
    fi
done

# 检查 Compose 配置
log_info "检查 Docker Compose 配置..."
if [ -f "docker-compose.staging.yml" ]; then
    if docker compose -f docker-compose.staging.yml config &> /dev/null; then
        log_info "docker-compose.staging.yml 配置有效"
    else
        log_error "docker-compose.staging.yml 配置无效"
        ((check_failed++))
    fi
else
    log_error "docker-compose.staging.yml 不存在"
    ((check_failed++))
fi

# 检查 FastDFS 配置
if [ -f "backend/dev/fastdfs/docker-compose.staging.yml" ]; then
    if docker compose -f backend/dev/fastdfs/docker-compose.staging.yml config &> /dev/null; then
        log_info "FastDFS Compose 配置有效"
    else
        log_error "FastDFS Compose 配置无效"
        ((check_failed++))
    fi
else
    log_error "FastDFS Compose 配置不存在"
    ((check_failed++))
fi

# 检查 Nginx 配置
log_info "检查 Nginx 配置..."
if [ -f "docker/nginx/panghu.work.conf" ]; then
    # 注意：本地没有 nginx，无法执行 nginx -t
    log_info "Nginx 配置文件存在（部署时在服务器验证）"
else
    log_error "Nginx 配置文件不存在"
    ((check_failed++))
fi

# 总结
echo
log_info "检查完成"
if [ ${check_failed} -eq 0 ]; then
    log_info "所有检查通过"
    exit 0
else
    log_error "${check_failed} 项检查失败"
    exit 1
fi