# YPAT 部署指南

## 端口分配表

| 服务 | 端口 | 说明 |
|------|------|------|
| Eureka Server | 8761 | 服务注册中心 |
| system-restapi | 9081 | 后端 REST API |
| system-wap | 8081 | 后端 WAP 服务 |
| nginx 反代 | 8088 | API 网关入口 |
| 前端 dev server | 5189 | Vite 开发服务器 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |

---

## 本地开发 (dev)

### 1. 编译后端（默认 dev profile）

```bash
cd backend-base && mvn clean package -DskipTests
cd ../backend && mvn clean package -DskipTests
```

### 2. 一键启动后端

```bash
bash scripts/start-local.sh
```

脚本会自动：
- 清理占用端口的旧进程
- 检查 MySQL / Redis 是否运行
- 启动 nginx (8088)
- 按顺序启动 Eureka → restapi → wap
- 验证 API 链路

### 3. 启动前端

```bash
cd frontend && npm run dev:h5
```

访问 http://localhost:5189

---

## 生产发版 (pro)

### 1. 编译后端（pro profile）

```bash
cd backend-base && mvn clean package -DskipTests -P pro
cd ../backend && mvn clean package -DskipTests -P pro
```

### 2. 前端构建

```bash
cd frontend && npm run build:h5
```

构建产物在 `frontend/dist/` 目录。

---

## 环境配置说明

### 前端

| 文件 | 环境 | API 地址 |
|------|------|----------|
| `.env.development` | 本地开发 | `http://localhost:8088` |
| `.env.production` | 生产构建 | `http://82.156.14.216:8088` |

Vite 根据 `npm run dev:h5` / `npm run build:h5` 自动加载对应文件。

### 后端

通过 Maven Profile 切换：
- `dev`（默认）：加载 `src/main/resources/dev/application.yml`
- `-P pro`：加载 `src/main/resources/pro/application.yml`

---

## 常见问题排查

### 1. logback /logs 目录问题

**现象**：启动报错 `Failed to create dir /logs`

**解决**：logback 配置中使用了绝对路径 `/logs`，需修补 JAR 中的 logback 配置，或在启动前创建目录：
```bash
sudo mkdir -p /logs && sudo chmod 777 /logs
```

### 2. MySQL 密码不匹配

**现象**：`Access denied for user 'root'@'localhost'`

**解决**：确认 dev/application.yml 中密码为 `Li123456.`，与本地 MySQL 一致。若修改了配置需重新打包：
```bash
cd backend && mvn clean package -DskipTests
```

### 3. Eureka IP 注册问题

**现象**：服务注册到 Eureka 的 IP 不是 localhost

**解决**：启动时加参数：
```bash
java -jar xxx.jar --eureka.instance.ip-address=127.0.0.1 --eureka.instance.hostname=localhost
```

### 4. CORS 跨域问题

**现象**：前端请求后端报 CORS 错误

**解决**：本地开发通过 nginx 反代 8088 端口统一入口，避免跨域。确认 nginx 配置正确代理了 restapi (9081) 和 wap (8081)。

### 5. 端口被占用

**现象**：`Address already in use`

**解决**：
```bash
lsof -ti :端口号 | xargs kill -9
```

---

## 日志查看

日志统一输出到项目根目录的 `logs/` 目录：

```bash
# 实时查看 Eureka 日志
tail -f logs/eureka.log

# 实时查看 restapi 日志
tail -f logs/restapi.log

# 实时查看 wap 日志
tail -f logs/wap.log
```

生产环境日志路径参考各模块 logback 配置。
