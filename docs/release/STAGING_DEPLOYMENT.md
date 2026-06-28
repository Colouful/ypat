# STAGING 部署指南

## 环境定义

- **域名**: panghu.work
- **服务器**: 82.156.14.216
- **角色**: 预发环境
- **操作系统**: OpenCloudOS

## 部署架构

```
公网 80/443
  ↓
主机 Nginx (systemctl)
  ├─ TLS 终止
  ├─ 静态文件 (/var/www/panghu.work)
  └─ 反代
      ├─ /api/ → 127.0.0.1:8081 (wap)
      ├─ /admin/ → 127.0.0.1:8082 (system-web)
      └─ /files/ → 127.0.0.1:8888 (fastdfs-storage)

Docker 网络: ypat_ypat-net
  ├─ mysql:3306
  ├─ redis:6379
  ├─ eureka:8761
  ├─ restapi:9081
  ├─ wap:8081
  ├─ system-web:8082
  └─ fastdfs-tracker:22122
      fastdfs-storage:8080
```

## 部署前检查

### 1. 运行预检脚本

```bash
./scripts/deploy/preflight.sh
```

### 2. 检查环境变量

```bash
# 服务器 .env 文件权限
stat -c '%a %U %G %n' /opt/ypat/.env
# 应该显示: 600 root root
```

### 3. 备份当前状态

```bash
# 备份数据库
docker exec ypat-mysql mysqldump -u root -p'$MYSQL_PASSWORD' ypat | gzip > /opt/ypat-data/backups/mysql/backup_$(date +%Y%m%d_%H%M%S).sql.gz

# 备份前端
cp -r /var/www/panghu.work /opt/ypat-data/backups/frontend_$(date +%Y%m%d_%H%M%S)
```

## 部署流程

### 1. 构建前端

```bash
cd frontend
pnpm install --frozen-lockfile
pnpm run build:h5:staging
```

### 2. 构建后端

```bash
cd backend
mvn clean package -Ppre -DskipTests=false
```

### 3. 部署前端

```bash
# 备份当前前端
mv /var/www/panghu.work /opt/ypat-data/backups/frontend_$(date +%Y%m%d_%H%M%S)

# 部署新前端
mkdir -p /var/www/panghu.work
cp -r dist/build/h5/* /var/www/panghu.work/
```

### 4. 构建 Docker 镜像

```bash
cd /opt/ypat
docker compose -f docker-compose.staging.yml build
```

### 5. 启动服务

```bash
docker compose -f docker-compose.staging.yml up -d
docker compose -f backend/dev/fastdfs/docker-compose.staging.yml up -d
```

### 6. 安装 Nginx 配置

```bash
./scripts/deploy/install-nginx-config.sh
```

### 7. 验证部署

```bash
# 检查服务健康
curl -I https://panghu.work/
curl -I https://panghu.work/api/banner/list
curl -I https://panghu.work/admin/

# 检查响应头
curl -I https://panghu.work/ | grep -i "strict-transport-security"
```

## 域名配置

### panghu.work

```nginx
server {
    listen 80;
    server_name panghu.work www.panghu.work;
    return 301 https://panghu.work$request_uri;
}

server {
    listen 443 ssl;
    server_name panghu.work;
    ssl_certificate /etc/nginx/ssl/panghu.work/panghu.work_bundle.crt;
    ssl_certificate_key /etc/nginx/ssl/panghu.work/panghu.work.key;
    # ...
}
```

### www.panghu.work

www.panghu.work 自动 301 重定向到 panghu.work。

## 端口分配

| 服务 | 监听地址 | 公网暴露 |
| --- | --- | --- |
| 主机 Nginx (HTTP) | 0.0.0.0:80 | ✓ |
| 主机 Nginx (HTTPS) | 0.0.0.0:443 | ✓ |
| MySQL | 127.0.0.1:3306 | ✗ |
| Redis | 127.0.0.1:6379 | ✗ |
| Eureka | 127.0.0.1:8761 | ✗ |
| REST API | 不映射 | ✗ |
| WAP | 127.0.0.1:8081 | ✗ |
| System Web | 127.0.0.1:8082 | ✗ |
| FastDFS Tracker | 容器内 22122 | ✗ |
| FastDFS Storage | 容器内 23000 | ✗ |
| FastDFS HTTP | 127.0.0.1:8888 | ✗ |

## 运维访问

### Eureka 控制台

Eureka 不对公网开放，需通过 SSH 隧道：

```bash
ssh -L 8761:127.0.0.1:8761 root@82.156.14.216
# 浏览器访问: http://127.0.0.1:8761
```

### 数据库

```bash
ssh -L 3306:127.0.0.1:3306 root@82.156.14.216
mysql -h 127.0.0.1 -u root -p
```

### Redis

```bash
ssh -L 6379:127.0.0.1:6379 root@82.156.14.216
redis-cli -h 127.0.0.1 -a "$YPAT_LOCAL_REDIS_PASSWORD"
```

## 故障排查

### 服务无法启动

```bash
# 查看容器日志
docker logs ypat-wap
docker logs ypat-mysql

# 查看资源使用
docker stats
```

### 数据库连接失败

```bash
# 检查数据库状态
docker exec ypat-mysql mysqladmin ping -h localhost

# 查看连接数
docker exec ypat-mysql mysql -u root -p -e "SHOW PROCESSLIST;"
```

### Nginx 配置错误

```bash
nginx -t
systemctl status nginx
tail -f /var/log/nginx/panghu.work.error.log
```

## 监控指标

- CPU 使用率
- 内存使用率
- 磁盘使用率
- Docker 容器健康状态
- HTTPS 证书有效期
- API 响应时间