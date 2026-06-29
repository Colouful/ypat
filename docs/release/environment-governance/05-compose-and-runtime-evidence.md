# Compose and Runtime Evidence

**日期**: 2026-06-29

## 1. docker-compose.yml（development）验证

```bash
$ YPAT_LOCAL_MYSQL_ROOT_PASSWORD=test \
  YPAT_LOCAL_REDIS_PASSWORD=test \
  YPAT_DB_USERNAME=ypat YPAT_DB_PASSWORD=test \
  docker compose config
```

**结果**: RC=0，services 列表 = [mysql, redis, eureka, restapi, wap, system-web, nginx]

## 2. docker-compose.staging.yml 验证

```bash
$ YPAT_LOCAL_MYSQL_ROOT_PASSWORD=test \
  YPAT_LOCAL_REDIS_PASSWORD=test \
  YPAT_DB_USERNAME=ypat YPAT_DB_PASSWORD=test \
  YPAT_DB_NAME=ypat YPAT_MYSQL_URL=jdbc:mysql://mysql:3306/ypat \
  YPAT_FASTDFS_TRACKER_DATA_DIR=/tmp YPAT_FASTDFS_STORAGE_DATA_DIR=/tmp \
  YPAT_SSO_JWT_SIGNING_KEY=dummy \
  docker compose -f docker-compose.staging.yml config
```

**结果**: RC=0，services = [mysql, redis, eureka, restapi, wap, system-web]
- 网络 ypat-staging-net
- 卷 ypat-staging-mysql-data, ypat-staging-redis-data
- 所有端口绑 127.0.0.1
- Redis 使用 requirepass 注入密码
- resource limits + logging driver + security_opt 全部生效

## 3. docker-compose.production.yml 验证

```bash
$ 注入完整生产环境变量 \
  docker compose -f docker-compose.production.yml config
```

**结果**: RC=0（全部变量注入后）
**fail-closed 验证**（不注入必填变量时）：
```
error while interpolating services.restapi.environment.SPRING_DATASOURCE_URL: 
required variable YPAT_MYSQL_URL is missing a value: YPAT_MYSQL_URL is required
```

## 4. FastDFS compose 验证

| 文件 | 验证结果 |
|------|----------|
| `backend/dev/fastdfs/docker-compose.yml` (dev) | ✅ RC=0 |
| `backend/dev/fastdfs/docker-compose.staging.yml` (staging) | ✅ RC=0 |
| `backend/dev/fastdfs/docker-compose.production.yml` (prod) | ✅ RC=0 |

## 5. Redis 密码启用验证

`docker-compose.yml`:
```yaml
redis:
  command:
    - redis-server
    - --appendonly
    - "yes"
    - --requirepass
    - "${YPAT_LOCAL_REDIS_PASSWORD:?YPAT_LOCAL_REDIS_PASSWORD is required}"
```

healthcheck:
```yaml
test: ["CMD", "redis-cli", "-a", "${YPAT_LOCAL_REDIS_PASSWORD}", "ping"]
```

✅ 必须注入 YPAT_LOCAL_REDIS_PASSWORD，否则启动失败

## 6. Dockerfile ENTRYPOINT / Compose command 冲突验证

修复前（PR5 staging compose 旧版）:
```yaml
command: ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=pre", "-jar", "app.jar"]
```

Dockerfile:
```dockerfile
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
```

实际启动命令（错误）：
```
java -Djava.security.egd=file:/dev/./urandom -jar app.jar java -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=pre -jar app.jar
```

修复后（PR 治理版 staging compose）:
```yaml
environment:
  SPRING_PROFILES_ACTIVE: pre
  # 不再设置 command
```

Dockerfile 保留 `ENTRYPOINT ["java", ..., "-jar", "app.jar"]`，实际启动命令：
```
java -Djava.security.egd=file:/dev/./urandom -jar app.jar
```

## 7. Resource Limits 验证

每个应用服务都有：
```yaml
deploy:
  resources:
    limits:
      memory: 1G  # 或 2G
```

✅ 防止内存爆炸

## 8. Logging 验证

```yaml
logging:
  driver: json-file
  options:
    max-size: "20m"
    max-file: "5"
```

✅ 日志轮转，防止磁盘填满

## 9. Security Opt 验证

```yaml
security_opt:
  - no-new-privileges:true
```

✅ 禁止容器内权限提升