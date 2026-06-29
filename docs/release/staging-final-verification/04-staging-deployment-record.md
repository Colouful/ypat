# Staging 部署记录

## 部署状态

**未部署 — STATEFUL_MIGRATION_REQUIRED**

详见 [08-open-items.md](./08-open-items.md)。

## 准备就绪情况

| 项 | 状态 |
| --- | --- |
| 干净 main clone | ✅ `/opt/ypat-src/f7df2e2ba8477904fc045a4e07a625ece7e7ae38` |
| HEAD = CI 全绿 SHA | ✅ f7df2e2 |
| Worktree clean | ✅ |
| `/opt/ypat/.env` 权限 | ✅ 600 |
| `.env` 必填变量 | ❌ 缺 YPAT_DB_NAME / YPAT_DB_USERNAME / YPAT_DB_PASSWORD |
| MySQL 业务账号 | ❌ 不存在 |
| 数据迁移完成 | ❌ 未执行 |
| FastDFS 网络桥接 | ❌ 未执行 |

## Compose 视图(新栈)

```
project = ypat-staging
services:
  - mysql      (volume ypat-staging-mysql-data) [empty]
  - redis      (volume ypat-staging-redis-data) [empty]
  - eureka     (build local; expose 127.0.0.1:8761)
  - restapi    (build local; expose 9081 internal only)
  - wap        (build local; expose 127.0.0.1:8081)
  - system-web (build local; expose 127.0.0.1:8082)
network = ypat-staging-net
```

FastDFS (独立 compose,`backend/dev/fastdfs/docker-compose.staging.yml`):
```
network = ypat_ypat-net (external, 已存在)
container_name = ypat-fastdfs-tracker / ypat-fastdfs-storage
volumes = bind /opt/ypat-data/fastdfs/{tracker,storage} (与现网一致,安全)
storage 端口 = 127.0.0.1:8888
```

## 现网栈 (`ypat`,运行中)

```
ypat-mysql-1       mysql:?            volume ypat_mysql_data (15 张表)
ypat-redis-1       redis:?            volume ypat_redis_data
ypat-eureka-1      build
ypat-restapi-1     build
ypat-wap-1         build
ypat-system-web-1  build
network            ypat_ypat-net (172.20.0.0/16)
```

FastDFS 同上(共享 ypat_ypat-net)。

## 部署执行(留空,待迁移完成后填入)

| 项 | 值 |
| --- | --- |
| 部署 SHA | — |
| 镜像 tag | — |
| 镜像 digest | — |
| 前端 release | — |
| 上一个 release | — |
| Compose project | — |
| MySQL volume | — |
| Redis volume | — |
| FastDFS 数据目录 | — |
| 部署开始 | — |
| 部署结束 | — |
| 是否触发自动回滚 | — |

## 备份就绪

| 备份 | 状态 |
| --- | --- |
| 旧工作区(repo state) | ✅ `/opt/ypat-data/backups/repository-state/20260629_180034` 12 MB,校验通过 |
| MySQL 全量 dump | ⏸ 待 Phase 0 执行 |
| Nginx 配置 | ⏸ 待 Phase 0 执行 |
| FastDFS rsync 快照 | ⏸ 待 Phase 0 执行 |
