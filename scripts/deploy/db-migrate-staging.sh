#!/usr/bin/env bash
set -euo pipefail

# 数据库迁移脚本（预发环境）
# 用途：执行预发环境数据库迁移
#
# 使用方式：
#   ./db-migrate-staging.sh --dry-run    # 仅显示待执行脚本
#   ./db-migrate-staging.sh --execute    # 实际执行
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

# 加载环境变量
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

# 确认环境
log_info "数据库: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
log_info "模式: ${MODE}"

if [ "${MODE}" = "--execute" ]; then
    log_warn "即将在预发数据库执行迁移"
    read -p "确认执行? (yes/no): " CONFIRM
    if [ "${CONFIRM}" != "yes" ]; then
        log_info "取消执行"
        exit 0
    fi
fi

# 创建 schema_migration 表
create_migration_table() {
    mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" "${DB_NAME}" <<EOF
CREATE TABLE IF NOT EXISTS schema_migration (
    id INT AUTO_INCREMENT PRIMARY KEY,
    version VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    script_sha256 CHAR(64) NOT NULL,
    executed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time_ms INT,
    INDEX idx_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
EOF
}

# 备份当前数据库
backup_database() {
    local backup_file="/opt/ypat-data/backups/mysql/ypat_$(date +%Y%m%d_%H%M%S).sql.gz"
    mkdir -p "$(dirname "${backup_file}")"
    log_info "备份数据库到: ${backup_file}"
    mysqldump -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" "${DB_NAME}" | gzip > "${backup_file}"
    log_info "✓ 备份完成"
}

# 计算 SHA256
calc_sha256() {
    sha256sum "$1" | awk '{print $1}'
}

# 执行迁移
execute_migration() {
    local script_file=$1

    if [ ! -f "${script_file}" ]; then
        log_error "脚本不存在: ${script_file}"
        return 1
    fi

    local version=$(basename "${script_file}" .sql)
    local sha256=$(calc_sha256 "${script_file}")

    # 检查是否已执行
    local applied=$(mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" "${DB_NAME}" -e "SELECT COUNT(*) FROM schema_migration WHERE version = '${version}';" -N -s)

    if [ "${applied}" -gt 0 ]; then
        log_warn "跳过（已执行）: ${version}"
        return 0
    fi

    log_info "待执行: ${version}"

    if [ "${MODE}" = "--execute" ]; then
        local start_time=$(date +%s%3N)

        # 执行脚本
        mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" "${DB_NAME}" < "${script_file}"

        local end_time=$(date +%s%3N)
        local execution_time=$((end_time - start_time))

        # 记录执行
        mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" "${DB_NAME}" <<EOF
INSERT INTO schema_migration (version, script_sha256, execution_time_ms)
VALUES ('${version}', '${sha256}', ${execution_time});
EOF

        log_info "✓ 执行完成: ${version} (${execution_time}ms)"
    fi
}

# 主程序
log_info "初始化 schema_migration 表..."
if [ "${MODE}" = "--execute" ]; then
    create_migration_table
    backup_database
fi

# 扫描所有 SQL 脚本
SCRIPT_DIR="backend/dev/mysql"
if [ ! -d "${SCRIPT_DIR}" ]; then
    log_error "脚本目录不存在: ${SCRIPT_DIR}"
    exit 1
fi

log_info "扫描迁移脚本..."
SCRIPTS=$(find "${SCRIPT_DIR}" -name "*.sql" -type f | grep -v "_rollback" | sort)

if [ -z "${SCRIPTS}" ]; then
    log_warn "没有找到迁移脚本"
    exit 0
fi

for script in ${SCRIPTS}; do
    execute_migration "${script}"
done

log_info "迁移完成"

if [ "${MODE}" = "--dry-run" ]; then
    log_warn "这是 dry-run 模式，未实际执行"
    log_info "如需执行，请使用: $0 --execute"
fi