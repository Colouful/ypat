# FastDFS 独立服务器 Docker 部署文档（供 YPAT 跨机使用）

> 目标：把 FastDFS（tracker + storage + nginx）单独部署到一台 Linux 服务器，让 YPAT 应用通过 IP/域名访问文件存储。
>
> 适用版本：ypat 1.x
> 镜像：`ygqygq2/fastdfs-nginx`（与项目当前 digest 保持一致）
> 最后更新：2026-07-01

---

## 1. 前置条件

- 一台 Linux 服务器（2 核 4G 起步，推荐 4 核 8G，文件盘根据预期容量挂载）。
- 已安装 Docker + Docker Compose v2。
- **应用服务器与 FastDFS 服务器之间 22122、23000 必须互通**（推荐同一 VPC/内网）。
- 防火墙/安全组放行：
  - `22122/tcp`：应用 → tracker（上传入口）。
  - `23000/tcp`：应用 → storage（实际上传数据）。
  - `8888/tcp`：前端/小程序 → HTTP 下载（可换成你自己的反代端口）。
  - `8080/tcp`：仅宿主机内部使用，**不必对外开放**。

> ⚠️ 本方案使用 `network_mode: host`，让 storage 向 tracker 上报宿主机 IP，从而解决跨机访问时 Docker 容器 IP 不可达的问题。该模式仅适用于 Linux 服务器。

---

## 2. 文件清单

在 FastDFS 服务器上新建目录（例如 `/opt/ypat-fastdfs`），放入以下三个文件：

- `docker-compose.remote.yml`：tracker + storage + nginx 反代。
- `nginx-remote.conf`：把宿主机 `8888` 端口反代到容器内部 nginx 的 `8080`。
- `.env`：服务器 IP、镜像 digest、数据目录。

也可以直接把项目里的 `backend/dev/fastdfs/docker-compose.remote.yml` 和 `nginx-remote.conf` 复制过去。

---

## 3. 环境变量配置（`.env`）

在 FastDFS 服务器上创建 `.env`：

```bash
# 必填：FastDFS 服务器可被应用服务器访问的 IP（内网优先；若应用走公网则填公网 IP）
FASTDFS_HOST_IP=192.168.1.100

# 可选：建议固定 digest，与项目当前一致
YPAT_FASTDFS_IMAGE=ygqygq2/fastdfs-nginx@sha256:076ee9c1ab73f16d81b28f6f8e0405e67446994f9541020115d5e2f546cbde48

# 数据目录
YPAT_FASTDFS_TRACKER_DATA_DIR=/opt/ypat-data/fastdfs/tracker
YPAT_FASTDFS_STORAGE_DATA_DIR=/opt/ypat-data/fastdfs/storage
```

---

## 4. Docker Compose 配置

`docker-compose.remote.yml`：

```yaml
name: ypat-remote-fastdfs

services:
  fastdfs-tracker:
    image: "${YPAT_FASTDFS_IMAGE:-ygqygq2/fastdfs-nginx:latest}"
    container_name: ypat-remote-fastdfs-tracker
    hostname: fastdfs-tracker
    command: ["tracker"]
    network_mode: host
    environment:
      PORT: "22122"
    volumes:
      - "${YPAT_FASTDFS_TRACKER_DATA_DIR:-/opt/ypat-data/fastdfs/tracker}:/var/fdfs"
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 512M
    logging:
      driver: json-file
      options:
        max-size: "20m"
        max-file: "5"

  fastdfs-storage:
    image: "${YPAT_FASTDFS_IMAGE:-ygqygq2/fastdfs-nginx:latest}"
    container_name: ypat-remote-fastdfs-storage
    hostname: fastdfs-storage
    command: ["storage"]
    network_mode: host
    depends_on:
      - fastdfs-tracker
    environment:
      TRACKER_SERVER: "${FASTDFS_HOST_IP:?FASTDFS_HOST_IP is required}:22122"
      GROUP_NAME: "group1"
    volumes:
      - "${YPAT_FASTDFS_STORAGE_DATA_DIR:-/opt/ypat-data/fastdfs/storage}:/var/fdfs"
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 2G
    logging:
      driver: json-file
      options:
        max-size: "20m"
        max-file: "5"

  fastdfs-nginx:
    image: nginx:1.25-alpine
    container_name: ypat-remote-fastdfs-nginx
    network_mode: host
    depends_on:
      - fastdfs-storage
    volumes:
      - ./nginx-remote.conf:/etc/nginx/conf.d/default.conf:ro
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 256M
    logging:
      driver: json-file
      options:
        max-size: "20m"
        max-file: "5"
```

`nginx-remote.conf`：

```nginx
server {
    listen 8888;
    server_name _;

    access_log /dev/stdout;
    error_log /dev/stderr;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_http_version 1.1;
        proxy_buffering off;
    }
}
```

---

## 5. 启动与验证

### 5.1 启动

```bash
cd /opt/ypat-fastdfs
mkdir -p /opt/ypat-data/fastdfs/tracker /opt/ypat-data/fastdfs/storage

docker compose -f docker-compose.remote.yml up -d
# 等待 storage 注册到 tracker
sleep 25
```

### 5.2 检查端口

```bash
ss -tlnp | grep -E '22122|23000|8080|8888'
```

应看到 `22122`、`23000`、`8080`、`8888` 都在监听。

### 5.3 上传/下载测试

```bash
# 上传文件
docker exec ypat-remote-fastdfs-storage sh -c \
  'echo test-fastdfs > /tmp/t.txt && /usr/bin/fdfs_upload_file /etc/fdfs/client.conf /tmp/t.txt'
# 预期返回：group1/M00/00/00/xxxx.txt

# 浏览器或 curl 下载
curl http://${FASTDFS_HOST_IP}:8888/group1/M00/00/00/xxxx.txt
# 预期输出：test-fastdfs
```

### 5.4 从应用服务器验证连通性

在应用服务器上执行：

```bash
telnet ${FASTDFS_HOST_IP} 22122
telnet ${FASTDFS_HOST_IP} 23000
curl -I http://${FASTDFS_HOST_IP}:8888/
```

三项都应能连通。

---

## 6. YPAT 项目配置变更

### 6.1 生产环境（`pro` / `docker-compose.production.yml`）

生产环境已经通过环境变量注入，只需在应用服务器 `.env` 中设置：

```bash
YPAT_FDFS_TRACKER_SERVERS=<FASTDFS_HOST_IP>:22122
YPAT_FDFS_PUBLIC_BASE_URL=http://<FASTDFS_HOST_IP>:8888/
```

注意 `YPAT_FDFS_PUBLIC_BASE_URL` **必须以 `/` 结尾**。

### 6.2 预发环境（`pre` / `docker-compose.staging.yml`）

预发环境的 `fdfs_client.properties` 默认写死了容器名 `fastdfs-tracker`，需要做两件事：

1. 在应用服务器 `.env` 中新增：

```bash
YPAT_FDFS_TRACKER_SERVERS=<FASTDFS_HOST_IP>:22122
YPAT_FDFS_PUBLIC_BASE_URL=http://<FASTDFS_HOST_IP>:8888/
```

2. 修改 `backend/system-wap/src/main/resources/pre/fdfs_client.properties` 和 `backend/system-web/src/main/resources/pre/fdfs_client.properties`：

```properties
fastdfs.tracker_servers = ${YPAT_FDFS_TRACKER_SERVERS:?YPAT_FDFS_TRACKER_SERVERS is required}
```

3. 在 `docker-compose.staging.yml` 的 `restapi`、`wap`、`system-web` 三个服务下添加环境变量：

```yaml
YPAT_FDFS_TRACKER_SERVERS: "${YPAT_FDFS_TRACKER_SERVERS:?YPAT_FDFS_TRACKER_SERVERS is required}"
```

### 6.3 开发环境（本地 IDE / `dev`）

- 修改 `backend/system-wap/src/main/resources/dev/fdfs_client.properties` 和 `backend/system-web/src/main/resources/dev/fdfs_client.properties`：

```properties
fastdfs.tracker_servers = <FASTDFS_HOST_IP>:22122
```

- 修改 `backend/system-wap/src/main/resources/dev/application.yml` 和 `backend/system-web/src/main/resources/dev/application.yml`：

```yaml
system:
  third:
    fdfs_path: ${YPAT_FDFS_PUBLIC_BASE_URL:http://<FASTDFS_HOST_IP>:8888/}
```

### 6.4 特别注意：`system-web` 的硬编码 URL

`backend/system-web/src/main/java/com/ypat/config/SystemConfig.java` 第 12 行把 `fdfs_path` 写死成了 `http://127.0.0.1:8888/`，**不会读取 `application.yml`**，必须修改：

```java
// 临时方案：改成远程地址并重新编译
public static final String fdfs_path = "http://<FASTDFS_HOST_IP>:8888/";
```

推荐方案（建议顺手重构）是把 `fdfs_path` 改成可注入属性，并把 `BannerController`、`YpatInfoController`、`ArticleController` 里使用 `SystemConfig.fdfs_path` 的地方改为注入 `SystemConfig` 实例后调用 `getFdfs_path()`。

---

## 7. 域名 + HTTPS（生产必填）

微信小程序要求下载域名必须是 HTTPS。推荐在 FastDFS 服务器前再挂一层 Nginx/EdgeOne/LB 做 SSL 终结：

```nginx
server {
    listen 443 ssl;
    server_name fdfs.yourdomain.com;

    ssl_certificate /path/to/fullchain.pem;
    ssl_certificate_key /path/to/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:8888;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

然后：

```bash
YPAT_FDFS_PUBLIC_BASE_URL=https://fdfs.yourdomain.com/
```

并在微信公众平台 → 开发管理 → 服务器域名 → `downloadFile` 合法域名中添加该域名。

---

## 8. 常见问题

### 8.1 应用上传失败，`recv package size -1 != 10`

- 检查应用服务器是否能访问 `FASTDFS_HOST_IP:22122` 和 `FASTDFS_HOST_IP:23000`。
- 检查 `fastdfs.tracker_servers` 是否配成了 `127.0.0.1` 或容器名。
- 检查 storage 是否已注册到 tracker：`docker logs ypat-remote-fastdfs-storage | grep "tracker server"`。

### 8.2 上传成功但应用无法下载

- 在 storage 容器里执行 `fdfs_monitor /etc/fdfs/client.conf` 查看 `ip_addr` 字段。它必须是应用服务器能访问到的 IP（即 `FASTDFS_HOST_IP`）。
- 如果服务器多网卡，storage 可能上报了错误的 IP。此时需要用 `bind_addr` 强制指定，方式可参考 [ygqygq2/fastdfs-nginx](https://github.com/ygqygq2/fastdfs-nginx) 的 `CUSTOM_CONFIG` 模式挂载自定义 `storage.conf`。

### 8.3 端口冲突

`network_mode: host` 会占用宿主机 `22122`、`23000`、`8080`、`8888`。如有冲突，请释放这些端口或改用更复杂的 macvlan/overlay 方案。

### 8.4 数据备份

```bash
# 停止写入
docker stop ypat-remote-fastdfs-storage

# 打包 storage 数据
tar czf fastdfs-backup-$(date +%Y%m%d).tar.gz -C /opt/ypat-data/fastdfs/storage .

# 恢复后重启
docker restart ypat-remote-fastdfs-storage
```

---

## 9. 相关文件

- `backend/dev/fastdfs/docker-compose.remote.yml`
- `backend/dev/fastdfs/nginx-remote.conf`
- `backend/dev/fastdfs/.env.remote`
- `docs/FASTDFS_GUIDE.md`（本地/开发环境说明）
