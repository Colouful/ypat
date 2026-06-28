# 环境隔离说明

## 环境定义

| 环境 | 域名 | 服务器 | 用途 |
| --- | --- | --- | --- |
| development | 本地 | 本地 | 开发调试 |
| staging | panghu.work | 82.156.14.216 | 预发环境 |
| production | TODO | TODO | 正式生产环境 |

## 预发环境配置

### 域名

- **panghu.work** → 预发服务（HTTPS）
- **www.panghu.work** → 301 重定向到 panghu.work

### 服务器

- **IP**: 82.156.14.216
- **OS**: OpenCloudOS
- **角色**: 预发服务器

### 网络安全

- 仅主机 Nginx 对公网开放 80、443
- 其他服务（MySQL、Redis、Eureka、FastDFS）只绑定 127.0.0.1
- Redis 已启用密码认证
- Eureka 不对公网开放

### 数据库

- 预发环境独立数据库
- 禁止连接生产数据库
- 备份路径: `/opt/ypat-data/backups/mysql`

### FastDFS

- 数据目录: `/opt/ypat-data/fastdfs`
- 内部地址: `fastdfs-tracker:22122`
- 公开地址: `https://panghu.work/files`
- 镜像 digest 已固定

## 生产环境隔离要求

未来正式生产环境必须具备：

1. **独立域名**（如 www.panghu.work）
2. **独立服务器或独立容器组**
3. **独立数据库**
4. **独立 Redis**
5. **独立 FastDFS**
6. **独立静态目录**
7. **独立环境变量**
8. **独立 SSL 证书**

禁止事项：

- 禁止继续使用 `panghu.work = staging` 和 `www.panghu.work = production` 这种伪隔离方案
- 禁止生产环境连接预发环境数据库
- 禁止生产环境使用预发环境文件存储
- 禁止在预发环境执行生产环境操作

## 前端环境配置

### .env.staging

```env
VITE_APP_ENV=staging
VITE_API_BASE_URL=https://panghu.work/api
VITE_IMAGE_BASE_URL=https://panghu.work/files
```

### .env.production

生产环境配置必须使用独立域名，例如：

```env
VITE_APP_ENV=production
VITE_API_BASE_URL=https://www.panghu.work/api
VITE_IMAGE_BASE_URL=https://www.panghu.work/files
```

**注意**: 当前 `.env.production` 仅作占位，生产域名确定后需更新。

## 后端环境配置

### Maven Profile

- `dev`: 本地开发（默认）
- `pre`: 预发环境
- `pro`: 生产环境

### 构建命令

```bash
# 预发环境
mvn clean package -Ppre

# 生产环境
mvn clean package -Ppro
```

### 环境变量

预发环境必需的环境变量：

```env
YPAT_FDFS_PUBLIC_BASE_URL=https://panghu.work/files
YPAT_LOCAL_MYSQL_ROOT_PASSWORD=CHANGE_ME_STRONG_PASSWORD
YPAT_LOCAL_REDIS_PASSWORD=CHANGE_ME_STRONG_REDIS_PASSWORD
```

## 运维访问

### Eureka 控制台

预发环境 Eureka 不对公网开放，运维需通过 SSH 隧道访问：

```bash
ssh -L 8761:127.0.0.1:8761 root@82.156.14.216
```

然后访问: `http://127.0.0.1:8761`

### 数据库

预发环境 MySQL 只绑定 127.0.0.1，运维需通过 SSH 隧道访问：

```bash
ssh -L 3306:127.0.0.1:3306 root@82.156.14.216
```

### Redis

预发环境 Redis 只绑定 127.0.0.1，运维需通过 SSH 隧道访问：

```bash
ssh -L 6379:127.0.0.1:6379 root@82.156.14.216
```

使用密码连接：

```bash
redis-cli -h 127.0.0.1 -p 6379 -a "${YPAT_LOCAL_REDIS_PASSWORD}"
```

## 环境切换检查清单

### 从 staging 切换到 production

- [ ] 确认生产域名已配置
- [ ] 确认生产服务器已部署
- [ ] 确认生产数据库已初始化
- [ ] 确认生产 Redis 已配置
- [ ] 确认生产 FastDFS 已部署
- [ ] 更新 `.env.production` 配置
- [ ] 执行生产环境构建
- [ ] 执行生产环境部署
- [ ] 执行冒烟测试
- [ ] 执行数据验证

### 从 production 回滚到 staging

- [ ] 通知相关人员
- [ ] 切换 DNS 或负载均衡配置
- [ ] 验证 staging 环境
- [ ] 执行冒烟测试
- [ ] 通知切换完成

## 变更历史

| 日期 | 变更内容 | 负责人 |
| --- | --- | --- |
| 2025-12-22 | 创建环境隔离文档 | Hermes |