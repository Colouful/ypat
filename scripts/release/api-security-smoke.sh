#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-${YPAT_API_BASE_URL:-http://localhost:8081}}"
TOKEN="${YPAT_TEST_TOKEN:-}"

PUBLIC_ENDPOINTS=(
  "/ypat/tc/list"
  "/ypat/zx/list"
  "/ypat/get?id=1"
  "/banner/list"
  "/article/list"
  "/area/list"
  "/tmplid/list"
  "/param/list"
  "/product/list"
)

PRIVATE_ENDPOINTS=(
  "/user/get?id=1"
  "/my/ypat/send/list?page=0&size=10"
  "/my/ypat/rec/list?page=0&size=10"
  "/mess/get?id=1"
  "/feedback/add"
  "/oauth/get"
  "/order/get?id=1"
  "/order/findPage?page=0&size=10"
  "/bill/findPage?page=0&size=10"
  "/record/findPage?page=0&size=10"
  "/user/token"
  "/manage/"
)

status_code() {
  local method="$1"
  local path="$2"
  local token="${3:-}"
  if [[ -n "$token" ]]; then
    curl -sS -o /tmp/ypat-smoke-body.txt -w '%{http_code}' -X "$method" -H "Token: $token" "$BASE_URL$path"
  elif [[ "$path" == "/feedback/add" ]]; then
    curl -sS -o /tmp/ypat-smoke-body.txt -w '%{http_code}' -X "$method" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      --data "content=valid-feedback-content&contact=test@example.invalid" \
      "$BASE_URL$path"
  else
    curl -sS -o /tmp/ypat-smoke-body.txt -w '%{http_code}' -X "$method" "$BASE_URL$path"
  fi
}

business_code() {
  sed -n 's/.*"code"[ ]*:[ ]*"\{0,1\}\([^",}]*\).*/\1/p' /tmp/ypat-smoke-body.txt | head -1
}

echo "# API 安全冒烟检查"
echo "BASE_URL=$BASE_URL"

for endpoint in "${PUBLIC_ENDPOINTS[@]}"; do
  code="$(status_code GET "$endpoint")"
  biz="$(business_code)"
  echo "PUBLIC GET $endpoint -> http:$code code:${biz:-N/A}"
  if [[ "$code" == "401" || "$code" == "403" || "$biz" == "401" || "$biz" == "1001" ]]; then
    echo "公开接口被拒绝: $endpoint" >&2
    exit 1
  fi
done

for endpoint in "${PRIVATE_ENDPOINTS[@]}"; do
  method="GET"
  [[ "$endpoint" == /feedback/add* || "$endpoint" == "/user/token" ]] && method="POST"
  code="$(status_code "$method" "$endpoint")"
  biz="$(business_code)"
  echo "PRIVATE $method $endpoint anonymous -> http:$code code:${biz:-N/A}"
  if [[ "$code" != "401" && "$code" != "403" && "$code" != "302" && "$biz" != "401" && "$biz" != "1001" ]]; then
    echo "私有接口未拒绝匿名访问: $endpoint" >&2
    exit 1
  fi
done

if [[ -n "$TOKEN" ]]; then
  code="$(status_code POST "/user/token" "$TOKEN")"
  biz="$(business_code)"
  echo "PRIVATE POST /user/token authenticated -> http:$code code:${biz:-N/A}"
  if [[ "$code" == "401" || "$code" == "403" ]]; then
    echo "有效测试 Token 无法刷新" >&2
    exit 1
  fi
else
  echo "未设置 YPAT_TEST_TOKEN，跳过有效 Token 刷新正向检查。"
fi
