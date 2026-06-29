# 运行时验证 — 待部署后填入

## 状态

**未执行 — STATEFUL_MIGRATION_REQUIRED**

迁移与部署完成后,按 [README 任务的第十六节](../STAGING_DEPLOYMENT.md) 执行下列验证。

## 已验证项 (旧栈)

| 项 | 结果 | 命令 |
| --- | --- | --- |
| Redis 强制密码 | ✓ NOAUTH on no-pass, PONG on auth | `docker exec ypat-redis-1 redis-cli ping` |
| WAP Redis 密码注入 | ✓ PASSWORD_SET | `docker exec ypat-wap-1 sh -c 'test -n "$SPRING_REDIS_PASSWORD"'` |
| WAP Redis HOST 变量 | ⚠ MISSING (但应用走 application.yml fallback) | `docker exec ypat-wap-1 sh -c 'echo $SPRING_REDIS_HOST'` |
| MySQL ypat 表数 | 15 | — |
| FastDFS 容器健康 | ✓ tracker + storage running | `docker ps` |

## 待验证项 (新栈,部署后执行)

### 16.1 Docker 启动参数

```bash
for c in ypat-staging-wap-1 ypat-staging-restapi-1 ypat-staging-system-web-1; do
  echo "=== $c ==="
  docker inspect "$c" --format '{{json .Config.Entrypoint}} {{json .Config.Cmd}}'
done
```

预期: Java 启动命令仅出现一次(Dockerfile 提供 ENTRYPOINT,compose 不重复)。

### 16.2 Spring Profile

```bash
for c in ypat-staging-wap-1 ypat-staging-restapi-1 ypat-staging-system-web-1; do
  docker exec "$c" sh -c 'echo "${SPRING_PROFILES_ACTIVE:-MISSING}"'
done
```

预期: 全部输出 `pre`。

### 16.3 容器健康

```bash
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}' \
  | grep -E "ypat-staging|ypat-fastdfs"
```

预期: 6 个新 staging 容器 + 2 个 FastDFS 全部 `Up (healthy)`。

### 16.4 端口

```bash
ss -lntp
```

预期对外仅 80/443/SSH;内部端口仅 127.0.0.1 绑定。

### 16.5 HTTPS / API

```bash
curl -fsS -o /dev/null -w '%{http_code}\n' https://panghu.work/
curl -fsS -o /dev/null -w '%{http_code}\n' https://panghu.work/api/banner/list
curl -sS -o /dev/null -w '%{http_code}\n' https://panghu.work/admin/
curl -I http://panghu.work/         # 应 301 → https
curl -I https://www.panghu.work/    # 应 301 → 主域
curl -I https://panghu.work/eureka/ # 应被拒(无公网暴露)
```

### 16.6 FastDFS

- 历史文件可访问: 选取 `select url from ypat.banner` 中的图片 URL,curl 验证 200
- 新上传测试: 通过 WAP 接口上传一张测试图,确认 URL 走 `https://panghu.work/files/`
- URL 不含 `:8888` 也不含服务器 IP

### 16.7 日志扫描

```bash
docker logs --tail 200 ypat-staging-restapi-1 2>&1 \
  | grep -E "ERROR|Exception|NOAUTH|Connection refused|Access denied|Unknown database|Could not resolve placeholder|BeanCreationException" \
  | head -50
```

预期: 0 命中。如有命中,需脱敏后归档。
