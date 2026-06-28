#!/usr/bin/env bash
set -euo pipefail

# FastDFS 数据迁移脚本
# 用途：将 FastDFS 数据从项目目录迁移到 /opt/ypat-data/fastdfs
#
# 使用方式：
#   ./migrate-fastdfs-data.sh --dry-run    # 仅显示迁移计划
#   ./migrate-fastdfs-data.sh --execute    # 实际执行
#
# 默认模式：--dry-run

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

MODE="${1:---dry-run}"

if [ "${MODE}" != "--dry-run" ] && [ "${MODE}" != "--execute" ]; then
    log_error "无效参数: ${MODE}"
    echo "使用方式: $0 [--dry-run|--execute]"
    exit 1
fi

# 源目录
SRC_TRACKER="backend/dev/fastdfs/tracker_data"
SRC_STORAGE="backend/dev/fastdfs/storage_data"

# 目标目录
DST_TRACKER="${YPAT_FASTDFS_TRACKER_DATA_DIR:-/opt/ypat-data/fastdfs/tracker}"
DST_STORAGE="${YPAT_FASTDFS_STORAGE_DATA_DIR:-/opt/ypat-data/fastdfs/storage}"

log_info "模式: ${MODE}"
log_info "源目录:"
echo "  Tracker: ${SRC_TRACKER}"
echo "  Storage: ${SRC_STORAGE}"
log_info "目标目录:"
echo "  Tracker: ${DST_TRACKER}"
echo "  Storage: ${DST_STORAGE}"

# 检查源目录
if [ ! -d "${SRC_TRACKER}" ] && [ ! -d "${SRC_STORAGE}" ]; then
    log_warn "源目录不存在，可能是新部署"
    exit 0
fi

# 停止写入
if [ "${MODE}" = "--execute" ]; then
    log_warn "即将停止 FastDFS 服务并迁移数据"
    read -p "确认执行? (yes/no): " CONFIRM
    if [ "${CONFIRM}" != "yes" ]; then
        log_info "取消执行"
        exit 0
    fi

    log_info "停止 FastDFS 服务..."
    docker compose -f backend/dev/fastdfs/docker-compose.yml down || true
    docker compose -f backend/dev/fastdfs/docker-compose.staging.yml down || true
fi

# 统计源目录
count_files() {
    local dir=$1
    if [ -d "${dir}" ]; then
        find "${dir}" -type f | wc -l
    else
        echo "0"
    fi
}

TRACKER_FILES=$(count_files "${SRC_TRACKER}")
STORAGE_FILES=$(count_files "${SRC_STORAGE}")

log_info "源目录文件统计:"
echo "  Tracker: ${TRACKER_FILES} 个文件"
echo "  Storage: ${STORAGE_FILES} 个文件"

# 创建目标目录
if [ "${MODE}" = "--execute" ]; then
    log_info "创建目标目录..."
    mkdir -p "${DST_TRACKER}"
    mkdir -p "${DST_STORAGE}"

    # 使用 rsync 迁移
    if [ -d "${SRC_TRACKER}" ] && [ "${TRACKER_FILES}" -gt 0 ]; then
        log_info "迁移 Tracker 数据..."
        rsync -aHAX "${SRC_TRACKER}/" "${DST_TRACKER}/"
    fi

    if [ -d "${SRC_STORAGE}" ] && [ "${STORAGE_FILES}" -gt 0 ]; then
        log_info "迁移 Storage 数据..."
        rsync -aHAX "${SRC_STORAGE}/" "${DST_STORAGE}/"
    fi

    # 验证文件数量
    NEW_TRACKER_FILES=$(count_files "${DST_TRACKER}")
    NEW_STORAGE_FILES=$(count_files "${DST_STORAGE}")

    log_info "目标目录文件统计:"
    echo "  Tracker: ${NEW_TRACKER_FILES} 个文件"
    echo "  Storage: ${NEW_STORAGE_FILES} 个文件"

    if [ "${TRACKER_FILES}" -ne "${NEW_TRACKER_FILES}" ]; then
        log_error "Tracker 文件数量不一致: 源 ${TRACKER_FILES} -> 目标 ${NEW_TRACKER_FILES}"
        exit 1
    fi

    if [ "${STORAGE_FILES}" -ne "${NEW_STORAGE_FILES}" ]; then
        log_error "Storage 文件数量不一致: 源 ${STORAGE_FILES} -> 目标 ${NEW_STORAGE_FILES}"
        exit 1
    fi

    log_info "✓ 迁移完成"

    log_warn "原目录未删除: ${SRC_TRACKER}、${SRC_STORAGE}"
    log_info "验证通过后，使用以下命令手动清理:"
    echo "  rm -rf ${SRC_TRACKER}"
    echo "  rm -rf ${SRC_STORAGE}"
fi

if [ "${MODE}" = "--dry-run" ]; then
    log_warn "这是 dry-run 模式，未实际执行"
    log_info "如需执行，请使用: $0 --execute"
fi