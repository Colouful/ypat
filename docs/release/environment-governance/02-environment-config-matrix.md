# Environment Configuration Matrix

| 维度 | development | staging | production |
|------|-------------|---------|------------|
| **域名** | localhost | panghu.work | TBD (独立域名) |
| **服务器** | 开发机 | 82.156.14.216 | TBD |
| **Docker 网络** | ypat_ypat-net | ypat-staging-net | ypat-production-net |
| **MySQL 卷** | mysql_data | ypat-staging-mysql-data | ypat-production-mysql-data |
| **Redis 卷** | redis_data | ypat-staging-redis-data | ypat-production-redis-data |
| **FastDFS 数据** | ./backend/dev/fastdfs/{tracker,storage}_data | /opt/ypat-data/staging/fastdfs/{tracker,storage} | /opt/ypat-data/production/fastdfs/{tracker,storage} |
| **MySQL 端口** | 127.0.0.1:3306 | 127.0.0.1:3306 | 127.0.0.1:3306 |
| **Redis 端口** | 127.0.0.1:6379 | 127.0.0.1:6379 | 127.0.0.1:6379 |
| **Eureka 端口** | 127.0.0.1:8761 | 127.0.0.1:8761 | 127.0.0.1:8761 |
| **WAP 端口** | 127.0.0.1:8081 | 127.0.0.1:8081 | 127.0.0.1:8081 |
| **Web 端口** | 127.0.0.1:8082 | 127.0.0.1:8082 | 127.0.0.1:8082 |
| **FastDFS 端口** | 127.0.0.1:8888 | 127.0.0.1:8888 | TBD |
| **公网 Nginx** | 无 | 主机 nginx (443) | TBD |
| **前端 API URL** | http://localhost:8088 | https://panghu.work/api | https://TBD/api |
| **前端 Image URL** | http://localhost:8088/ | https://panghu.work/files | https://TBD/files |
| **后端 Profile** | dev | pre | pro |
| **MySQL 密码** | dev_password_change_me | YPAT_LOCAL_MYSQL_ROOT_PASSWORD | YPAT_LOCAL_MYSQL_ROOT_PASSWORD |
| **Redis 密码** | dev_password_change_me | YPAT_LOCAL_REDIS_PASSWORD | YPAT_LOCAL_REDIS_PASSWORD |
| **业务账号** | ypat_dev | TBD | ypat_app + ypat_migrate |
| **FastDFS tracker** | ypat-fastdfs-tracker:22122 | fastdfs-tracker:22122 | TBD |
| **FastDFS public URL** | http://localhost:8888 | https://panghu.work/files | https://TBD/files |
| **JWT signing key** | dev-only | YPAT_SSO_JWT_SIGNING_KEY | YPAT_SSO_JWT_SIGNING_KEY |
| **SMS Mock** | allowed | forbidden | forbidden |
| **部署脚本** | docker compose up | scripts/deploy/deploy-staging.sh | scripts/deploy/deploy-production.sh --confirm-production |
| **回滚脚本** | docker compose restart | scripts/deploy/rollback-staging.sh | scripts/deploy/rollback-production.sh --confirm-rollback |
| **预检脚本** | 无（本地） | scripts/deploy/preflight.sh --env staging | scripts/deploy/preflight-production.sh |
| **CI 环境** | frontend-development + backend-build-dev + compose-config | frontend-staging + backend-build-pre | frontend-production + backend-build-pro |

## 禁止矩阵

| 操作 | dev | staging | production |
|------|-----|---------|------------|
| 使用 panghu.work | ✓ (in code as example) | ✓ | ❌ |
| 使用 www.panghu.work | ❌ | ✓ | ❌ |
| 使用 82.156.14.216 | ❌ | ✓ | ❌ |
| 使用 localhost | ✓ | ❌ | ❌ |
| 使用 127.0.0.1 | ✓ | ❌ | ❌ |
| 使用 HTTP（前端） | ✓ | ❌ | ❌ |
| :latest 镜像 | ❌ | ❌ | ❌ |
| SMS Mock | ✓ | ❌ | ❌ |
| 用 MySQL root 给应用 | ✓ (本地) | ❌ | ❌ |
| placeholder 值（CHANGE_ME 等） | ✓ (示例文件) | ❌ | ❌ |
| 直接 push main | ❌ | ❌ | ❌ |
| 自动部署 | ✓ | ⚠️ (需 preflight) | ❌ (必须 --confirm-production) |
| 自动回滚 DB | ❌ | ❌ | ❌ |