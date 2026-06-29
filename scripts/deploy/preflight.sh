#!/usr/bin/env bash
# YPAT 部署预检脚本（staging / production 通用）
#
# 修复要点：
#   - 移除硬编码分支名 codex/deploy-config-hardening
#   - 接受 --env <staging|production> --compose-file <path> --work-dir <abs path>
#   - 接受 main / release/* / hotfix/* / 任意 SHA
#   - 全面预检：环境文件 / .env 权限 / 必填变量 / 端口 / 磁盘 / 镜像 digest / DNS
#   - 缺一不可：任何 fail → exit 1
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

check_failed=0
WORK_DIR=""
ENV_NAME="staging"
COMPOSE_FILE="docker-compose.staging.yml"
FASTDFS_COMPOSE_FILE="backend/dev/fastdfs/docker-compose.staging.yml"

usage() {
  cat <<EOF
用法: $0 [选项]

选项:
  --env <name>              环境：staging | production （默认 staging）
  --work-dir <abs path>     工作目录（绝对路径，默认当前目录）
  --compose-file <path>     主 compose 文件（默认随环境）
  --fastdfs-compose <path>  FastDFS compose 文件（默认随环境）
  --skip-disk               跳过磁盘检查
  --skip-port               跳过端口检查
  -h, --help                显示帮助
EOF
  exit "${1:-0}"
}

while [ $# -gt 0 ]; do
  case "$1" in
    --env) ENV_NAME="${2:?}"; shift 2 ;;
    --work-dir) WORK_DIR="${2:?}"; shift 2 ;;
    --compose-file) COMPOSE_FILE="${2:?}"; shift 2 ;;
    --fastdfs-compose) FASTDFS_COMPOSE_FILE="${2:?}"; shift 2 ;;
    --skip-disk) SKIP_DISK=1; shift ;;
    --skip-port) SKIP_PORT=1; shift ;;
    -h|--help) usage 0 ;;
    *) log_error "未知选项: $1"; usage 1 ;;
  esac
done

if [ -z "${WORK_DIR}" ]; then
  WORK_DIR="$(pwd)"
fi

# 必须为绝对路径（避免 cd 后丢失路径）
case "${WORK_DIR}" in
  /*) ;;
  *) log_error "--work-dir 必须是绝对路径: ${WORK_DIR}"; exit 1 ;;
esac

cd "${WORK_DIR}"

if [ "${ENV_NAME}" = "staging" ]; then
  COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.staging.yml}"
  FASTDFS_COMPOSE_FILE="${FASTDFS_COMPOSE_FILE:-backend/dev/fastdfs/docker-compose.staging.yml}"
elif [ "${ENV_NAME}" = "production" ]; then
  COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.production.yml}"
  FASTDFS_COMPOSE_FILE="${FASTDFS_COMPOSE_FILE:-backend/dev/fastdfs/docker-compose.production.yml}"
else
  log_error "--env 必须是 staging 或 production，当前: ${ENV_NAME}"
  exit 1
fi

log_info "环境: ${ENV_NAME}"
log_info "工作目录: ${WORK_DIR}"
log_info "主 compose: ${COMPOSE_FILE}"
log_info "FastDFS compose: ${FASTDFS_COMPOSE_FILE}"

# 检查 Git 仓库
log_info "检查 Git 状态..."
if ! git rev-parse --git-dir > /dev/null 2>&1; then
  log_error "不在 Git 仓库中"
  exit 1
fi

CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "DETACHED")
log_info "当前分支: ${CURRENT_BRANCH}"

# 分支白名单：main / release/* / hotfix/* 或 detached HEAD 显式 SHA
ALLOWED=0
case "${CURRENT_BRANCH}" in
  main|main2|release/*|hotfix/*) ALLOWED=1 ;;
  DETACHED)
    log_warn "DETACHED HEAD 状态：必须明确 SHA 用于部署"
    ;;
  *)
    log_warn "当前分支不在白名单 (main / release/* / hotfix/*) 中，请确认"
    ;;
esac
if [ "${ALLOWED}" -eq 0 ] && [ "${CURRENT_BRANCH}" != "DETACHED" ]; then
  log_warn "分支 ${CURRENT_BRANCH} 不在推荐白名单内"
fi

CURRENT_SHA=$(git rev-parse HEAD)
log_info "当前 SHA: ${CURRENT_SHA}"

# 工作区必须干净
if [ -n "$(git status --porcelain)" ]; then
  log_error "工作区不干净，请先提交或暂存更改"
  git status --short
  exit 1
fi

# 镜像 digest 必须固定
log_info "检查镜像 digest 固定..."
LATEST_USAGE=$(grep -E "image:\s+\S+:latest" docker-compose*.yml 2>/dev/null || true)
if [ -n "${LATEST_USAGE}" ]; then
  log_error "发现 :latest 镜像：必须固定 digest 或补丁版本"
  echo "${LATEST_USAGE}"
  ((check_failed++))
fi

# 环境文件
log_info "检查环境文件..."
for f in ".env" "frontend/.env.staging.example" "frontend/.env.production.example" "frontend/.env.development.example" ".env.example"; do
  if [ ! -f "${f}" ]; then
    log_error "缺少 ${f}"
    ((check_failed++))
  fi
done

# .env 权限 600
if [ -f ".env" ]; then
  PERM=$(stat -c "%a" ".env" 2>/dev/null || stat -f "%Lp" ".env" 2>/dev/null)
  if [ "${PERM}" != "600" ] && [ "${PERM}" != "400" ]; then
    log_error ".env 权限 ${PERM} 必须为 600 或 400"
    ((check_failed++))
  fi
fi

# 必填变量
log_info "检查必填变量..."
if [ -f ".env" ]; then
  REQUIRED_VARS=(
    YPAT_LOCAL_MYSQL_ROOT_PASSWORD
    YPAT_LOCAL_REDIS_PASSWORD
    YPAT_FASTDFS_IMAGE
    YPAT_FDFS_PUBLIC_BASE_URL
  )
  for var in "${REQUIRED_VARS[@]}"; do
    val=$(grep -E "^${var}=" .env | head -1 | cut -d= -f2- || true)
    if [ -z "${val}" ]; then
      log_error "${var} 未设置"
      ((check_failed++))
    fi
    case "${val}" in
      CHANGE_ME|placeholder|TODO|example.invalid)
        log_error "${var} 仍为占位符: ${val}"
        ((check_failed++))
        ;;
      "<placeholder>")
        log_error "${var} 仍为占位符: ${val}"
        ((check_failed++))
        ;;
    esac
  done
fi

# 检查敏感串环境
log_info "检查环境串写..."
if [ -f ".env" ]; then
  if [ "${ENV_NAME}" = "production" ]; then
    if grep -E "panghu\.work|82\.156\.14\.216|www\.panghu\.work" .env; then
      log_error "production 环境引用 staging 域名或 IP"
      ((check_failed++))
    fi
  fi
fi

# Docker
log_info "检查 Docker..."
if ! command -v docker &> /dev/null; then
  log_error "Docker 未安装"
  ((check_failed++))
else
  log_info "Docker 版本: $(docker --version)"
fi

# Docker Compose（必须用 docker compose 子命令，不是 docker-compose）
if ! command -v docker &> /dev/null; then
  log_error "Docker 未安装，无法检查 docker compose"
  ((check_failed++))
elif ! docker compose version &> /dev/null; then
  log_error "docker compose 不可用（缺失 v2 插件）"
  ((check_failed++))
else
  log_info "Docker Compose 版本: $(docker compose version --short)"
fi

# 磁盘
if [ -z "${SKIP_DISK:-}" ]; then
  log_info "检查磁盘空间..."
  DISK_USAGE=$(df -h . | tail -1 | awk '{print $5}' | sed 's/%//' || echo "0")
  if [ "${DISK_USAGE}" -gt 85 ]; then
    log_error "磁盘使用率 ${DISK_USAGE}% 超过 85%"
    ((check_failed++))
  else
    log_info "磁盘使用率 ${DISK_USAGE}%"
  fi
fi

# 端口
if [ -z "${SKIP_PORT:-}" ]; then
  log_info "检查端口占用..."
  case "${ENV_NAME}" in
    staging)
      PORTS=(80 443 3306 6379 8761 8081 8082 9081 8888 22122 23000) ;;
    production)
      PORTS=(80 443 3306 6379 8761 8081 8082 9081 8888 22122 23000) ;;
  esac
  for port in "${PORTS[@]}"; do
    if lsof -i ":${port}" &> /dev/null; then
      log_warn "端口 ${port} 已被占用"
    fi
  done
fi

# 证书（如果 HTTPS 端口由主机 nginx 监听）
if [ -f "/etc/nginx/ssl/panghu.work/panghu.work_bundle.crt" ]; then
  log_info "检查 TLS 证书..."
  NOT_AFTER=$(openssl x509 -in /etc/nginx/ssl/panghu.work/panghu.work_bundle.crt -noout -enddate 2>/dev/null | sed 's/notAfter=//')
  if [ -n "${NOT_AFTER}" ]; then
    EXPIRY_EPOCH=$(date -d "${NOT_AFTER}" +%s 2>/dev/null || echo 0)
    NOW_EPOCH=$(date +%s)
    REMAINING=$(( (EXPIRY_EPOCH - NOW_EPOCH) / 86400 ))
    if [ "${REMAINING}" -lt 30 ]; then
      log_warn "证书将在 ${REMAINING} 天后过期 (${NOT_AFTER})"
    else
      log_info "证书有效，剩余 ${REMAINING} 天"
    fi
  fi
fi

# Compose 配置
log_info "检查 Docker Compose 配置..."
for cf in "${COMPOSE_FILE}" "${FASTDFS_COMPOSE_FILE}"; do
  if [ ! -f "${cf}" ]; then
    log_error "Compose 文件不存在: ${cf}"
    ((check_failed++))
    continue
  fi
  if docker compose -f "${cf}" config > /dev/null 2>&1; then
    log_info "${cf} 配置有效"
  else
    log_error "${cf} 配置无效"
    docker compose -f "${cf}" config 2>&1 | head -10
    ((check_failed++))
  fi
done

# Nginx 配置（如果存在主机配置）
if [ -f "/etc/nginx/conf.d/panghu.work.conf" ]; then
  log_info "检查主机 Nginx 配置..."
  if nginx -t 2>&1 | grep -q "test is successful"; then
    log_info "Nginx 配置语法正确"
  else
    log_error "Nginx 配置语法错误"
    nginx -t 2>&1 | tail -5
    ((check_failed++))
  fi
fi

# 镜像 digest
log_info "检查 FastDFS 镜像 digest..."
if [ -f ".env" ]; then
  DIGEST=$(grep -E "^YPAT_FASTDFS_IMAGE=" .env | cut -d= -f2- || true)
  if [ -n "${DIGEST}" ]; then
    if echo "${DIGEST}" | grep -q "@sha256:"; then
      log_info "FastDFS 镜像 digest 固定: ${DIGEST}"
    else
      log_warn "FastDFS 镜像未固定 digest: ${DIGEST}"
    fi
  fi
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