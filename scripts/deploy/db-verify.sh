#!/usr/bin/env bash
set -euo pipefail

# 数据库验证脚本

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

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

log_info "验证数据库: ${DB_HOST}:${DB_PORT}/${DB_NAME}"

# 检查表数量
TABLE_COUNT=$(mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${DB_NAME}';" -N -s)
log_info "表数量: ${TABLE_COUNT}"

# 列出所有表
log_info "表列表:"
mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" -e "SHOW TABLES FROM ${DB_NAME};"

# 检查 schema_migration
if mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" "${DB_NAME}" -e "SHOW TABLES LIKE 'schema_migration';" 2>/dev/null | grep -q "schema_migration"; then
    log_info "已执行的迁移:"
    mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" "${DB_NAME}" -e "SELECT version, executed_at, execution_time_ms FROM schema_migration ORDER BY id;"
else
    log_warn "schema_migration 表不存在"
fi

log_info "验证完成"