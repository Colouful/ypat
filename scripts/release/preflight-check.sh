#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ENV_FILE="$ROOT_DIR/frontend/.env.production"
DIST_DIR="$ROOT_DIR/frontend/dist"

SKIP_BUILD=0

usage() {
  cat <<'USAGE'
用法: scripts/release/preflight-check.sh [--skip-build]

检查生产发布前的配置、构建产物和敏感信息。当前正式 HTTPS 域名未配置时会返回非 0。
USAGE
}

for arg in "$@"; do
  case "$arg" in
    --skip-build)
      SKIP_BUILD=1
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "未知参数: $arg" >&2
      usage >&2
      exit 2
      ;;
  esac
done

fail() {
  echo "PRE_FLIGHT_FAIL: $*" >&2
  exit 1
}

info() {
  echo "PRE_FLIGHT: $*"
}

[[ -f "$ENV_FILE" ]] || fail "缺少 frontend/.env.production"

if grep -nE 'localhost|127\.0\.0\.1' "$ENV_FILE"; then
  fail "frontend/.env.production 包含 localhost 或 127.0.0.1"
fi

if grep -nE 'http://[0-9]+(\.[0-9]+){3}(:[0-9]+)?' "$ENV_FILE"; then
  fail "frontend/.env.production 包含 HTTP IP，正式发布必须使用 HTTPS 域名"
fi

if grep -nE '^VITE_(API|IMAGE)_BASE_URL=http://' "$ENV_FILE"; then
  fail "frontend/.env.production 的 API 或图片地址不是 HTTPS"
fi

if [[ "$SKIP_BUILD" -eq 0 ]]; then
  info "执行前端检查和构建"
  (cd "$ROOT_DIR/frontend" && pnpm install --frozen-lockfile --registry=https://registry.npmjs.org)
  (cd "$ROOT_DIR/frontend" && pnpm run check)
  (cd "$ROOT_DIR/frontend" && pnpm run build:h5)
  (cd "$ROOT_DIR/frontend" && pnpm run build:mp-weixin)

  info "执行后端测试"
  (cd "$ROOT_DIR/backend" && mvn test)
fi

if [[ -d "$DIST_DIR" ]]; then
  if grep -RIn '82\.156\.14\.216' "$DIST_DIR"; then
    fail "构建产物包含历史 HTTP IP"
  fi
  if grep -RInE 'localhost|127\.0\.0\.1' "$DIST_DIR"; then
    fail "构建产物包含 localhost 或 127.0.0.1"
  fi
  if grep -RIn 'http://' "$DIST_DIR" \
    --exclude='*.map' \
    --exclude='*.png' \
    --exclude='*.jpg' \
    --exclude='*.jpeg' \
    --exclude='*.gif' \
    --exclude='*.webp' \
    | grep -vE 'http://www\.w3\.org|http://www\.w3\.org/2000/svg'; then
    fail "构建产物包含非预期 http:// 地址"
  fi
fi

if grep -RInE 'BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY' \
  "$ROOT_DIR/frontend" "$ROOT_DIR/backend" "$ROOT_DIR/backend-base" "$ROOT_DIR/.github" "$ROOT_DIR/scripts" "$ROOT_DIR/docs" \
  --exclude-dir=node_modules --exclude-dir=target --exclude-dir=dist; then
  fail "仓库包含私钥内容"
fi

KNOWN_PATTERNS=(
  "WNFSIDH""FIOWEF"
  "Li""123456\\."
  "DY""X~!@#\\$""123qwe"
  "tc""123456"
  "sso""secret0"
  "sso""secret1"
  "FastDFS""1234567890"
)

for pattern in "${KNOWN_PATTERNS[@]}"; do
  if grep -RInE "$pattern" \
    "$ROOT_DIR/frontend" "$ROOT_DIR/backend" "$ROOT_DIR/backend-base" "$ROOT_DIR/.github" "$ROOT_DIR/scripts" "$ROOT_DIR/docs" "$ROOT_DIR/docker-compose.yml" \
    --exclude-dir=node_modules --exclude-dir=target --exclude-dir=dist; then
    fail "仓库包含已知历史密钥或密码"
  fi
done

info "检查通过"
