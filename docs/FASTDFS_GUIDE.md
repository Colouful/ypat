# FastDFS 文件存储部署指南

> 适用版本：ypat 1.x
> 最后更新：2026-06-27
> 镜像：`ygqygq2/fastdfs-nginx:latest`（多架构原生支持，含 amd64/arm64）

## 1. 为什么需要 FastDFS

YPAT 平台需要存储大量用户头像、约拍图、认证图片（身份证 OCR、约拍作品）。这些文件：

- 单个文件 100KB~5MB
- 总量会增长到 TB 级
- 必须用对象存储/分布式文件系统，不能放在应用服务器本地磁盘

FastDFS 是淘宝开源的轻量级分布式文件系统，特点：

- **轻量**：三个进程（tracker + storage + nginx），没有 Hadoop/HDFS 那么重
- **高可用**：tracker 多节点可集群，storage 故障自动恢复
- **HTTP 直读**：内置 nginx 插件直接通过 HTTP 下载，不走后端
- **够用**：满足 10TB 以内、QPS 万级以内的中小项目

**为什么不直接用 OSS/S3**：

- 商业项目优先 OSS（更省运维）
- 学习/独立项目用 FastDFS（不依赖外部账号、纯内网）
- YPAT 当前用 FastDFS 是为了内网部署、不出网

## 2. 架构概览

```
┌─────────────┐         ┌─────────────┐
│   Client    │         │   Backend   │
│ (微信小程序)│         │  (Spring)   │
└──────┬──────┘         └──────┬──────┘
       │                        │
       │ HTTP 下载文件          │ fdfs_upload_file
       │ (返回 group/M00/...)  │ (返回文件路径)
       │                        ▼
       │                ┌───────────────┐
       │                │   Tracker     │ (注册中心)
       │                │   :22122      │
       │                └───────┬───────┘
       │                        │
       │                  返回 storage 地址
       │                        ▼
       ▼                ┌───────────────┐
┌──────────────┐        │   Storage     │
│  nginx       │ ◄──────│   :23000      │
│  :8888 →8080 │        │   (实际存文件) │
└──────────────┘        └───────────────┘
```

**三个角色**：

| 角色 | 作用 | 端口 |
|------|------|------|
| Tracker | 注册中心，记录 storage 节点 | 22122 |
| Storage | 实际存文件的节点 | 23000 |
| Nginx | HTTP 代理，浏览器访问文件入口 | 8888 → 容器内 8080 |

## 3. 三种部署方案对比

| 方案 | 适用场景 | 难度 | 成本 |
|------|---------|------|------|
| **A. Docker 本地** | 本地开发调试 | ⭐ 简单 | 免费 |
| **B. Docker 单机部署** | 小规模生产 (<1TB) | ⭐⭐ 中等 | 服务器一台 |
| **C. 自建集群** | 中大规模生产 (>1TB) | ⭐⭐⭐ 复杂 | 服务器 3+ 台 |

YPAT 当前默认走**方案 A**（本地 Docker）；生产前需要切到方案 B。

## 4. 方案 A：Docker 本地部署（开发用）

### 4.1 准备

- Docker Desktop 已安装
- Mac Apple Silicon（M1/M2/M3）或 Intel Mac
- Linux/WSL2 同理

### 4.2 启动

```bash
cd backend/dev/fastdfs
docker compose up -d
sleep 25  # 等待 storage 注册到 tracker
```

### 4.3 验证

```bash
# 1. 端口监听
lsof -nP -iTCP:22122,23000,8888 -sTCP:LISTEN

# 2. 上传文件（容器内）
docker exec ypat-fastdfs-storage sh -c \
  'echo test > /tmp/t.txt && /usr/bin/fdfs_upload_file /etc/fdfs/client.conf /tmp/t.txt'
# 预期: group1/M00/00/00/xxxx.txt

# 3. HTTP 下载
curl -sS http://127.0.0.1:8888/group1/M00/00/00/xxxx.txt
# 预期: "test"
```

### 4.4 后端配置（已就位）

| 文件 | 字段 | 值 |
|------|------|-----|
| `backend/system-wap/src/main/resources/conf/fdfs_client.properties` | `fastdfs.tracker_servers` | `127.0.0.1:22122` |
| `backend/system-wap/src/main/resources/conf/sys_conf.properties` | `system.third.fdfs_path` | `http://127.0.0.1:8888/` |
| `backend/system-web/src/main/resources/conf/fdfs_client.properties` | `fastdfs.tracker_servers` | `127.0.0.1:22122` |

修改后重启后端：

```bash
docker restart ypat-wap ypat-system-web
```

### 4.5 完全清理

```bash
cd backend/dev/fastdfs
docker compose down -v
rm -rf tracker_data storage_data
```

## 5. 方案 B：Docker 单机部署（生产用，单台云服务器）

**WHY**：小规模生产用单机部署够用，省钱，运维简单。

### 5.1 前置条件

- 一台云服务器（2 核 4G 起步，推荐 4 核 8G）
- 已装 Docker + Docker Compose
- 公网 IP 一个（用于 HTTP 下载）

### 5.2 部署步骤

```bash
# 1. 把 backend/dev/fastdfs 目录上传到服务器
scp -r backend/dev/fastdfs root@<your-server-ip>:/opt/

# 2. 服务器上启动
ssh root@<your-server-ip>
cd /opt/fastdfs
docker compose up -d
```

### 5.3 修改后端配置

把 `127.0.0.1` 改成服务器公网 IP：

```properties
# backend/system-wap/src/main/resources/conf/sys_conf.properties
system.third.fdfs_path = http://<your-server-ip>:8888/

# backend/system-wap/src/main/resources/conf/fdfs_client.properties
fastdfs.tracker_servers = <your-server-ip>:22122
```

### 5.4 防火墙

```bash
# 开放端口（云服务器安全组也要开）
ufw allow 22122/tcp  # tracker（后端访问）
ufw allow 23000/tcp  # storage（一般不需要对外开放）
ufw allow 8888/tcp   # nginx（前端访问）
```

### 5.5 进阶：绑定域名 + HTTPS

**WHY**：直接用 IP + HTTP 在微信小程序里会被拒（必须 HTTPS）。

1. **域名解析**：到域名服务商把 `fdfs.91qupaier.com` 解析到服务器 IP
2. **申请证书**：Let's Encrypt 免费证书
3. **加 HTTPS nginx 反代**（不要直接给 FastDFS nginx 加证书，配置容易踩坑）：

```nginx
# /etc/nginx/conf.d/fdfs.conf
server {
    listen 443 ssl;
    server_name fdfs.91qupaier.com;
    
    ssl_certificate /etc/letsencrypt/live/fdfs.91qupaier.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/fdfs.91qupaier.com/privkey.pem;
    
    location / {
        proxy_pass http://127.0.0.1:8888;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

4. **后端 fdfs_path 改成**：

```properties
system.third.fdfs_path = https://fdfs.91qupaier.com/
```

5. **微信小程序后台**配置 `downloadFile` 合法域名为 `https://fdfs.91qupaier.com`

## 6. 方案 C：集群部署（>1TB 大规模）

不推荐 YPAT 现阶段做。等日活过万、文件量过 TB 再切 OSS。详见 https://github.com/happyfish100/fastdfs/wiki

## 7. 故障排查

### 7.1 上传报 `tracker_query_storage fail, error no: 2`

**原因**：storage 没注册到 tracker。

**诊断**：

```bash
docker logs ypat-fastdfs-storage | grep -E "connect|leader"
# 期望: "successfully connect to tracker server ..."
# 如果没有: storage 根本没找到 tracker
```

**修复**：

```bash
# 1. 确认 tracker 起来了
docker logs ypat-fastdfs-tracker | grep "leader"
# 期望: "I am the new tracker leader ..."

# 2. 确认两个容器在同一网络
docker network inspect fastdfs_fastdfs-net

# 3. 重启 storage
docker restart ypat-fastdfs-storage
sleep 15
```

### 7.2 `curl http://127.0.0.1:8888/xxx` 报 Connection reset

**原因**：端口映射错误（映到了容器内 8888，但镜像内 nginx 监听 8080）。

**修复**：

```yaml
# backend/dev/fastdfs/docker-compose.yml 必须这样写：
ports:
  - "8888:8080"   # 容器内是 8080，不是 8888
```

### 7.3 微信小程序无法下载文件

**症状**：浏览器能下载，小程序里 fail。

**原因**：
1. 用了 HTTP（小程序要求 HTTPS）
2. 没在微信后台配置 `downloadFile` 合法域名

**修复**：
1. 上 HTTPS（见 5.5）
2. 微信公众平台 → 开发 → 开发管理 → 服务器域名 → downloadFile 合法域名 加 `https://fdfs.91qupaier.com`

### 7.4 文件上传成功但下载 404

**原因**：`mod_fastdfs.conf` 里的 `tracker_server` 没指向新 tracker。

**修复**：

```bash
# 进入 storage 容器手动修
docker exec -it ypat-fastdfs-storage sh
sed -i 's|tracker_server=.*|tracker_server=fastdfs-tracker:22122|' /etc/fdfs/mod_fastdfs.conf
nginx -s reload
```

### 7.5 重启后文件丢失

**原因**：docker compose down 删了挂载卷。

**修复**：永远用 `docker compose stop` / `docker compose restart`，不要用 `down -v`（除非你想清空数据）。

## 8. 监控

### 8.1 磁盘使用

```bash
# storage 实际磁盘使用
docker exec ypat-fastdfs-storage df -h /var/fdfs

# 文件数统计
docker exec ypat-fastdfs-storage find /var/fdfs/data -type f | wc -l
```

### 8.2 连接数

```bash
# tracker 当前连接
docker exec ypat-fastdfs-tracker netstat -an | grep 22122 | wc -l
```

### 8.3 健康检查脚本

```bash
#!/bin/bash
# save as ~/check-fastdfs.sh
TRACKER_OK=$(docker exec ypat-fastdfs-tracker pgrep fdfs_trackerd | wc -l)
STORAGE_OK=$(docker exec ypat-fastdfs-storage pgrep fdfs_storaged | wc -l)
NGINX_OK=$(docker exec ypat-fastdfs-storage pgrep nginx | wc -l)

if [ "$TRACKER_OK" -eq 0 ]; then
  echo "❌ tracker not running"
  exit 1
fi
if [ "$STORAGE_OK" -eq 0 ]; then
  echo "❌ storage not running"
  exit 1
fi
if [ "$NGINX_OK" -eq 0 ]; then
  echo "❌ nginx not running"
  exit 1
fi

# 实测上传下载
FILE=$(docker exec ypat-fastdfs-storage sh -c \
  'echo healthcheck > /tmp/h.txt && /usr/bin/fdfs_upload_file /etc/fdfs/client.conf /tmp/h.txt' | grep group1)
if [ -z "$FILE" ]; then
  echo "❌ upload failed"
  exit 1
fi

RESULT=$(curl -sS "http://127.0.0.1:8888/$FILE")
if [ "$RESULT" != "healthcheck" ]; then
  echo "❌ download failed: got '$RESULT'"
  exit 1
fi

echo "✅ FastDFS all OK"
```

挂到 cron：

```bash
crontab -e
*/5 * * * * ~/check-fastdfs.sh || mail -s "FastDFS DOWN" you@example.com < /dev/null
```

## 9. 备份与恢复

### 9.1 备份

```bash
# 停 storage（避免文件写入冲突）
docker stop ypat-fastdfs-storage

# 打包 storage_data
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/backend/dev/fastdfs
tar czf fastdfs-backup-$(date +%Y%m%d).tar.gz storage_data/

# 启 storage
docker start ypat-fastdfs-storage
```

### 9.2 恢复

```bash
# 解压到挂载目录
tar xzf fastdfs-backup-20260627.tar.gz -C backend/dev/fastdfs/
# 重启 storage 让它重新扫描
docker restart ypat-fastdfs-storage
```

### 9.3 迁移到 OSS（终局方案）

YPAT 后期量大了，推荐迁阿里云 OSS：

```java
// 改 FastDFSClient 的实现，内部换 OSS SDK
// 但保留 fdfs_path 接口不变，前端无感
```

## 10. 常见问题

**Q: Mac M1/M2 上能不能跑？**
A: 可以。用 `ygqygq2/fastdfs-nginx` 镜像有 arm64 原生支持。不要用 `delron/fastdfs`（只有 amd64，QEMU 模拟下 mod_fastdfs IO 异常）。

**Q: tracker 和 storage 必须在同一台机器吗？**
A: 不必须。tracker 是无状态的，可以独立部署。storage 必须挂在 tracker 上注册。

**Q: 不用 FastDFS 行不行？**
A: 可以。直接换 OSS（阿里云/腾讯云/S3），改 `FastDFSClient.java` 实现即可，对外接口不变。

**Q: 微信小程序能用吗？**
A: 可以。但必须 HTTPS（见 5.5），且在微信后台配置 `downloadFile` 合法域名。

**Q: 后端代码要不要改？**
A: 不需要。`FastDFSClient.java` 只认 tracker 地址和 fdfs_path，跟实现无关。

---

> 📚 相关文档
> - [LOCAL_DEV_GUIDE.md](./LOCAL_DEV_GUIDE.md) — 本地开发完整流程
> - [DOCKER_DEPLOY_GUIDE.md](./DOCKER_DEPLOY_GUIDE.md) — YPAT 主项目 Docker 部署
> - [DEPLOY_GUIDE.md](./DEPLOY_GUIDE.md) — 阿里云生产部署