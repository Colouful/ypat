#!/usr/bin/env bash
set -euo pipefail

# 获取 FastDFS 镜像 digest
# 使用方式：./get-fastdfs-digest.sh

CONTAINER_NAME="ypat-fastdfs-tracker"

log_info() { echo "[INFO] $1"; }
log_error() { echo "[ERROR] $1"; }

if docker inspect "${CONTAINER_NAME}" &> /dev/null; then
    IMAGE=$(docker inspect "${CONTAINER_NAME}" --format '{{.Config.Image}}')
    DIGEST=$(docker inspect "${CONTAINER_NAME}" --format '{{.Image}}')
    log_info "镜像: ${IMAGE}"
    log_info "Digest: ${DIGEST}"
    echo
    echo "在 docker-compose.staging.yml 中使用："
    echo "YPAT_FASTDFS_IMAGE=${IMAGE}@${DIGEST}"
else
    log_error "容器 ${CONTAINER_NAME} 不存在，请先部署 FastDFS"
    exit 1
fi