#!/bin/sh
set -eu

MODULE="${1:?module name is required}"
INTERVAL="${HOT_RELOAD_INTERVAL:-2}"
LOCK_DIR="${MAVEN_LOCK_DIR:-/root/.m2/ypat-hot-compile.lock}"
MAVEN_ARGS="-Pdev -DskipTests -B"
APP_PID=""

case "$MODULE" in
  system-restapi|system-wap)
    WATCH_PATHS="pom.xml system-object/pom.xml system-domain/pom.xml ${MODULE}/pom.xml system-object/src/main system-domain/src/main ${MODULE}/src/main"
    ;;
  system-web)
    WATCH_PATHS="pom.xml system-object/pom.xml ${MODULE}/pom.xml system-object/src/main ${MODULE}/src/main"
    ;;
  *)
    echo "不支持的模块: ${MODULE}" >&2
    exit 2
    ;;
esac

cleanup() {
  if [ -n "$APP_PID" ] && kill -0 "$APP_PID" 2>/dev/null; then
    kill "$APP_PID" 2>/dev/null || true
    wait "$APP_PID" 2>/dev/null || true
  fi
  rmdir "$LOCK_DIR" 2>/dev/null || true
}

trap cleanup INT TERM EXIT

signature() {
  for path in $WATCH_PATHS; do
    [ -e "$path" ] || continue
    if [ -d "$path" ]; then
      find "$path" -type f \( \
        -name '*.java' -o \
        -name '*.xml' -o \
        -name '*.properties' -o \
        -name '*.yml' -o \
        -name '*.yaml' \
      \) -print
    else
      printf '%s\n' "$path"
    fi
  done | sort | xargs -r sha256sum | sha256sum | awk '{print $1}'
}

with_compile_lock() {
  while ! mkdir "$LOCK_DIR" 2>/dev/null; do
    echo "[hot-run] 等待 Maven 编译锁: ${LOCK_DIR}"
    sleep 1
  done

  set +e
  "$@"
  status=$?
  set -e

  rmdir "$LOCK_DIR" 2>/dev/null || true
  return "$status"
}

compile_module() {
  echo "[hot-run] 编译 ${MODULE}"
  with_compile_lock mvn -pl "$MODULE" -am compile $MAVEN_ARGS
}

start_module() {
  echo "[hot-run] 使用 spring-boot:run 启动 ${MODULE}"
  mvn -pl "$MODULE" -am spring-boot:run $MAVEN_ARGS &
  APP_PID="$!"
}

compile_module
LAST_SIGNATURE="$(signature)"
start_module

while kill -0 "$APP_PID" 2>/dev/null; do
  sleep "$INTERVAL"
  NEXT_SIGNATURE="$(signature)"
  if [ "$NEXT_SIGNATURE" != "$LAST_SIGNATURE" ]; then
    echo "[hot-run] 检测到 ${MODULE} 文件变化"
    if compile_module; then
      LAST_SIGNATURE="$NEXT_SIGNATURE"
    else
      echo "[hot-run] 编译失败，${MODULE} 继续使用上一次成功编译的 class 文件运行" >&2
    fi
  fi
done

wait "$APP_PID"
