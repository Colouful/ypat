#!/usr/bin/env bash
set -euo pipefail

# YPAT production 回滚脚本（TEMPLATE — 禁止自动执行）
#
# 必须显式 --confirm-rollback

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

CONFIRM_ROLLBACK=0
ROLLBACK_TO=""
APPROVAL_TICKET=""
ROLLBACK_OWNER=""

while [ $# -gt 0 ]; do
  case "$1" in
    --confirm-rollback) CONFIRM_ROLLBACK=1; shift ;;
    --rollback-to) ROLLBACK_TO="${2:?}"; shift 2 ;;
    --approval-ticket) APPROVAL_TICKET="${2:?}"; shift 2 ;;
    --rollback-owner) ROLLBACK_OWNER="${2:?}"; shift 2 ;;
    -h|--help)
      grep "^# " "${BASH_SOURCE[0]}" | head -10
      exit 0
      ;;
    *) log_error "未知选项: $1"; exit 1 ;;
  esac
done

if [ "${CONFIRM_ROLLBACK}" -ne 1 ]; then
  log_error "必须 --confirm-rollback 显式确认"
  exit 1
fi

if [ -z "${ROLLBACK_TO}" ] || [ -z "${APPROVAL_TICKET}" ] || [ -z "${ROLLBACK_OWNER}" ]; then
  log_error "必须 --rollback-to --approval-ticket --rollback-owner"
  exit 1
fi

log_warn "PRODUCTION ROLLBACK PLACEHOLDER"
log_warn "实际步骤（待实现）:"
log_warn "  1. 停止当前流量入口"
log_warn "  2. 数据库：仅当确认有 schema 变更时手动回滚 schema（不自动）"
log_warn "  3. 滚动更新 application 到 --rollback-to 镜像"
log_warn "  4. 健康检查 + 业务冒烟"
log_warn "  5. 通知业务方 + 记录事故报告"