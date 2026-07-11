# 本地后端 jar 构建 + Docker 快速重启

> 只适用于本机开发环境 `docker-compose.yml`。
> staging/production 仍使用各服务原 Dockerfile 在容器内执行 `mvn package`，不依赖本机 `target/`。

## 目标

本地改后端代码后的流程固定为：

```bash
改 Java 代码 -> 本机 mvn 打 jar -> docker compose up -d --build --no-deps <service>
```

Docker 只把已经打好的 jar 复制进镜像，不在本地 Docker build 阶段跑 Maven。

## 本次支持的服务

| Compose 服务 | Maven 模块 | 本地 jar |
| --- | --- | --- |
| `restapi` | `system-restapi` | `backend/system-restapi/target/system-restapi-1.0-SNAPSHOT.jar` |
| `wap` | `system-wap` | `backend/system-wap/target/system-wap-1.0-SNAPSHOT.jar` |

`eureka` 不在本地快速重启范围内，继续沿用原构建方式。

## 实现方式

本地默认由 `docker-compose.override.yml` 接管 `restapi` 和 `wap` 的 build 配置：

- `restapi` 使用 `backend/system-restapi/Dockerfile.local`
- `wap` 使用 `backend/system-wap/Dockerfile.local`
- 两个 local Dockerfile 只执行 `COPY target/*.jar app.jar`
- `backend/system-restapi/.dockerignore` 和 `backend/system-wap/.dockerignore` 只放行目标 jar，避免把整个 `target/` 传给 Docker

共享 Dockerfile 保持不变：

- `backend/system-restapi/Dockerfile`
- `backend/system-wap/Dockerfile`

这些文件仍然是 staging/production 的安全路径，会在 Docker build 内部自己跑 `mvn package`。

## 日常命令

最简单用法：

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
bash scripts/restart-docker-backend.sh restapi
bash scripts/restart-docker-backend.sh wap
bash scripts/restart-docker-backend.sh all
```

下面是脚本内部做的手动命令。

### 只改了 restapi

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/backend
mvn -pl system-restapi -am package -DskipTests -B
cd ..
docker compose up -d --build --no-deps restapi
```

### 只改了 wap

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/backend
mvn -pl system-wap -am package -DskipTests -B
cd ..
docker compose up -d --build --no-deps wap
```

### 两个都改了

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/backend
mvn -pl system-restapi,system-wap -am package -DskipTests -B
cd ..
docker compose up -d --build --no-deps restapi wap
```

公共模块改动时，例如 `system-domain`、`system-object`、`system-security`、`system-sso`，通常需要同时重启 `restapi` 和 `wap`。

## 快速自查

确认本机 jar 时间是刚刚生成的：

```bash
ls -lh backend/system-restapi/target/system-restapi-1.0-SNAPSHOT.jar
ls -lh backend/system-wap/target/system-wap-1.0-SNAPSHOT.jar
```

确认 Compose 当前确实使用本地快速 Dockerfile：

```bash
docker compose config | grep -A4 "dockerfile: Dockerfile.local"
```

确认容器已重新创建：

```bash
docker compose ps restapi wap
```

如果只执行了 `docker compose build`，容器不会自动重启，新代码不会生效；日常直接使用 `docker compose up -d --build --no-deps <service>`。

## 常见问题

### jar not found

说明还没在本机跑 Maven，或 Maven 命令跑错模块。先生成目标 jar，再执行 Docker 命令。

### Docker 又开始跑 mvn

说明没有加载 `docker-compose.override.yml`，或命令显式使用了 `-f` 但没带 override 文件。日常在项目根目录直接执行 `docker compose ...` 即可。

### staging/production 会不会受影响

不会。staging/production compose 文件仍指向共享 Dockerfile；本地 override 只服务于默认开发 compose。共享 Dockerfile 内部仍会自己跑 `mvn package`，保留 2026-07-03 事故后的服务器安全路径。
