#!/usr/bin/env bash
set -euo pipefail

# TLS 证书有效期检查脚本
# 用途：检查 SSL 证书是否即将过期

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 检查证书
check_cert() {
    local cert_file=$1
    local cert_name=$2

    if [ ! -f "${cert_file}" ]; then
        log_error "证书文件不存在: ${cert_file}"
        exit 1
    fi

    log_info "检查证书: ${cert_name}"

    # 获取证书信息
    SUBJECT=$(openssl x509 -in "${cert_file}" -noout -subject -nameopt compat)
    ISSUER=$(openssl x509 -in "${cert_file}" -noout -issuer -nameopt compat)
    NOT_BEFORE=$(openssl x509 -in "${cert_file}" -noout -startdate -date '+%Y-%m-%d %H:%M:%S')
    NOT_AFTER=$(openssl x509 -in "${cert_file}" -noout -enddate -date '+%Y-%m-%d %H:%M:%S')

    # 检查 SAN（Subject Alternative Name）
    SAN=$(openssl x509 -in "${cert_file}" -noout -ext subjectAltName 2>/dev/null | grep -A1 "Subject Alternative Name" | tail -1 || echo "无")

    echo "  Subject: ${SUBJECT}"
    echo "  Issuer: ${ISSUER}"
    echo "  生效时间: ${NOT_BEFORE}"
    echo "  过期时间: ${NOT_AFTER}"
    echo "  SAN: ${SAN}"
    echo

    # 计算剩余天数
    NOT_AFTER_EPOCH=$(openssl x509 -in "${cert_file}" -noout -enddate -date +%s 2>/dev/null)
    CURRENT_EPOCH=$(date +%s)
    REMAINING_DAYS=$(( ($NOT_AFTER_EPOCH - $CURRENT_EPOCH) / 86400 ))

    echo "  剩余天数: ${REMAINING_DAYS} 天"

    # 检查域名
    if echo "${SAN}" | grep -q "panghu.work"; then
        log_info "  ✓ panghu.work 在 SAN 中"
    else
        log_warn "  ✗ panghu.work 不在 SAN 中"
    fi

    if echo "${SAN}" | grep -q "www.panghu.work"; then
        log_info "  ✓ www.panghu.work 在 SAN 中"
    else
        log_warn "  ✗ www.panghu.work 不在 SAN 中"
    fi

    # 判断风险级别
    if [ ${REMAINING_DAYS} -lt 7 ]; then
        log_error "  ⚠️  证书将在 7 天内过期！"
        return 3
    elif [ ${REMAINING_DAYS} -lt 15 ]; then
        log_error "  ⚠️  证书将在 15 天内过期！"
        return 2
    elif [ ${REMAINING_DAYS} -lt 30 ]; then
        log_warn "  ⚠️  证书将在 30 天内过期"
        return 1
    else
        log_info "  ✓ 证书有效期正常"
        return 0
    fi
}

# 主程序
CERT_FILE="/etc/nginx/ssl/panghu.work/panghu.work_bundle.crt"
CERT_NAME="panghu.work"

if [ -f "${CERT_FILE}" ]; then
    check_cert "${CERT_FILE}" "${CERT_NAME}"
    exit $?
else
    log_warn "证书文件不存在: ${CERT_FILE}"
    log_info "请在服务器上运行此脚本"
    exit 1
fi