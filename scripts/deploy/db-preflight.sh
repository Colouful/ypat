#!/usr/bin/env bash
set -euo pipefail

# 数据库预检脚本
# 用途：执行迁移前检查数据库连接、备份状态、环境

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 数据库连接信息（从 .env 读取）
if [ -f ".env" ]; then
    set -a
    # shellcheck disable=SC1091
    source .env
    set +a
fi

DB_HOST="${YPAT_LOCAL_MYSQL_HOST:-127.0.0.1}"
DB_PORT="${YPAT_LOCAL_MYSQL_PORT:-3306}"
DB_USER="${YPAT_LOCAL_MYSQL_USER:-root}"
DB_NAME="${YPAT_LOCAL_MYSQL_DATABASE:-ypat}"

# 检查 mysql 客户端
if ! command -v mysql &> /dev/null; then
    log_error "mysql 客户端未安装"
    exit 1
fi

# 测试连接
log_info "测试数据库连接: ${DB_HOST}:${DB_PORT}"
if mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" -e "SELECT 1;" &> /dev/null; then
    log_info "✓ 数据库连接成功"
else
    log_error "✗ 数据库连接失败"
    exit 1
fi

# 检查数据库是否存在
log_info "检查数据库 ${DB_NAME}..."
if mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" -e "USE ${DB_NAME};" &> /dev/null; then
    log_info "✓ 数据库 ${DB_NAME} 存在"

    # 获取表数量
    TABLE_COUNT=$(mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${DB_NAME}';" -N -s)
    log_info "表数量: ${TABLE_COUNT}"

    if [ "${TABLE_COUNT}" -eq 0 ]; then
        log_warn "数据库为空，需要初始化"
    fi
else
    log_warn "数据库 ${DB_NAME} 不存在"
    read -p "是否创建数据库? (yes/no): " CONFIRM
    if [ "${CONFIRM}" = "yes" ]; then
        mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" -e "CREATE DATABASE ${DB_NAME} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
        log_info "✓ 数据库创建成功"
    else
        log_error "数据库不存在，取消操作"
        exit 1
    fi
fi

# 检查备份目录
log_info "检查备份目录..."
BACKUP_DIR="/opt/ypat-data/backups/mysql"
if [ -d "${BACKUP_DIR}" ]; then
    BACKUP_COUNT=$(ls -1 "${BACKUP_DIR}" 2>/dev/null | wc -l)
    log_info "备份目录: ${BACKUP_DIR}（${BACKUP_COUNT} 个备份）"
else
    log_warn "备份目录不存在: ${BACKUP_DIR}"
    read -p "是否创建备份目录? (yes/no): " CONFIRM
    if [ "${CONFIRM}" = "yes" ]; then
        mkdir -p "${BACKUP_DIR}"
        log_info "✓ 备份目录创建成功"
    fi
fi

# 检查 schema_migration 表
log_info "检查 schema_migration 表..."
if mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" "${DB_NAME}" -e "SHOW TABLES LIKE 'schema_migration';" 2>/dev/null | grep -q "schema_migration"; then
    log_info "✓ schema_migration 表存在"

    # 获取已执行的脚本
    APPLIED_COUNT=$(mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" "${DB_NAME}" -e "SELECT COUNT(*) FROM schema_migration;" -N -s)
    log_info "已执行脚本: ${APPLIED_COUNT}"
else
    log_warn "schema_migration 表不存在"
    log_info "首次执行时会自动创建"
fi

log_info "数据库预检完成"