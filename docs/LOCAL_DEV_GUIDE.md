# YPAT 本地开发启动指南

> 适用于 macOS，一站式启动后端 + 前端 + nginx 反代。

---

## 前置条件

| 依赖 | 版本 | 安装 |
|------|------|------|
| Java 8 | Zulu 8 | `/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home` |
| Maven | 3.x | `brew install maven` |
| MySQL | 8.0 | `/usr/local/mysql-8.0.27-macos11-arm64/` |
| Node.js | 18+ | `brew install node` |
| nginx | latest | `brew install nginx` |
| Redis | 7.x | `brew install redis` |

### 环境变量

```bash
export JAVA8_HOME="/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home"
export MYSQL_BIN="/usr/local/mysql-8.0.27-macos11-arm64/bin"
export PROJECT_ROOT="$HOME/.config/superpowers/worktrees/ypat-workspace/codex-ypat-keep-ui-redesign"
```

---

## 端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| Eureka | 8761 | 服务注册中心 |
| system-restapi | 9081 | 后端 API 服务（直连 MySQL） |
| system-wap | 8081 | 后端网关（通过 Feign 调 restapi） |
| nginx 反代 | 8088 | 解决跨域，前端请求走这里 |
| 前端 (Vite) | 5173 | UniApp H5 开发服务器 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |

---

## 一键启动脚本

```bash
#!/bin/bash
# 文件: scripts/start-local.sh
# 用法: bash scripts/start-local.sh

set -e
JAVA8_HOME="/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home"
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="/tmp/ypat-logs"
mkdir -p "$LOG_DIR"

echo "=========================================="
echo "  YPAT 本地开发环境启动"
echo "=========================================="

# 0. 清理旧进程
echo "[0/5] 清理旧进程..."
lsof -ti:8761,9081,8081,5173 2>/dev/null | xargs kill -9 2>/dev/null || true
sleep 2

# 1. 检查 MySQL
echo "[1/5] 检查 MySQL..."
if ! lsof -i :3306 > /dev/null 2>&1; then
    echo "  MySQL 未运行，请手动启动:"
    echo "  sudo /usr/local/mysql/support-files/mysql.server start"
    exit 1
fi
echo "  MySQL 已运行"

# 2. 检查 Redis
echo "[2/5] 检查 Redis..."
if ! lsof -i :6379 > /dev/null 2>&1; then
    echo "  启动 Redis..."
    brew services start redis
    sleep 3
fi
echo "  Redis 已运行"

# 3. 启动 nginx
echo "[3/5] 启动 nginx..."
nginx -t 2>&1 && nginx -s reload 2>&1 || nginx 2>&1
echo "  nginx 已启动 (8088)"

# 4. 启动后端服务 (顺序很重要!)
echo "[4/5] 启动后端服务..."

echo "  启动 Eureka (8761)..."
cd "$PROJECT_ROOT"
$JAVA8_HOME/bin/java -jar backend-base/base-eureka/target/base-eureka-1.0-SNAPSHOT.jar \
    > "$LOG_DIR/eureka.log" 2>&1 &
EUREKA_PID=$!

# 等待 Eureka 就绪
for i in $(seq 1 12); do
    if curl -s http://127.0.0.1:8761/health > /dev/null 2>&1; then
        echo "  Eureka 就绪 ✓"
        break
    fi
    sleep 5
done

echo "  启动 system-restapi (9081)..."
$JAVA8_HOME/bin/java -jar backend/system-restapi/target/system-restapi-1.0-SNAPSHOT.jar \
    --server.port=9081 \
    --eureka.instance.ip-address=127.0.0.1 \
    --eureka.instance.hostname=localhost \
    > "$LOG_DIR/restapi.log" 2>&1 &
RESTAPI_PID=$!

# 等待 restapi 就绪
for i in $(seq 1 24); do
    if curl -s http://127.0.0.1:9081/health > /dev/null 2>&1; then
        echo "  system-restapi 就绪 ✓"
        break
    fi
    sleep 5
done

echo "  启动 system-wap (8081)..."
$JAVA8_HOME/bin/java -jar backend/system-wap/target/system-wap-1.0-SNAPSHOT.jar \
    --server.port=8081 \
    --eureka.instance.ip-address=127.0.0.1 \
    --eureka.instance.hostname=localhost \
    > "$LOG_DIR/wap.log" 2>&1 &
WAP_PID=$!

# 等待 wap 就绪
for i in $(seq 1 24); do
    if curl -s http://127.0.0.1:8081/health > /dev/null 2>&1; then
        echo "  system-wap 就绪 ✓"
        break
    fi
    sleep 5
done

# 5. 验证 API 链路
echo "[5/5] 验证 API 链路..."
RESULT=$(curl -s --max-time 10 'http://127.0.0.1:8088/banner/list?page=0&size=5&status=1')
if echo "$RESULT" | grep -q '"code":200'; then
    echo "  API 链路正常 ✓"
else
    echo "  API 链路异常: $RESULT"
fi

echo ""
echo "=========================================="
echo "  所有服务启动完成!"
echo "=========================================="
echo ""
echo "  后端 API:  http://localhost:8088"
echo "  Eureka:    http://localhost:8761"
echo ""
echo "  启动前端:  cd frontend && npm run dev:h5"
echo "  前端访问:  http://localhost:5173"
echo ""
echo "  日志目录:  $LOG_DIR/"
echo "  停止服务:  kill $EUREKA_PID $RESTAPI_PID $WAP_PID"
echo "=========================================="

# 保存 PID 方便停止
echo "$EUREKA_PID $RESTAPI_PID $WAP_PID" > /tmp/ypat-pids.txt
```

---

## 手动启动步骤

### Step 1: 检查基础设施

```bash
# 检查 MySQL
lsof -i :3306
# 检查 Redis
lsof -i :6379
# 启动 Redis (如果没运行)
brew services start redis
```

### Step 2: 启动 nginx 反代

```bash
nginx -t && nginx -s reload || nginx
```

nginx 配置 (在 `/opt/homebrew/etc/nginx/nginx.conf` 中已配置):

```nginx
server {
    listen 8088;
    server_name localhost;

    add_header Access-Control-Allow-Origin $http_origin always;
    add_header Access-Control-Allow-Credentials true always;
    add_header Access-Control-Allow-Methods 'GET, POST, PUT, DELETE, OPTIONS' always;
    add_header Access-Control-Allow-Headers '*' always;

    if ($request_method = 'OPTIONS') {
        return 204;
    }

    location / {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Step 3: 启动后端 (顺序很重要!)

```bash
export JAVA8_HOME="/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home"
cd $PROJECT_ROOT

# 3.1 Eureka (必须第一个启动)
$JAVA8_HOME/bin/java -jar backend-base/base-eureka/target/base-eureka-1.0-SNAPSHOT.jar
# 等待看到 "Started EurekaApplication"

# 3.2 system-restapi (第二个, 直连 MySQL)
$JAVA8_HOME/bin/java -jar backend/system-restapi/target/system-restapi-1.0-SNAPSHOT.jar \
    --server.port=9081 \
    --eureka.instance.ip-address=127.0.0.1 \
    --eureka.instance.hostname=localhost
# 等待看到 "Started SystemRestApiApplication"

# 3.3 system-wap (第三个, 通过 Feign 调 restapi)
$JAVA8_HOME/bin/java -jar backend/system-wap/target/system-wap-1.0-SNAPSHOT.jar \
    --server.port=8081 \
    --eureka.instance.ip-address=127.0.0.1 \
    --eureka.instance.hostname=localhost
# 等待看到 "Started SystemWapApplication"
```

### Step 4: 验证

```bash
# 检查 Eureka 注册
curl -s http://127.0.0.1:8761/eureka/apps | grep -E '<name>|<ipAddr>'

# 测试 API (通过 nginx)
curl -s 'http://127.0.0.1:8088/banner/list?page=0&size=5&status=1'
# 期望: {"code":200,"msg":"成功",...}
```

### Step 5: 启动前端

```bash
cd frontend
npm run dev:h5
# 浏览器打开 http://localhost:5173
```

---

## 常见问题排查

### 1. 启动报错: `/logs/system-restapi.log` No such file or directory

**原因**: logback.xml 硬编码了 `/logs` 目录，macOS 根目录无写权限。

**修复**: 修改 JAR 内的 `BOOT-INF/classes/logback.xml`:

```xml
<!-- 将 -->
<property name="logback.logdir" value="/logs" />
<!-- 改为 -->
<property name="logback.logdir" value="/tmp/ypat-logs" />
```

用 Python 一键修补:

```python
import zipfile
for jar in ['backend/system-restapi/target/system-restapi-1.0-SNAPSHOT.jar',
            'backend/system-wap/target/system-wap-1.0-SNAPSHOT.jar']:
    with zipfile.ZipFile(jar, 'r') as zin:
        entries = {}
        for item in zin.infolist():
            data = zin.read(item.filename)
            if item.filename == 'BOOT-INF/classes/logback.xml':
                data = data.decode('utf-8').replace(
                    'value="/logs"', 'value="/tmp/ypat-logs"'
                ).encode('utf-8')
            entries[item.filename] = (data, item)
    with zipfile.ZipFile(jar, 'w', zipfile.ZIP_DEFLATED) as zout:
        for fname, (data, item) in entries.items():
            zout.writestr(item, data)
```

### 2. MySQL 连接被拒绝: Access denied for user 'root'@'localhost'

**原因**: JAR 内 `application.yml` 的密码与实际 MySQL 密码不一致。

**修复**: 修改 JAR 内 `BOOT-INF/classes/application.yml` 中的密码字段:

```yaml
spring:
  datasource:
    password: Li123456.  # 实际密码
```

### 3. Feign 超时: Read timed out executing POST http://SYSTEM-API/...

**原因**: Eureka 注册了 VPN/WiFi IP (如 172.20.10.4)，wap 通过该 IP 连 restapi 超时。

**修复**: 启动时强制指定 IP:

```bash
--eureka.instance.ip-address=127.0.0.1 --eureka.instance.hostname=localhost
```

### 4. Load balancer does not have available server for client: SYSTEM-API

**原因**: restapi 还没注册到 Eureka，wap 就启动了。

**修复**: 严格按顺序启动，等每个服务完全就绪后再启动下一个。

### 5. 前端请求被 CORS 拦截

**原因**: 前端直连 8081 端口，浏览器拦截跨域。

**修复**: 前端 `.env.development` 必须指向 nginx 反代:

```
VITE_API_BASE_URL=http://localhost:8088
VITE_IMAGE_BASE_URL=http://localhost:8088/
```

### 6. 编译报错: Record 类冲突 (Java 16+)

**原因**: Java 16+ 引入了 `java.lang.Record`，与项目中同名实体冲突。

**修复**: 在使用 `Record` 的文件中，将显式 import 放在通配符 import 前面:

```java
import com.ypat.entity.Record;  // 显式优先
import com.ypat.entity.*;       // 通配符
```

---

## 停止所有服务

```bash
# 方法1: 使用保存的 PID
kill $(cat /tmp/ypat-pids.txt)

# 方法2: 按端口杀
lsof -ti:8761,9081,8081,5173 | xargs kill -9

# 方法3: 杀所有 Java 进程 (慎用)
pkill -f "base-eureka"
pkill -f "system-restapi"
pkill -f "system-wap"
```

---

## 日志查看

```bash
# 实时查看
tail -f /tmp/ypat-logs/eureka.log
tail -f /tmp/ypat-logs/restapi.log
tail -f /tmp/ypat-logs/wap.log

# 搜索错误
grep -i error /tmp/ypat-logs/restapi.log | tail -20
```

---

## 重新编译

只有修改了 Java 源码才需要重新编译。前端修改自动热更新。

```bash
# 用 Java 17 编译 (兼容性更好)
export JAVA_HOME="/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home"
cd $PROJECT_ROOT

# 编译后端
cd backend-base && mvn clean package -DskipTests && cd ..
cd backend && mvn clean package -DskipTests && cd ..

# 编译后重新修补 JAR (logback + 密码) 再启动
```
