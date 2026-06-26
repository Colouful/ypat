# YPAT 管理后台 (system-web) 部署指南

> system-web 是基于 Spring Boot + Thymeleaf 的管理后台，用于审核约拍、管理用户、内容管理等运营操作。

---

## 服务信息

| 项目 | 说明 |
|------|------|
| 技术栈 | Spring Boot 1.5 + Thymeleaf + Bootstrap |
| 端口 | 8082 |
| 依赖服务 | Eureka (服务发现) + Redis (缓存) + system-restapi (数据接口) |
| 默认账号 | 见数据库 t_admin 表 |

---

## Docker 部署（推荐）

system-web 已集成到项目根目录的 `docker-compose.yml`，随其他服务一起启动：

```bash
# 在项目根目录执行
docker compose up --build -d

# 查看所有容器状态
docker compose ps

# 访问管理后台
# http://localhost:8082
```

### 单独重启 system-web

```bash
docker compose restart system-web

# 查看日志
docker compose logs -f system-web
```

---

## 本地非 Docker 部署

### 前置条件

- Java 8 (Zulu 8)
- Eureka 已运行 (8761)
- Redis 已运行 (6379)
- system-restapi 已运行 (9081)

### 编译

```bash
cd backend
mvn clean package -DskipTests
# 产物: backend/system-web/target/system-web-1.0-SNAPSHOT.jar
```

### 启动

```bash
export JAVA8_HOME="/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home"

$JAVA8_HOME/bin/java \
  -Dlogback.logdir=/tmp/ypat-logs \
  -jar backend/system-web/target/system-web-1.0-SNAPSHOT.jar \
  --server.port=8082 \
  --eureka.instance.ip-address=127.0.0.1 \
  --eureka.instance.hostname=localhost
```

### 访问

```
http://localhost:8082
```

首次访问会跳转到登录页面。

---

## 配置说明

### 环境配置文件

```
backend/system-web/src/main/resources/
├── dev/
│   ├── application.yml      # 开发环境配置
│   └── bootstrap.yml
└── pro/
    ├── application.yml      # 生产环境配置
    └── bootstrap.yml
```

### dev/application.yml 关键配置

```yaml
server:
  port: 8082

spring:
  redis:
    host: localhost      # Docker 内会被环境变量覆盖为 redis
    port: 6379

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/  # Docker 内覆盖为 eureka:8761
```

### Docker 环境变量覆盖

docker-compose.yml 中通过环境变量覆盖 localhost 为容器名：

```yaml
environment:
  SPRING_REDIS_HOST: redis
  EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: "http://eureka:8761/eureka/"
```

---

## 常见问题

### 1. 启动报错 `/logs/system-web.log` No such file

**原因**: logback.xml 硬编码 `/logs` 目录。

**修复 (本地)**: 启动时加 `-Dlogback.logdir=/tmp/ypat-logs`

**修复 (Docker)**: Dockerfile 中已 `mkdir -p /tmp/ypat-logs`，JAR 内 logback.xml 已修补。

### 2. 页面 500 / 模板渲染错误

**排查**:
```bash
docker compose logs system-web | tail -50
```

常见原因：
- Thymeleaf 模板语法错误
- 缺少静态资源
- Eureka 未注册导致 Feign 调用失败

### 3. 登录后立即跳回登录页

**原因**: Redis 未连接，Session 无法保存。

**排查**:
```bash
# 检查 Redis 是否正常
docker compose logs redis | tail -5
# 检查 system-web 是否连上 Redis
docker compose logs system-web | grep -i redis
```

### 4. 无法访问后端 API

**原因**: system-web 通过 Feign 调用 system-restapi，如果 restapi 未注册到 Eureka 会失败。

**排查**:
```bash
# 检查 Eureka 注册状态
curl -s http://localhost:8761/eureka/apps | grep -E '<name>|<status>'
# 期望看到 SYSTEM-API (restapi) 和 SYSTEM-WAP 都是 UP
```

---

## 端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| system-web | 8082 | 管理后台 |
| system-wap | 8081 | 用户端网关 |
| system-restapi | 9081 | API 服务 |
| Eureka | 8761 | 服务注册 |
| nginx (API) | 8088 | API 反代 |
| nginx (前端) | 5189 | 用户端前端 |
| MySQL | 3307 | 数据库 |
| Redis | 6379 | 缓存 |
