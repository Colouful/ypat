# YPAT 爱去拍 — 完整部署指南

> **版本**: 1.0 | **最后更新**: 2026-06-26
> 本文档覆盖本地开发、Docker 部署、生产环境部署的全部流程，按 codefather 标准编写：每步有 WHY 解释，代码块可直接复制执行。

---

## 目录

- [一、项目架构说明](#一项目架构说明)
  - [1.1 技术栈](#11-技术栈)
  - [1.2 服务架构图](#12-服务架构图)
  - [1.3 端口分配表](#13-端口分配表)
  - [1.4 项目目录结构](#14-项目目录结构)
- [二、环境准备](#二环境准备)
  - [2.1 本地开发环境要求](#21-本地开发环境要求)
  - [2.2 服务器环境要求](#22-服务器环境要求)
  - [2.3 一键检查脚本](#23-一键检查脚本)
- [三、本地开发部署](#三本地开发部署)
  - [3.1 Docker 方式（推荐）](#31-docker-方式推荐)
  - [3.2 非 Docker 方式](#32-非-docker-方式)
  - [3.3 前端开发模式](#33-前端开发模式)
- [四、编译构建](#四编译构建)
  - [4.1 后端编译](#41-后端编译)
  - [4.2 前端构建](#42-前端构建)
  - [4.3 Docker 镜像构建](#43-docker-镜像构建)
- [五、生产部署](#五生产部署)
  - [5.1 服务器初始化](#51-服务器初始化)
  - [5.2 Docker Compose 生产部署](#52-docker-compose-生产部署)
  - [5.3 域名和 HTTPS 配置](#53-域名和-https-配置)
  - [5.4 日志管理](#54-日志管理)
  - [5.5 数据备份](#55-数据备份)
- [六、常见问题排查](#六常见问题排查)
- [七、生产最佳实践](#七生产最佳实践)
  - [7.1 安全加固](#71-安全加固)
  - [7.2 监控告警](#72-监控告警)
  - [7.3 日志轮转](#73-日志轮转)
  - [7.4 数据备份策略](#74-数据备份策略)

---

## 一、项目架构说明

### 1.1 技术栈

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **后端框架** | Spring Boot | 1.5.x | 微服务基座 |
| **微服务治理** | Spring Cloud (Eureka + Feign + Hystrix) | Dalston/Edgware | 服务注册/调用/熔断 |
| **ORM** | Spring Data JPA + Hibernate | — | 自动建表 (ddl-auto=update) |
| **数据库** | MySQL | 8.0 | 主存储，utf8mb4 编码 |
| **缓存** | Redis | 7.x | Session + 业务缓存 |
| **管理后台** | Spring Boot + Thymeleaf | 1.5.x | 服务端渲染管理界面 |
| **前端框架** | UniApp 3.x + Vue 3 + TypeScript | — | H5 / 微信小程序 |
| **反向代理** | nginx | alpine | API 反代 + 静态资源 |
| **容器化** | Docker + Docker Compose | 20.10+ / 2.0+ | 一键编排 |

> **WHY**: 项目采用 Spring Cloud 微服务架构，Eureka 负责服务发现，Feign 负责服务间调用。前端通过 nginx 反代解决跨域问题。MySQL 8.0 需要 `mysql_native_password` 插件兼容老版 JDBC 驱动。

### 1.2 服务架构图

```
                    ┌─────────────────────────────────────────────────────┐
                    │                    用户浏览器                        │
                    └────────────┬────────────────────┬───────────────────┘
                                 │ :5189              │ :8088
                    ┌────────────▼─────────┐  ┌───────▼────────────────┐
                    │   nginx (前端静态)     │  │  nginx (API 反代)       │
                    │   H5 用户页面          │  │  解决 CORS 跨域         │
                    └──────────────────────┘  └───────┬────────────────┘
                                                      │ proxy_pass
                                                      ▼
                    ┌─────────────────────────────────────────────────────┐
                    │            system-wap (:8081)                       │
                    │            后端网关 — 通过 Feign 调用 restapi         │
                    └─────────────────────┬───────────────────────────────┘
                                          │ Feign (通过 Eureka 服务发现)
                                          ▼
                    ┌─────────────────────────────────────────────────────┐
                    │         system-restapi (:9081)                      │
                    │         后端 API — 直连 MySQL / Redis                │
                    └──────────┬─────────────────┬────────────────────────┘
                               │ JDBC             │
                    ┌──────────▼───────┐  ┌───────▼──────────────────────┐
                    │  MySQL (:3307)   │  │  Redis (:6379)               │
                    │  主存储           │  │  缓存 + Session              │
                    └──────────────────┘  └──────────────────────────────┘

                    ┌─────────────────────────────────────────────────────┐
                    │            Eureka (:8761)                           │
                    │            服务注册中心                              │
                    │  restapi / wap / system-web 均注册到此               │
                    └─────────────────────────────────────────────────────┘

                    ┌─────────────────────────────────────────────────────┐
                    │         system-web (:8082)                          │
                    │         管理后台 — Thymeleaf + Feign 调用 restapi    │
                    └─────────────────────────────────────────────────────┘
```

**请求链路**:
```
用户浏览器 → nginx(:5189) → 静态页面
用户浏览器 → nginx(:8088) → system-wap(:8081) → Eureka 服务发现 → system-restapi(:9081) → MySQL/Redis
管理员浏览器 → system-web(:8082) → Eureka 服务发现 → system-restapi(:9081) → MySQL/Redis
```

### 1.3 端口分配表

| 服务 | 容器名 | 主机端口 | 容器端口 | 协议 | 说明 |
|------|--------|----------|----------|------|------|
| MySQL | ypat-mysql | **3307** | 3306 | TCP | 数据库（主机映射 3307 避免冲突） |
| Redis | ypat-redis | **6379** | 6379 | TCP | 缓存 |
| Eureka | ypat-eureka | **8761** | 8761 | HTTP | 服务注册中心控制台 |
| system-restapi | ypat-restapi | **9081** | 9081 | HTTP | 后端 API 服务 |
| system-wap | ypat-wap | **8081** | 8081 | HTTP | 后端网关服务 |
| system-web | ypat-system-web | **8082** | 8082 | HTTP | 管理后台 |
| nginx (API) | ypat-nginx | **8088** | 8088 | HTTP | API 反向代理 |
| nginx (前端) | ypat-nginx | **5189** | 80 | HTTP | 前端静态资源 |

> **WHY**: MySQL 映射到 3307 是因为本地开发机可能已有 MySQL 实例占用了 3306，避免端口冲突。

### 1.4 项目目录结构

```
ypat/
├── docker-compose.yml              # Docker Compose 主编排配置
├── docker/
│   ├── nginx/
│   │   └── default.conf            # nginx 配置（API 反代 + 前端静态）
│   └── mysql/
│       └── init.sql                # MySQL 初始化脚本
├── backend-base/
│   └── base-eureka/
│       ├── Dockerfile              # Eureka 镜像构建
│       ├── src/                    # Eureka 源码
│       └── target/*.jar            # 编译产物
├── backend/
│   ├── pom.xml                     # Maven 父 POM (profiles: dev / pro)
│   ├── system-restapi/
│   │   ├── Dockerfile              # restapi 镜像构建
│   │   ├── src/                    # restapi 源码
│   │   └── target/*.jar            # 编译产物
│   ├── system-wap/
│   │   ├── Dockerfile              # wap 镜像构建
│   │   ├── src/                    # wap 源码
│   │   └── target/*.jar            # 编译产物
│   └── system-web/
│       ├── Dockerfile              # 管理后台镜像构建
│       ├── src/                    # 管理后台源码
│       └── target/*.jar            # 编译产物
├── frontend/
│   ├── .env.development            # 本地开发环境变量
│   ├── .env.production             # 生产环境变量
│   ├── Dockerfile                  # 前端镜像构建
│   ├── src/                        # 前端源码
│   └── dist/build/h5/              # 前端构建产物
├── scripts/
│   └── start-local.sh              # 非 Docker 一键启动脚本
└── docs/
    └── DEPLOY_GUIDE.md             # 本文档
```

---

## 二、环境准备

### 2.1 本地开发环境要求

| 依赖 | 最低版本 | 推荐版本 | 安装方式 | 用途 |
|------|----------|----------|----------|------|
| Java 8 | 1.8 | Zulu 8 | [Azul Zulu](https://www.azul.com/downloads/) | 编译 & 运行后端 |
| Maven | 3.6+ | 3.9+ | `brew install maven` | 后端构建 |
| Node.js | 18+ | 18 LTS | `brew install node` | 前端构建 |
| Docker | 20.10+ | 24+ | [Docker Desktop](https://www.docker.com/products/docker-desktop/) | 容器运行时 |
| Docker Compose | 2.0+ | — | Docker Desktop 自带 | 服务编排 |
| Git | 2.30+ | — | `brew install git` | 代码管理 |

> **WHY**: 项目基于 Java 8 + Spring Boot 1.5，**必须使用 Java 8 编译**。Java 16+ 引入的 `java.lang.Record` 会与项目实体类冲突。前端 UniApp 3.x 需要 Node.js 18+。

### 2.2 服务器环境要求

| 项目 | 要求 | 说明 |
|------|------|------|
| 操作系统 | Ubuntu 20.04+ / CentOS 7+ | 推荐 Ubuntu |
| CPU | 2 核+ | Java 服务较吃资源 |
| 内存 | 4GB+ | MySQL + Redis + 3 个 Java 服务 + nginx |
| 磁盘 | 20GB+ | 镜像 + 数据卷 + 日志 |
| 带宽 | 6Mbps+ | 图片上传/下载需求 |
| Docker | 20.10+ | 容器运行时 |
| Docker Compose | 2.0+ | 服务编排 |

### 2.3 一键检查脚本

> **WHY**: 部署前执行此脚本，提前发现环境问题，避免部署过程中浪费时间排查。

```bash
#!/bin/bash
# 文件: scripts/check-env.sh
# 用法: bash scripts/check-env.sh
# 说明: 一键检查 YPAT 部署所需环境

set -e
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'
PASS=0
FAIL=0

check() {
    local name="$1"
    local cmd="$2"
    local expect="$3"
    if result=$(eval "$cmd" 2>/dev/null); then
        echo -e "  ${GREEN}✓${NC} $name: $result"
        ((PASS++))
    else
        echo -e "  ${RED}✗${NC} $name: 未找到 ($expect)"
        ((FAIL++))
    fi
}

echo "=========================================="
echo "  YPAT 环境检查"
echo "=========================================="
echo ""

echo "[1/6] 基础工具"
check "Git" "git --version | head -1" "git >= 2.30"
check "Docker" "docker --version | awk '{print \$3}' | tr -d ','" "docker >= 20.10"
check "Docker Compose" "docker compose version --short" "compose >= 2.0"
echo ""

echo "[2/6] Java 环境"
check "Java 8" "java -version 2>&1 | head -1 | awk -F'\"' '{print \$2}'" "1.8.x"
check "Maven" "mvn --version 2>&1 | head -1 | awk '{print \$3}'" "mvn >= 3.6"
echo ""

echo "[3/6] Node.js 环境"
check "Node.js" "node --version" "node >= 18"
check "npm" "npm --version" "npm >= 9"
echo ""

echo "[4/6] 端口检查"
for port in 3307 6379 8761 9081 8081 8082 8088 5189; do
    if lsof -i ":$port" > /dev/null 2>&1; then
        echo -e "  ${YELLOW}⚠${NC} 端口 $port 已被占用: $(lsof -i :$port -t | head -1)"
    else
        echo -e "  ${GREEN}✓${NC} 端口 $port 空闲"
    fi
done
echo ""

echo "[5/6] 磁盘空间"
AVAIL=$(df -h . | awk 'NR==2{print $4}')
echo -e "  ${GREEN}✓${NC} 可用磁盘空间: $AVAIL"
echo ""

echo "[6/6] Docker 状态"
if docker info > /dev/null 2>&1; then
    echo -e "  ${GREEN}✓${NC} Docker daemon 运行中"
else
    echo -e "  ${RED}✗${NC} Docker daemon 未运行，请先启动 Docker Desktop"
    ((FAIL++))
fi
echo ""

echo "=========================================="
echo -e "  检查完成: ${GREEN}${PASS} 通过${NC}, ${RED}${FAIL} 失败${NC}"
if [ $FAIL -gt 0 ]; then
    echo -e "  ${YELLOW}请先修复上述问题再部署${NC}"
else
    echo -e "  ${GREEN}环境就绪，可以开始部署!${NC}"
fi
echo "=========================================="
```

执行：

```bash
chmod +x scripts/check-env.sh
bash scripts/check-env.sh
```

---

## 三、本地开发部署

### 3.1 Docker 方式（推荐）

> **WHY**: Docker 方式最省心——MySQL、Redis、Eureka、后端服务、nginx 全部容器化，一键启动，不污染本机环境。

#### Step 1: 编译后端 JAR

Docker 构建镜像需要编译好的 JAR 文件，必须先在主机上完成编译：

```bash
# 编译 Eureka (服务注册中心)
cd backend-base
mvn clean package -DskipTests
cd ..

# 编译 restapi + wap + system-web (默认 dev profile)
cd backend
mvn clean package -DskipTests
cd ..
```

> **WHY**: `mvn clean package -DskipTests` 跳过测试加速编译。默认激活 `dev` profile，使用开发环境配置（localhost 连接）。编译需要 Java 8，确保 `JAVA_HOME` 指向 Java 8。

编译产物位置：
- `backend-base/base-eureka/target/base-eureka-1.0-SNAPSHOT.jar`
- `backend/system-restapi/target/system-restapi-1.0-SNAPSHOT.jar`
- `backend/system-wap/target/system-wap-1.0-SNAPSHOT.jar`
- `backend/system-web/target/system-web-1.0-SNAPSHOT.jar`

#### Step 2: 构建前端

```bash
cd frontend
npm install
npx uni build --mode development
cd ..
```

> **WHY**: 本地开发必须用 `--mode development`，这会加载 `.env.development` 中的 API 地址（`http://localhost:8088`）。如果用默认的 production mode，会触发 HTTPS 强制检查导致请求失败。

构建产物位于 `frontend/dist/build/h5/`。

#### Step 3: 检查端口

```bash
# 检查端口占用
lsof -i :3307 -i :6379 -i :8761 -i :9081 -i :8081 -i :8082 -i :8088 -i :5189

# 如有冲突，停止主机服务
# macOS:
brew services stop redis 2>/dev/null || true
brew services stop nginx 2>/dev/null || true
brew services stop mysql 2>/dev/null || true

# Linux:
# sudo systemctl stop mysql redis nginx
```

#### Step 4: 启动 Docker Compose

```bash
# 首次启动（构建镜像 + 启动容器）
docker compose up --build -d

# 后续启动（镜像已存在，直接启动）
docker compose up -d
```

> **WHY**: 首次需要 `--build` 因为 Dockerfile 需要构建镜像。`-d` 表示后台运行。启动顺序由 `depends_on` + `healthcheck` 自动保证：MySQL/Redis → Eureka → restapi → wap → nginx。

#### Step 5: 验证

```bash
# 查看所有容器状态（期望全部 healthy）
docker compose ps

# 测试 API 链路
curl -s 'http://localhost:8088/banner/list?page=0&size=5&status=1'
# 期望: {"code":200,"msg":"成功",...}

# 测试前端
curl -s -o /dev/null -w "%{http_code}" http://localhost:5189/
# 期望: 200

# 测试管理后台
curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/
# 期望: 200 或 302 (跳转登录)
```

#### 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端页面 | http://localhost:5189 | H5 用户端 |
| API 网关 | http://localhost:8088 | nginx 反代 → wap |
| 管理后台 | http://localhost:8082 | Thymeleaf 管理界面 |
| Eureka 控制台 | http://localhost:8761 | 服务注册中心 |
| MySQL | `127.0.0.1:3307` | root / <历史本地 MySQL 密码，已脱敏> |
| Redis | `127.0.0.1:6379` | 无密码 |

---

### 3.2 非 Docker 方式

> **WHY**: 如果不想用 Docker，或需要调试后端代码，可以直接在主机上运行所有服务。

#### 前置依赖

| 依赖 | 端口 | 启动方式 |
|------|------|----------|
| MySQL 8.0 | 3306 | `sudo /usr/local/mysql/support-files/mysql.server start` 或 `brew services start mysql` |
| Redis | 6379 | `brew services start redis` |
| nginx | 8088 | 配置后 `nginx` 或 `nginx -s reload` |

#### Step 1: 确保 MySQL 和 Redis 运行

```bash
# 检查 MySQL
lsof -i :3306 || echo "MySQL 未运行，请手动启动"

# 启动 Redis
brew services start redis
lsof -i :6379
```

#### Step 2: 配置 nginx 反代

在 nginx 配置文件（macOS: `/opt/homebrew/etc/nginx/nginx.conf`，Linux: `/etc/nginx/nginx.conf`）中添加：

```nginx
server {
    listen 8088;
    server_name localhost;

    # CORS 头
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

启动 nginx：

```bash
nginx -t && nginx -s reload || nginx
```

#### Step 3: 启动后端服务（顺序很重要!）

> **WHY**: Eureka 必须第一个启动，因为 restapi 和 wap 都需要注册到 Eureka。restapi 必须在 wap 之前启动，因为 wap 通过 Feign 调用 restapi。

```bash
export JAVA8_HOME="/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home"
cd /path/to/ypat

# ① 启动 Eureka（必须第一个）
$JAVA8_HOME/bin/java -jar backend-base/base-eureka/target/base-eureka-1.0-SNAPSHOT.jar &
# 等待看到 "Started EurekaApplication in" 字样，约 15-30 秒

# ② 启动 system-restapi（第二个）
$JAVA8_HOME/bin/java \
  -Dlogback.logdir=/tmp/ypat-logs \
  -jar backend/system-restapi/target/system-restapi-1.0-SNAPSHOT.jar \
  --server.port=9081 \
  --eureka.instance.ip-address=127.0.0.1 \
  --eureka.instance.hostname=localhost &

# ③ 启动 system-wap（第三个）
$JAVA8_HOME/bin/java \
  -Dlogback.logdir=/tmp/ypat-logs \
  -jar backend/system-wap/target/system-wap-1.0-SNAPSHOT.jar \
  --server.port=8081 \
  --eureka.instance.ip-address=127.0.0.1 \
  --eureka.instance.hostname=localhost &

# ④ 启动 system-web（管理后台，可选）
$JAVA8_HOME/bin/java \
  -Dlogback.logdir=/tmp/ypat-logs \
  -jar backend/system-web/target/system-web-1.0-SNAPSHOT.jar \
  --server.port=8082 \
  --eureka.instance.ip-address=127.0.0.1 \
  --eureka.instance.hostname=localhost &
```

> **WHY**: `--eureka.instance.ip-address=127.0.0.1` 是关键参数。不加的话 Eureka 会注册 VPN/WiFi 的 IP（如 172.20.10.4），导致 Feign 调用超时。`-Dlogback.logdir=/tmp/ypat-logs` 解决 macOS 根目录无写权限的问题。

#### Step 4: 验证

```bash
# 检查 Eureka 注册状态
curl -s http://127.0.0.1:8761/eureka/apps | grep -E '<name>|<ipAddr>'

# 测试 API 链路
curl -s 'http://127.0.0.1:8088/banner/list?page=0&size=5&status=1'
# 期望: {"code":200,"msg":"成功",...}
```

#### 停止所有服务

```bash
# 方法 1: 按端口杀进程
lsof -ti:8761,9081,8081,8082 | xargs kill -9

# 方法 2: 按进程名杀
pkill -f "base-eureka"
pkill -f "system-restapi"
pkill -f "system-wap"
pkill -f "system-web"
```

---

### 3.3 前端开发模式

> **WHY**: 前端开发时使用 Vite dev server 支持热更新（HMR），修改代码后浏览器自动刷新，无需每次手动构建。

```bash
cd frontend
npm install
npm run dev:h5
# 浏览器打开 http://localhost:5173
```

> **注意**: 前端 `.env.development` 中 `VITE_API_BASE_URL` 必须指向 `http://localhost:8088`（nginx 反代），不能直连 8081，否则浏览器会拦截 CORS 请求。

---

## 四、编译构建

### 4.1 后端编译

| Profile | 命令 | 用途 | 配置文件 |
|---------|------|------|----------|
| dev (默认) | `mvn clean package -DskipTests` | 本地开发 | `src/main/resources/dev/application.yml` |
| pro | `mvn clean package -DskipTests -P pro` | 生产部署 | `src/main/resources/pro/application.yml` |

```bash
# 编译 Eureka
cd backend-base
mvn clean package -DskipTests
cd ..

# 编译 restapi + wap + system-web（默认 dev）
cd backend
mvn clean package -DskipTests
cd ..

# 生产编译（加 -P pro）
cd backend-base && mvn clean package -DskipTests -P pro && cd ..
cd backend && mvn clean package -DskipTests -P pro && cd ..
```

> **WHY**: `-DskipTests` 跳过单元测试加速编译。`-P pro` 激活生产 profile，使用生产环境的数据库地址、密码等配置。

编译产物：

```
backend-base/base-eureka/target/base-eureka-1.0-SNAPSHOT.jar
backend/system-restapi/target/system-restapi-1.0-SNAPSHOT.jar
backend/system-wap/target/system-wap-1.0-SNAPSHOT.jar
backend/system-web/target/system-web-1.0-SNAPSHOT.jar
```

### 4.2 前端构建

| 模式 | 命令 | 用途 | 环境文件 |
|------|------|------|----------|
| 本地开发 | `npx uni build --mode development` | 本地 Docker 部署 | `.env.development` |
| 生产构建 | `npm run build:h5` | 生产环境部署 | `.env.production` |

```bash
cd frontend
npm install

# 本地开发构建（API 地址指向 localhost:8088）
npx uni build --mode development

# 生产构建（API 地址指向生产域名）
npm run build:h5
cd ..
```

> **WHY**: 本地必须用 `--mode development`，否则 UniApp 会检查 HTTPS 并拒绝请求。生产构建使用 `npm run build:h5`，自动加载 `.env.production` 配置。

构建产物位于 `frontend/dist/build/h5/`。

### 4.3 Docker 镜像构建

Dockerfile 统一基于 `eclipse-temurin:8-jre`，每个后端服务一个 Dockerfile：

```dockerfile
# backend/system-restapi/Dockerfile (示例)
FROM eclipse-temurin:8-jre
WORKDIR /app
RUN mkdir -p /tmp/ypat-logs
COPY target/system-restapi-1.0-SNAPSHOT.jar app.jar
EXPOSE 9081
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
```

> **WHY**: 使用 JRE 而非 JDK 可以大幅减小镜像体积。`-Djava.security.egd=file:/dev/./urandom` 加速 JVM 启动时的随机数生成。`mkdir -p /tmp/ypat-logs` 解决 logback 日志目录问题。

构建并启动：

```bash
# 一键构建所有镜像并启动
docker compose up --build -d

# 单独构建某个服务的镜像
docker compose build restapi
docker compose build wap
docker compose build system-web
```

---

## 五、生产部署

### 5.1 服务器初始化

> **WHY**: 腾讯云新服务器需要安装 Docker、配置安全组、优化系统参数，这是部署的基础。

```bash
# === 1. 系统更新 ===
sudo apt update && sudo apt upgrade -y

# === 2. 安装 Docker ===
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
# 重新登录使 docker 组生效

# === 3. 安装 Docker Compose (如未自带) ===
sudo apt install docker-compose-plugin -y
docker compose version

# === 4. 配置 Docker 镜像加速 ===
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com",
    "https://docker.mirrors.ustc.edu.cn"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "50m",
    "max-file": "3"
  }
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker

# === 5. 创建部署目录 ===
sudo mkdir -p /opt/ypat
sudo chown $USER:$USER /opt/ypat
```

**腾讯云安全组配置**:

| 方向 | 端口 | 协议 | 来源 | 说明 |
|------|------|------|------|------|
| 入站 | 22 | TCP | 你的 IP | SSH |
| 入站 | 80 | TCP | 0.0.0.0/0 | HTTP |
| 入站 | 443 | TCP | 0.0.0.0/0 | HTTPS |
| 入站 | 8082 | TCP | 你的 IP | 管理后台（限 IP） |
| 出站 | 全部 | 全部 | 0.0.0.0/0 | — |

> **WHY**: MySQL (3307) 和 Redis (6379) 不暴露到公网，只通过 Docker 内部网络访问。管理后台 (8082) 限制为管理员 IP 访问。

### 5.2 Docker Compose 生产部署

#### Step 1: 上传代码到服务器

```bash
# 在本地打包
tar czf ypat-deploy.tar.gz \
  --exclude='node_modules' \
  --exclude='.git' \
  --exclude='91pai-master' \
  --exclude='*/target' \
  .

# 上传到服务器
scp ypat-deploy.tar.gz root@82.156.14.216:/opt/ypat/

# 在服务器上解压
ssh root@82.156.14.216
cd /opt/ypat
tar xzf ypat-deploy.tar.gz
```

或者用 Git：

```bash
ssh root@82.156.14.216
cd /opt/ypat
git clone https://github.com/Colouful/ypat.git .
```

#### Step 2: 在服务器上编译

```bash
# 安装 Java 8 和 Maven (如果服务器上没有)
sudo apt install openjdk-8-jdk maven -y

# 编译后端 (pro profile)
cd /opt/ypat/backend-base && mvn clean package -DskipTests -P pro && cd ..
cd /opt/ypat/backend && mvn clean package -DskipTests -P pro && cd ..

# 安装 Node.js (如果服务器上没有)
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y

# 构建前端 (生产模式)
cd /opt/ypat/frontend
npm install
npm run build:h5
cd ..
```

#### Step 3: 修改生产配置

编辑 `docker-compose.yml`，**必须修改以下内容**：

```bash
# 1. 修改 MySQL 密码（不要用默认的 <历史本地 MySQL 密码，已脱敏>）
# 将所有 MYSQL_ROOT_PASSWORD 和 SPRING_DATASOURCE_PASSWORD 改为强密码

# 2. 生产环境不暴露 MySQL 和 Redis 端口到主机
# 注释掉或删除 ports 中的 "3307:3306" 和 "6379:6379"

# 3. 前端 .env.production 确认 API 地址
cat frontend/.env.production
# 确保 VITE_API_BASE_URL 指向生产域名或 IP
```

#### Step 4: 启动

```bash
cd /opt/ypat
docker compose up --build -d

# 等待所有服务健康（约 2-3 分钟）
docker compose ps

# 实时查看启动日志
docker compose logs -f
```

#### Step 5: 验证

```bash
# 检查容器状态
docker compose ps

# 测试 API
curl -s 'http://localhost:8088/banner/list?page=0&size=5&status=1'

# 测试前端
curl -s -o /dev/null -w "%{http_code}" http://localhost:5189/

# 测试管理后台
curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/
```

### 5.3 域名和 HTTPS 配置

> **WHY**: 生产环境必须使用 HTTPS，保护用户数据安全，也是微信小程序的强制要求。

#### 方案 A: Caddy 自动 HTTPS（推荐）

```bash
# 安装 Caddy
sudo apt install -y debian-keyring debian-archive-keyring apt-transport-https
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | sudo gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | sudo tee /etc/apt/sources.list.d/caddy-stable.list
sudo apt update
sudo apt install caddy -y
```

配置 Caddyfile：

```
# /etc/caddy/Caddyfile

api.91pai.com {
    reverse_proxy localhost:8088
}

www.91pai.com {
    reverse_proxy localhost:5189
}

admin.91pai.com {
    reverse_proxy localhost:8082
}
```

```bash
sudo systemctl restart caddy
```

> **WHY**: Caddy 自动申请和续期 Let's Encrypt 证书，零配置 HTTPS。

#### 方案 B: nginx + Let's Encrypt

```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d api.91pai.com -d www.91pai.com -d admin.91pai.com
```

### 5.4 日志管理

```bash
# 查看所有服务日志
docker compose logs -f

# 查看单个服务日志
docker compose logs -f restapi
docker compose logs -f wap

# 查看最近 100 行
docker compose logs --tail 100 restapi

# 搜索错误
docker compose logs restapi 2>&1 | grep -i "error\|exception" | tail -20
```

> **WHY**: Docker 日志通过 `json-file` driver 存储，已在 daemon.json 中配置了 50MB × 3 的轮转限制，防止磁盘被日志撑爆。

### 5.5 数据备份

```bash
# 手动备份 MySQL
docker exec ypat-mysql mysqldump -u root -p'YOUR_PASSWORD' ypat > /opt/ypat/backups/ypat_$(date +%Y%m%d_%H%M%S).sql

# 手动备份 Redis
docker exec ypat-redis redis-cli BGSAVE
docker cp ypat-redis:/data/dump.rdb /opt/ypat/backups/redis_$(date +%Y%m%d_%H%M%S).rdb

# 备份前端静态资源
cp -r frontend/dist/build/h5 /opt/ypat/backups/frontend_$(date +%Y%m%d)
```

自动备份脚本（见 [7.4 数据备份策略](#74-数据备份策略)）。

---

## 六、常见问题排查

### 1. logback.xml 硬编码 `/logs` 目录

**症状**: 启动报错 `/logs/system-restapi.log (No such file or directory)`

**原因**: JAR 内 `BOOT-INF/classes/logback.xml` 硬编码了 `/logs` 目录，macOS 根目录无写权限。

**解决方案 A** — 启动时加 JVM 参数（推荐）：

```bash
-Dlogback.logdir=/tmp/ypat-logs
```

**解决方案 B** — 用 Python 修补 JAR：

```python
import zipfile

for jar in [
    'backend/system-restapi/target/system-restapi-1.0-SNAPSHOT.jar',
    'backend/system-wap/target/system-wap-1.0-SNAPSHOT.jar',
    'backend/system-web/target/system-web-1.0-SNAPSHOT.jar',
]:
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
    print(f"已修补: {jar}")
```

### 2. Eureka 注册 VPN IP 导致 Feign 超时

**症状**: `Read timed out executing POST http://SYSTEM-API/...`

**原因**: Eureka 注册了 VPN/WiFi 的 IP（如 172.20.10.4），wap 通过该 IP 调用 restapi 超时。

**解决方案**:

```bash
# 启动时强制指定 IP
--eureka.instance.ip-address=127.0.0.1
--eureka.instance.hostname=localhost
```

**验证**:

```bash
curl -s http://127.0.0.1:8761/eureka/apps | grep -E '<ipAddr>'
# 期望看到 127.0.0.1，不是 VPN IP
```

### 3. MySQL 认证失败: `caching_sha2_password` 不支持

**症状**: `Access denied for user 'root'@'localhost'` 或 `Unable to load authentication plugin 'caching_sha2_password'`

**原因**: MySQL 8.0 默认使用 `caching_sha2_password` 认证插件，老版本 JDBC 驱动不支持。

**解决方案**: docker-compose.yml 中已配置 `--default-authentication-plugin=mysql_native_password`。如果手动安装的 MySQL：

```sql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'YOUR_PASSWORD';
FLUSH PRIVILEGES;
```

### 4. 前端构建触发 HTTPS 检查

**症状**: 前端构建后请求全部失败，控制台报 HTTPS 相关错误。

**原因**: UniApp 默认 production mode 会强制 HTTPS。

**解决方案**: 本地构建用 development mode：

```bash
npx uni build --mode development
```

### 5. Load balancer does not have available server for client: SYSTEM-API

**症状**: wap 启动后调用 restapi 报错 "no available server"。

**原因**: restapi 还没注册到 Eureka，wap 就启动了。

**解决方案**: 严格按顺序启动，等每个服务完全就绪后再启动下一个。Docker 环境下通过 `depends_on` + `healthcheck` 自动保证顺序。

### 6. 前端页面空白 / API 请求失败

**排查步骤**:

```bash
# 1. 检查前端构建产物是否存在
ls frontend/dist/build/h5/index.html

# 2. 检查 nginx 配置
docker exec ypat-nginx nginx -t

# 3. 检查 nginx 日志
docker compose logs nginx

# 4. 检查 API 是否可达
curl -v http://localhost:8088/banner/list?page=0&size=5&status=1
```

### 7. MySQL 数据丢失

**原因**: 执行了 `docker compose down -v`（`-v` 会删除数据卷）。

**预防**:

```bash
# 正常停止（保留数据）
docker compose down

# ⚠️ 永远不要在生产环境执行
# docker compose down -v  # 这会删除数据卷!
```

### 8. 容器状态 unhealthy

**排查**:

```bash
# 查看健康检查详情
docker inspect --format='{{json .State.Health}}' ypat-restapi | jq

# 查看容器日志
docker compose logs restapi | tail -50
```

常见原因：MySQL 还没就绪、Eureka 还没就绪、端口冲突。

### 9. Java 16+ 编译报错: Record 类冲突

**症状**: `java.lang.Record` 与项目中的 `com.ypat.entity.Record` 冲突。

**原因**: Java 16+ 引入了 `java.lang.Record`，与项目实体类同名。

**解决方案**: 使用 Java 8 编译（**必须**），或在 Java 16+ 中显式 import：

```java
import com.ypat.entity.Record;  // 显式优先
import com.ypat.entity.*;       // 通配符放后面
```

### 10. Docker 磁盘空间不足

```bash
# 查看磁盘使用
docker system df

# 清理无用镜像和缓存
docker system prune -a

# 清理构建缓存
docker builder prune -a
```

---

## 七、生产最佳实践

### 7.1 安全加固

#### 数据库安全

```bash
# 1. 使用强密码（替换 docker-compose.yml 中的 MYSQL_ROOT_PASSWORD）
# 示例: 生成随机密码
openssl rand -base64 24

# 2. 创建应用专用数据库用户（不要用 root）
docker exec -i ypat-mysql mysql -uroot -p'YOUR_ROOT_PASSWORD' <<EOF
CREATE USER 'ypat_app'@'%' IDENTIFIED WITH mysql_native_password BY 'YOUR_APP_PASSWORD';
GRANT ALL PRIVILEGES ON ypat.* TO 'ypat_app'@'%';
FLUSH PRIVILEGES;
EOF
```

#### 网络安全

```yaml
# docker-compose.yml 生产环境修改：
# 1. 不暴露 MySQL 和 Redis 端口
services:
  mysql:
    # ports:                # 注释掉
    #   - "3307:3306"       # 生产环境不暴露
    networks:
      - ypat-net

  redis:
    # ports:                # 注释掉
    #   - "6379:6379"       # 生产环境不暴露
    networks:
      - ypat-net
```

#### 防火墙

```bash
# 只允许必要端口
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw allow 8082/tcp  # 管理后台 (可选，建议限制 IP)
sudo ufw enable
```

### 7.2 监控告警

#### 简易健康检查脚本

```bash
#!/bin/bash
# 文件: scripts/health-check.sh
# 用法: crontab -e → */5 * * * * /opt/ypat/scripts/health-check.sh

SERVICES=(
    "http://localhost:8761/health:Eureka"
    "http://localhost:9081/health:restapi"
    "http://localhost:8081/health:wap"
    "http://localhost:8088/banner/list?page=0&size=1&status=1:API"
)

ALERT_EMAIL="admin@91pai.com"  # 改为你的邮箱
LOG_FILE="/var/log/ypat-health.log"

for entry in "${SERVICES[@]}"; do
    URL="${entry%%:*}"
    NAME="${entry##*:}"
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "$URL")
    if [ "$STATUS" != "200" ]; then
        MSG="[$(date)] ALERT: $NAME ($URL) 返回 HTTP $STATUS"
        echo "$MSG" >> "$LOG_FILE"
        echo "$MSG" | mail -s "YPAT 服务告警: $NAME 异常" "$ALERT_EMAIL" 2>/dev/null
        # 也可以接入企业微信/钉钉 webhook
    fi
done
```

```bash
chmod +x scripts/health-check.sh
# 添加 crontab 每 5 分钟检查一次
(crontab -l 2>/dev/null; echo "*/5 * * * * /opt/ypat/scripts/health-check.sh") | crontab -
```

### 7.3 日志轮转

Docker 层面（已在 daemon.json 中配置）：

```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "50m",
    "max-file": "3"
  }
}
```

应用层面（logback 已内置按天轮转）。如需自定义，在各服务的 `logback.xml` 中调整。

### 7.4 数据备份策略

#### 自动备份脚本

```bash
#!/bin/bash
# 文件: scripts/backup.sh
# 用法: crontab -e → 0 3 * * * /opt/ypat/scripts/backup.sh

BACKUP_DIR="/opt/ypat/backups"
RETENTION_DAYS=7  # 保留最近 7 天的备份
MYSQL_PASSWORD="YOUR_PASSWORD"

mkdir -p "$BACKUP_DIR"

# MySQL 备份
docker exec ypat-mysql mysqldump \
  -u root -p"$MYSQL_PASSWORD" \
  --single-transaction --routines --triggers \
  ypat | gzip > "$BACKUP_DIR/mysql_$(date +%Y%m%d_%H%M%S).sql.gz"

# Redis 备份
docker exec ypat-redis redis-cli BGSAVE
sleep 2
docker cp ypat-redis:/data/dump.rdb "$BACKUP_DIR/redis_$(date +%Y%m%d_%H%M%S).rdb"

# 清理过期备份
find "$BACKUP_DIR" -name "mysql_*.sql.gz" -mtime +$RETENTION_DAYS -delete
find "$BACKUP_DIR" -name "redis_*.rdb" -mtime +$RETENTION_DAYS -delete

echo "[$(date)] 备份完成"
```

```bash
chmod +x scripts/backup.sh
# 每天凌晨 3 点自动备份
(crontab -l 2>/dev/null; echo "0 3 * * * /opt/ypat/scripts/backup.sh") | crontab -
```

#### 异地备份（可选）

```bash
# 上传到腾讯云 COS
# pip install coscmd
# coscmd upload /opt/ypat/backups/mysql_*.sql.gz /ypat-backups/
```

---

## 附录: 部署前检查清单

在执行部署前，逐项确认：

- [ ] Java 8 已安装且 `JAVA_HOME` 正确
- [ ] Maven 已安装
- [ ] Node.js 18+ 已安装
- [ ] Docker 20.10+ 已安装且 daemon 运行中
- [ ] Docker Compose 2.0+ 可用
- [ ] 端口 3307/6379/8761/9081/8081/8082/8088/5189 均空闲
- [ ] 磁盘空间 ≥ 2GB（本地）/ ≥ 20GB（生产）
- [ ] 内存 ≥ 4GB
- [ ] 生产环境已修改 MySQL 默认密码
- [ ] 生产环境已配置防火墙（不暴露 MySQL/Redis）
- [ ] 生产环境已配置 HTTPS
- [ ] 已运行 `bash scripts/check-env.sh` 无报错

---

> **文档维护**: 如遇新问题请补充到「常见问题排查」章节。如修改了端口/架构/依赖，请同步更新本文档。
