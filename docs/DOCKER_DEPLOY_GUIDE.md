# YPAT Docker 部署指南

> 适用于 macOS 本地开发和 Linux 服务器部署，基于 Docker Compose 一键管理全部服务。

---

## 目录

- [前置条件](#前置条件)
- [项目结构](#项目结构)
- [快速启动](#快速启动)
- [分步部署](#分步部署)
- [配置说明](#配置说明)
- [常用操作](#常用操作)
- [常见问题排查](#常见问题排查)
- [线上部署](#线上部署)

---

## 前置条件

### 软件要求

| 软件 | 版本 | 说明 |
|------|------|------|
| Docker | 20.10+ | 容器运行时 |
| Docker Compose | 2.0+ | 编排工具（Docker Desktop 自带） |
| Java 8 | Zulu 8 | 仅编译后端需要，运行不需要 |
| Maven | 3.x | 仅编译后端需要 |
| Node.js | 18+ | 仅构建前端需要 |

### 硬件要求（推荐）

- 内存: 4GB+ (MySQL + Redis + 3个 Java 服务 + nginx)
- 磁盘: 2GB+ (镜像 + 数据卷)
- CPU: 2核+

---

## 项目结构

```
ypat/
├── docker-compose.yml          # Docker Compose 主配置
├── docker/
│   ├── nginx/
│   │   └── default.conf        # nginx 配置 (反代 + 前端)
│   └── mysql/
│       └── init.sql            # MySQL 初始化脚本
├── backend-base/
│   └── base-eureka/
│       ├── Dockerfile
│       └── target/*.jar
├── backend/
│   ├── system-restapi/
│   │   ├── Dockerfile
│   │   └── target/*.jar
│   ├── system-wap/
│   │   ├── Dockerfile
│   │   └── target/*.jar
│   └── pom.xml                 # Maven profiles: dev (默认) / pro
├── frontend/
│   ├── Dockerfile
│   ├── .env.development        # 本地开发配置
│   ├── .env.production         # 生产环境配置
│   └── dist/build/h5/          # 前端构建产物
└── scripts/
    └── start-local.sh          # 非 Docker 一键启动脚本
```

---

## 快速启动

如果已经编译好 JAR 和前端，直接执行：

```bash
# 1. 启动全部容器
docker compose up -d

# 2. 等待所有容器健康 (~2-3分钟)
docker compose ps

# 3. 访问
# 前端: http://localhost:5189
# API:  http://localhost:8088
```

---

## 分步部署

### Step 1: 编译后端

```bash
# 编译 Eureka (服务注册中心)
cd backend-base
mvn clean package -DskipTests
cd ..

# 编译 restapi + wap (默认 dev profile)
cd backend
mvn clean package -DskipTests
cd ..
```

> **生产环境** 加 `-P pro` 参数:
> ```bash
> cd backend-base && mvn clean package -DskipTests -P pro && cd ..
> cd backend && mvn clean package -DskipTests -P pro && cd ..
> ```

编译产物：
- `backend-base/base-eureka/target/base-eureka-1.0-SNAPSHOT.jar`
- `backend/system-restapi/target/system-restapi-1.0-SNAPSHOT.jar`
- `backend/system-wap/target/system-wap-1.0-SNAPSHOT.jar`

### Step 2: 构建前端

```bash
cd frontend
npm install
npm run build:h5
cd ..
```

构建产物在 `frontend/dist/build/h5/` 目录。

### Step 3: 检查端口占用

Docker 容器需要以下端口，确保没有被占用：

```bash
# 检查端口占用
lsof -i :3307 -i :6379 -i :8761 -i :9081 -i :8081 -i :8088 -i :5189

# 如果主机有 MySQL/Redis/nginx 在运行，需要先停掉
# macOS:
brew services stop redis
brew services stop nginx
# 停止主机 MySQL (不同安装方式命令不同)
sudo /usr/local/mysql/support-files/mysql.server stop
# 或
brew services stop mysql

# Linux:
sudo systemctl stop mysql redis nginx
```

> ⚠️ **重要**: 如果主机 MySQL 占用 3306 端口，docker-compose.yml 中已映射为 3307。
> 本地开发工具连接数据库时用 3307 端口: `mysql -h 127.0.0.1 -P 3307 -u root -p`

### Step 4: 启动 Docker Compose

```bash
# 首次启动 (会构建镜像)
docker compose up --build -d

# 后续启动 (镜像已存在，直接启动)
docker compose up -d
```

### Step 5: 验证服务状态

```bash
# 查看所有容器状态
docker compose ps

# 期望输出:
# NAME          STATUS                    PORTS
# ypat-mysql    Up 5min (healthy)         0.0.0.0:3307->3306/tcp
# ypat-redis    Up 5min (healthy)         0.0.0.0:6379->6379/tcp
# ypat-eureka   Up 5min (healthy)         0.0.0.0:8761->8761/tcp
# ypat-restapi  Up 3min (healthy)         0.0.0.0:9081->9081/tcp
# ypat-wap      Up 2min (healthy)         0.0.0.0:8081->8081/tcp
# ypat-nginx    Up 1min                   0.0.0.0:8088->8088/tcp
#                                         0.0.0.0:5189->80/tcp

# 测试 API 链路
curl -s 'http://localhost:8088/banner/list?page=0&size=5&status=1'
# 期望: {"code":200,"msg":"成功",...}

# 测试前端
curl -s -o /dev/null -w "%{http_code}" http://localhost:5189/
# 期望: 200
```

### Step 6: 访问

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端页面 | http://localhost:5189 | H5 用户端 |
| API 网关 | http://localhost:8088 | nginx 反代 → wap |
| Eureka 控制台 | http://localhost:8761 | 服务注册中心 |
| MySQL | 127.0.0.1:3307 | 数据库 (root / <历史本地 MySQL 密码，已脱敏>) |
| Redis | 127.0.0.1:6379 | 缓存 |

---

## 配置说明

### docker-compose.yml 核心配置

```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: "<历史本地 MySQL 密码，已脱敏>"  # 数据库密码
      MYSQL_DATABASE: ypat              # 自动创建的数据库
    ports:
      - "3307:3306"                     # 主机3307 → 容器3306
    command: >
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --default-authentication-plugin=mysql_native_password

  restapi:
    environment:
      # 通过环境变量覆盖 application.yml 中的配置
      # Docker 网络内用容器名代替 localhost
      SPRING_DATASOURCE_URL: "jdbc:mysql://mysql:3306/ypat?..."
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: "<历史本地 MySQL 密码，已脱敏>"
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: "http://eureka:8761/eureka/"
```

### 启动顺序（自动管理）

```
mysql ──┐
        ├── restapi ──┐
redis ──┤              ├── wap ── nginx
        │              │
eureka ─┘──────────────┘
```

Docker Compose 通过 `depends_on` + `healthcheck` 自动保证启动顺序，无需手动等待。

### 前端环境配置

| 文件 | 用途 | API 地址 |
|------|------|----------|
| `.env.development` | `npm run dev:h5` 本地开发 | http://localhost:8088 |
| `.env.production` | `npm run build:h5` 生产构建 | http://82.156.14.216:8088 |

### 后端环境配置 (Maven Profile)

| Profile | 命令 | 用途 | 数据库 |
|---------|------|------|--------|
| dev (默认) | `mvn package -DskipTests` | 本地开发 | localhost |
| pro | `mvn package -DskipTests -P pro` | 生产部署 | 82.156.14.216 |

---

## 常用操作

### 查看日志

```bash
# 实时查看所有服务日志
docker compose logs -f

# 查看单个服务日志
docker compose logs -f restapi
docker compose logs -f wap
docker compose logs -f mysql

# 查看最近 100 行
docker compose logs --tail 100 restapi
```

### 重启服务

```bash
# 重启全部
docker compose restart

# 重启单个服务
docker compose restart restapi

# 重建并重启 (Dockerfile 变更后)
docker compose up --build -d restapi
```

### 停止与清理

```bash
# 停止全部容器 (保留数据)
docker compose down

# 停止并删除数据卷 (⚠️ 会清空数据库!)
docker compose down -v

# 停止并删除镜像
docker compose down --rmi local
```

### 进入容器调试

```bash
# 进入 MySQL 容器
docker exec -it ypat-mysql mysql -u root -p'<历史本地 MySQL 密码，已脱敏>' ypat

# 进入 Redis 容器
docker exec -it ypat-redis redis-cli

# 进入 Java 服务容器
docker exec -it ypat-restapi bash

# 查看容器内日志文件
docker exec -it ypat-restapi ls /tmp/ypat-logs/
```

### 重新编译后端后更新

```bash
# 1. 重新编译
cd backend && mvn clean package -DskipTests && cd ..

# 2. 重建镜像并重启
docker compose up --build -d restapi wap
```

### 重新构建前端后更新

```bash
# 1. 重新构建
cd frontend && npm run build:h5 && cd ..

# 2. nginx 通过 bind mount 读取 dist，重启即可
docker compose restart nginx
```

---

## 常见问题排查

### 1. 端口被占用: `ports are not available: bind: address already in use`

**原因**: 主机上已有服务占用该端口。

**排查**:
```bash
lsof -i :3306    # 检查 MySQL
lsof -i :6379    # 检查 Redis
lsof -i :8088    # 检查 nginx
```

**解决**: 停掉主机服务，或修改 docker-compose.yml 中的端口映射。

### 2. MySQL 认证失败: `Unable to load authentication plugin 'caching_sha2_password'`

**原因**: MySQL 8.0 默认使用 `caching_sha2_password`，旧版 JDBC 驱动不支持。

**解决**: docker-compose.yml 中 MySQL command 已包含 `--default-authentication-plugin=mysql_native_password`。

如果仍有问题，进入 MySQL 手动修改：
```bash
docker exec -it ypat-mysql mysql -u root -p'<历史本地 MySQL 密码，已脱敏>'
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '<历史本地 MySQL 密码，已脱敏>';
FLUSH PRIVILEGES;
```

### 3. restapi 启动失败: `Access denied for user 'root'@'localhost'`

**原因**: JAR 内 application.yml 的密码与 MySQL 实际密码不一致。

**排查**:
```bash
docker logs ypat-restapi 2>&1 | grep "Access denied"
```

**解决**: docker-compose.yml 通过环境变量 `SPRING_DATASOURCE_PASSWORD` 覆盖了 JAR 内配置，确保该值与 `MYSQL_ROOT_PASSWORD` 一致。

### 4. Feign 超时: `Read timed out executing POST http://SYSTEM-API/...`

**原因**: wap 通过 Eureka 找到 restapi，但连不上。

**排查**:
```bash
# 检查 Eureka 注册状态
curl -s http://localhost:8761/eureka/apps | grep -E '<name>|<ipAddr>'
```

**解决**: Docker 网络内容器间通信使用容器名，通过环境变量 `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` 和 `EUREKA_INSTANCE_PREFER_IP_ADDRESS` 配置。

### 5. 前端页面空白 / API 请求失败

**排查**:
```bash
# 检查前端 dist 是否存在
ls frontend/dist/build/h5/index.html

# 检查 nginx 配置
docker exec ypat-nginx nginx -t

# 检查 nginx 日志
docker compose logs nginx
```

**解决**: 确保前端已构建 (`npm run build:h5`)，nginx 配置中的 `proxy_pass` 指向 `wap:8081`（不是 localhost）。

### 6. MySQL 数据丢失

**原因**: 使用了 `docker compose down -v` 删除了数据卷。

**预防**:
```bash
# 正常停止（保留数据）
docker compose down

# 定期备份
docker exec ypat-mysql mysqldump -u root -p'<历史本地 MySQL 密码，已脱敏>' ypat > backup_$(date +%Y%m%d).sql
```

### 7. 容器启动后状态为 `unhealthy`

**排查**:
```bash
# 查看健康检查日志
docker inspect --format='{{json .State.Health}}' ypat-restapi

# 查看容器日志
docker compose logs restapi | tail -50
```

常见原因：
- MySQL 还没就绪 → 等待重试
- Eureka 还没就绪 → 等待重试
- 端口冲突 → 检查端口占用

### 8. Docker 磁盘空间不足

```bash
# 清理无用镜像和缓存
docker system prune -a

# 查看 Docker 磁盘使用
docker system df
```

---

## 线上部署

### 服务器要求

- 操作系统: Ubuntu 20.04+ / CentOS 7+
- Docker: 20.10+
- Docker Compose: 2.0+
- 内存: 4GB+
- 带宽: 6Mbps+

### 部署步骤

```bash
# 1. 克隆代码
git clone https://github.com/Colouful/ypat.git
cd ypat

# 2. 编译后端 (pro profile)
cd backend-base && mvn clean package -DskipTests -P pro && cd ..
cd backend && mvn clean package -DskipTests -P pro && cd ..

# 3. 构建前端
cd frontend && npm install && npm run build:h5 && cd ..

# 4. 修改 docker-compose.yml
#    - 修改 MYSQL_ROOT_PASSWORD 为强密码
#    - 修改端口映射（生产环境建议只暴露 8088 和 5189）
#    - 确保 .env.production 中的 API 地址正确

# 5. 启动
docker compose up -d

# 6. 验证
docker compose ps
curl -s 'http://localhost:8088/banner/list?page=0&size=5&status=1'
```

### 线上安全建议

1. **修改默认密码**: `MYSQL_ROOT_PASSWORD` 不要用 `<历史本地 MySQL 密码，已脱敏>`
2. **限制端口暴露**: MySQL (3307) 和 Redis (6379) 不要暴露到公网
3. **配置防火墙**: 只开放 80/443 (nginx) 端口
4. **使用 HTTPS**: 在 nginx 前加一层反代 (Caddy/Traefik) 或配置 SSL
5. **定期备份数据库**: 设置 cron 定时备份 MySQL
6. **日志轮转**: 配置 Docker 日志大小限制

```yaml
# docker-compose.yml 中添加日志限制
services:
  restapi:
    logging:
      driver: "json-file"
      options:
        max-size: "50m"
        max-file: "3"
```

---

## 端口分配总览

| 服务 | 容器端口 | 主机端口 | 说明 |
|------|----------|----------|------|
| MySQL | 3306 | 3307 | 数据库 |
| Redis | 6379 | 6379 | 缓存 |
| Eureka | 8761 | 8761 | 服务注册中心 |
| system-restapi | 9081 | 9081 | 后端 API 服务 |
| system-wap | 8081 | 8081 | 后端网关 |
| nginx (API) | 8088 | 8088 | API 反代 |
| nginx (前端) | 80 | 5189 | 前端静态资源 |

## 附录：本地 Docker 构建前端注意事项

`npm run build:h5` 默认加载 `.env.production`，会触发 HTTPS 检查。
本地 Docker 部署使用 HTTP，需要加 `--mode development` 参数：

```bash
# 本地 Docker 构建 (使用 development 环境)
cd frontend
npx uni build --mode development

# 正式发版构建 (使用 production 环境，需要 HTTPS)
npm run build:h5
```

| 构建方式 | 加载的环境文件 | 适用场景 |
|----------|--------------|----------|
| `npx uni build --mode development` | `.env.development` | 本地 Docker 部署 |
| `npm run build:h5` | `.env.production` | 正式线上发版 (需 HTTPS) |
