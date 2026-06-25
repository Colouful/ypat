#!/usr/bin/env bash
set -euo pipefail

##############################################################################
# YPAT 本地一键启动脚本
# 用法: bash scripts/start-local.sh
##############################################################################

JAVA_HOME_8="/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home"
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"
JAR_DIR="$BACKEND_DIR"

EUREKA_PORT=8761
RESTAPI_PORT=9081
WAP_PORT=8081
FRONTEND_PORT=5189
NGINX_PORT=8088

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log()  { echo -e "${GREEN}[YPAT]${NC} $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
err()  { echo -e "${RED}[ERROR]${NC} $*"; }

# ---------- 1. 清理旧进程 ----------
log "清理占用端口的旧进程..."
for PORT in $EUREKA_PORT $RESTAPI_PORT $WAP_PORT $FRONTEND_PORT; do
  PID=$(lsof -ti :"$PORT" 2>/dev/null || true)
  if [ -n "$PID" ]; then
    warn "端口 $PORT 被进程 $PID 占用，正在终止..."
    kill -9 "$PID" 2>/dev/null || true
    sleep 1
  fi
done
log "旧进程清理完毕"

# ---------- 2. 检查依赖服务 ----------
log "检查 MySQL..."
if ! mysqladmin ping -h 127.0.0.1 --silent 2>/dev/null; then
  err "MySQL 未运行，请先启动 MySQL"
  exit 1
fi
log "MySQL ✓"

log "检查 Redis..."
if ! redis-cli ping 2>/dev/null | grep -q PONG; then
  err "Redis 未运行，请先启动 Redis"
  exit 1
fi
log "Redis ✓"

# ---------- 3. 启动 nginx (8088) ----------
log "检查 nginx (端口 $NGINX_PORT)..."
if lsof -ti :"$NGINX_PORT" >/dev/null 2>&1; then
  log "nginx 已在端口 $NGINX_PORT 运行"
else
  if command -v nginx >/dev/null 2>&1; then
    nginx
    log "nginx 已启动 (端口 $NGINX_PORT)"
  else
    warn "未找到 nginx，跳过（请确保 $NGINX_PORT 端口有反代服务）"
  fi
fi

# ---------- 4. 查找 JAR 包 ----------
find_jar() {
  local module="$1"
  local jar
  jar=$(find "$BACKEND_DIR/$module/target" -maxdepth 1 -name "*.jar" \
        ! -name "*-sources.jar" 2>/dev/null | head -1)
  if [ -z "$jar" ]; then
    err "找不到 $module 的 JAR，请先执行: cd backend && mvn clean package -DskipTests"
    exit 1
  fi
  echo "$jar"
}

EUREKA_JAR=$(find_jar "eureka-server")
RESTAPI_JAR=$(find_jar "system-restapi")
WAP_JAR=$(find_jar "system-wap")

# ---------- 5. 启动后端服务 ----------
JAVA_CMD="$JAVA_HOME_8/bin/java"
if [ ! -x "$JAVA_CMD" ]; then
  err "找不到 Java 8: $JAVA_CMD"
  exit 1
fi

EUREKA_OPTS="--eureka.instance.ip-address=127.0.0.1 --eureka.instance.hostname=localhost"
LOG_DIR="$PROJECT_ROOT/logs"
mkdir -p "$LOG_DIR"

log "启动 Eureka Server (端口 $EUREKA_PORT)..."
nohup "$JAVA_CMD" -jar "$EUREKA_JAR" \
  > "$LOG_DIR/eureka.log" 2>&1 &
EUREKA_PID=$!
log "Eureka PID: $EUREKA_PID，等待就绪..."

# 等待 Eureka 启动
for i in $(seq 1 60); do
  if curl -sf "http://localhost:$EUREKA_PORT/actuator/health" >/dev/null 2>&1 || \
     curl -sf "http://localhost:$EUREKA_PORT/" >/dev/null 2>&1; then
    log "Eureka 已就绪"
    break
  fi
  if ! kill -0 "$EUREKA_PID" 2>/dev/null; then
    err "Eureka 启动失败，查看日志: $LOG_DIR/eureka.log"
    exit 1
  fi
  sleep 2
done

log "启动 system-restapi (端口 $RESTAPI_PORT)..."
nohup "$JAVA_CMD" -jar "$RESTAPI_JAR" \
  $EUREKA_OPTS \
  > "$LOG_DIR/restapi.log" 2>&1 &
RESTAPI_PID=$!
log "restapi PID: $RESTAPI_PID，等待就绪..."

for i in $(seq 1 90); do
  if curl -sf "http://localhost:$RESTAPI_PORT/actuator/health" >/dev/null 2>&1; then
    log "system-restapi 已就绪"
    break
  fi
  if ! kill -0 "$RESTAPI_PID" 2>/dev/null; then
    err "system-restapi 启动失败，查看日志: $LOG_DIR/restapi.log"
    exit 1
  fi
  sleep 2
done

log "启动 system-wap (端口 $WAP_PORT)..."
nohup "$JAVA_CMD" -jar "$WAP_JAR" \
  $EUREKA_OPTS \
  > "$LOG_DIR/wap.log" 2>&1 &
WAP_PID=$!
log "wap PID: $WAP_PID，等待就绪..."

for i in $(seq 1 90); do
  if curl -sf "http://localhost:$WAP_PORT/actuator/health" >/dev/null 2>&1; then
    log "system-wap 已就绪"
    break
  fi
  if ! kill -0 "$WAP_PID" 2>/dev/null; then
    err "system-wap 启动失败，查看日志: $LOG_DIR/wap.log"
    exit 1
  fi
  sleep 2
done

# ---------- 6. 验证 API 链路 ----------
log "验证 API 链路..."
if curl -sf "http://localhost:$NGINX_PORT/" >/dev/null 2>&1; then
  log "nginx 反代 ✓"
else
  warn "nginx 反代可能未配置，但后端服务已启动"
fi

echo ""
log "=========================================="
log "  后端服务全部启动完毕！"
log "=========================================="
log "  Eureka:      http://localhost:$EUREKA_PORT"
log "  restapi:     http://localhost:$RESTAPI_PORT"
log "  wap:         http://localhost:$WAP_PORT"
log "  nginx 反代:  http://localhost:$NGINX_PORT"
log ""
log "  进程 PID: Eureka=$EUREKA_PID, restapi=$RESTAPI_PID, wap=$WAP_PID"
log "  日志目录: $LOG_DIR"
echo ""
warn "请手动启动前端:"
echo "  cd frontend && npm run dev:h5"
echo "  访问 http://localhost:$FRONTEND_PORT"
