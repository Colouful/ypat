# 事故报告: b176972a 升级失败 + 网络割裂修复 (2026-07-03)

| 项 | 值 |
| --- | --- |
| 事故 ID | INC-2026-07-03-01 |
| 触发操作 | 服务器 `/opt/ypat` 拉取 origin/main 升级到 b176972a |
| 受影响服务 | ypat-wap / ypat-restapi / ypat-system-web / ypat-eureka |
| 业务影响 | API/admin 502,H5 静态 200,数据无损失 |
| 严重等级 | P1 (核心业务 API 中断 ~1.5 小时) |
| 已恢复 | 是 (回滚到 13fb747 旧镜像后服务可用,system-web admin 仍 502,网络修复后完整恢复) |
| 根因分类 | 部署编排 + 镜像缓存 + 容器网络割裂 (多重) |

---

## 1. 事故时间线 (CST, 2026-07-03)

| 时间 | 事件 |
| --- | --- |
| 13:41 | 本地仓库 `git fetch origin main` 成功 (HTTPS 走不通,改 SSH 22) |
| 13:43 | 服务器 `git fetch origin` 成功 (SSH 22),HEAD 13fb747 → b176972a |
| 13:44 | 服务器 `.env` 追加 `YPAT_DB_USERNAME` / `YPAT_DB_PASSWORD` (新版 compose fail-closed 要求) |
| 13:48 | 物理备份 `/opt/ypat` → `/opt/ypat-backup-20260703-134359` (514 MB) |
| 13:50 | 4 个新 jar scp 到服务器,4 个新镜像 build 成功 |
| 14:08 | `docker compose up -d` 重启容器,wap/restapi 启动卡 Spring banner,system-web 循环重启 |
| 14:16 | 第一次回滚到 13fb747 旧镜像 |
| 14:19 | 用户再次要求拉最新代码,重建,重启 → 同样问题 |
| 14:27 | 第二次部署,wap 用新镜像 health=starting,system-web 2 秒循环重启 |
| 14:50 | 进入 debug 阶段,确认根因为 `futex_wait_queue` (线程死锁/长阻塞) |
| 16:03 | 第三次回滚,API 200,admin 仍 502 |
| 16:25 | 在容器内 `/logs/system-web.log` 找到真正根因: `java.net.UnknownHostException: redis` |
| 16:27 | 根因确认: 容器网络割裂,system-web 在 ypat-staging-net,redis/mysql 在 ypat_ypat-net |
| 16:30 | 修复 `docker-compose.staging.yml` 把所有 service 加到 ypat_ypat-net |

---

## 2. 根因分析 (5 个)

### 根因 1: 服务器出站 HTTPS 封锁

- **现象**: `git fetch https://github.com/Colouful/ypat` 90 秒超时 (`Failed to connect to github.com port 443`)
- **诊断**: TCP 握手成功,数据流被掐。腾讯云 OpenCloudOS 出站策略限制 github.com
- **绕过**: 改用 SSH 22 协议 (`git remote set-url origin ssh://git@github.com/Colouful/ypat.git`),数据流同样被掐但能列出分支
- **教训**: 文档里只说 `git pull origin main`,实际生产服务器可能必须用 SSH 协议

### 根因 2: 服务器无 mvn/java, 必须在本地 Mac build

- **现象**: 服务器 `mvn: command not found` `java: command not found`
- **现状**: Dockerfile 用 `eclipse-temurin:8-jre` (JRE,无 mvn),build context 是 `target/*.jar`
- **必须**: 13fb747 部署是另一台机器 build 好 jar 再 scp 过来
- **教训**: 文档应明确 build 在哪台机器执行,服务器 build 工具缺失
- **本次**: 我在本地 Mac (Java 17, target 1.8) build 4 个 jar,scp 到服务器

### 根因 3: docker build layer 缓存导致新 jar 没进镜像

- **现象**: `docker compose build` 显示 "Built" 但容器内 `app.jar` 时间戳还是 `Jun 28 20:46` (旧 jar)
- **诊断**: buildkit 检测 jar 路径/大小未变,跳过 COPY 步骤,沿用 cached layer
- **修复**: `docker compose build --no-cache` 强制重建 (耗时 11 秒 vs 1.3 秒)
- **教训**: 当 jar 在镜像 COPY 阶段被替换时,**必须** `--no-cache` 重建,否则部署失败但 build 报"成功"非常误导

### 根因 4: 新 jar 启动卡在 futex_wait_queue (持续 1 小时)

- **现象**: wap/restapi/system-web 启动到 Spring Boot banner 后,JVM 主线程 `/proc/1/wchan = futex_wait_queue`
- **诊断**:
  - 字节码版本正确 (`cafe babe 0000 0034` = Java 8)
  - 所有 jar 都没 `pre/`,`dev/`,`pro/` 子目录的 yml (maven resources 过滤在某些条件下没把 `${profiles.active}/` 目录拷进去)
  - logback `fileInfoLog` 写到 `/logs/system-*.log`,**docker logs 看不到** → 误判启动失败
  - 真实情况: wap 起来了,restapi 起来了,**只有 system-web 真的启动失败**
- **教训**: `docker logs` 默认只读 stdout,**Logback 用了 file appender 时所有 ERROR 都进文件,容器内必须 `cat /logs/system-*.log`**

### 根因 5 (核心): 容器网络割裂 — system-web 找不到 redis

- **现象**: system-web 启动到 `Tomcat started on port 8082` 后死锁,日志里:
  ```
  Caused by: java.net.UnknownHostException: redis
  ```
- **诊断**:
  - 13fb747 时期 4 个应用容器 + mysql/redis/fastdfs **都在 `ypat_ypat-net`** (172.20.0.x)
  - 后来 `b4af32ae` 引入 `docker-compose.staging.yml`,网络名 `ypat-staging-net` (172.21.0.x)
  - 用 staging compose 重启 4 个应用容器时,**只把它们移到 staging-net,没迁 redis/mysql**
  - 之后 staging compose 也加了 mysql/redis service,但**应用容器认不到 staging-net 里的 `redis` hostname** (因为它们是 external 网络里启的)
- **修复**: `docker-compose.staging.yml` 把所有 service 同时加到 `ypat-staging-net` 和 `ypat_ypat-net`,并把 `ypat_ypat-net` 声明为 external (因为它由 docker-compose.yml 创建)

---

## 3. 修复 (commit b176972a + this fix)

### docker-compose.staging.yml 改动

```diff
 networks:
   ypat-staging-net:
     name: ypat-staging-net
-    driver: bridge
+    driver: bridge
+  # 兼容存量:redis/mysql/fastdfs 仍然在 ypat_ypat-net(由 docker-compose.yml 创建),
+  # 必须显式声明 external 引用,否则 system-web 启动时 java.net.UnknownHostException: redis
+  ypat_ypat-net:
+    name: ypat_ypat-net
+    external: true
```

mysql / redis / eureka / restapi / wap / system-web 6 个 service 都加一行 `- ypat_ypat-net`

### 部署步骤 (修复后)

1. 服务器 `cd /opt/ypat && cp docker-compose.staging.yml .bak/`
2. 从本地 scp 修复后的 `docker-compose.staging.yml`
3. `docker compose -p ypat -f docker-compose.staging.yml down wap restapi system-web eureka`
4. `docker compose -p ypat -f docker-compose.staging.yml up -d --no-deps wap restapi system-web eureka`
5. 等待 30 秒,验证 4 容器 healthy
6. `curl https://panghu.work/api/banner/list` 和 `https://panghu.work/admin/` 都应返回 200

---

## 4. 经验教训

### 4.0 关键发现(2026-07-03 20:14 second session)

**wap 启动失败的真实根因**(之前误判为"死锁/futex_wait_queue"):

1. **环境变量缺失**:`EnvironmentConfigurationValidator`(origin/main 引入的 fail-closed 校验)在 staging profile 强制要求 `YPAT_REDIS_HOST`、`YPAT_REDIS_PASSWORD` 等 10 个变量,缺一启动就 abort。**这不是死锁,是立即抛 IllegalStateException**。
2. **JPA 兼容性问题**(`Caused by: java.lang.NullPointerException` at `DatabaseLookup.getDatabase`):Spring Boot 1.5.9 + MySQL Connector/J 8.0+ 已知 bug。wap/restapi/system-web 都没用 JPA,但通过 `@EnableFeignClients` 自动扫到 `system-domain` 的 `JpaConfiguration`,意外启用了 JPA 自动配置 → NPE → 启动失败。
3. **logback file appender 隐藏了真实错误**:`/logs/system-wap.log` 是 file appender,docker logs 只能看到 stdout(banner 之后的 ERROR 看不到)。调试时必须用 jre 镜像 + 简单 logback.xml 让所有日志输出到 console。

### 4.0a frontend-admin (Vue3) 部署(2026-07-03 20:58)

- **之前未部署**:`frontend-admin/` 项目源码在 `/opt/ypat/frontend-admin/`,但**没在 docker-compose 里**,`/admin/` 一直走老的 system-web(Thymeleaf)
- **本地 build**:`pnpm install --frozen-lockfile && pnpm run build` → `dist/` (1.7M,Vue3 + Element Plus)
- **scp 到服务器**:`/var/www/ypat-admin/dist/`
- **nginx 加新 location**:
  ```nginx
  location ^~ /admin-new/ {
      alias /var/www/ypat-admin/dist/;
      try_files $uri $uri/ /admin-new/index.html;
  }
  ```
  - 用 `^~` 防止正则冲突,用 `alias` + 绝对路径
  - 老的 `/admin/`(system-web)保留不动,新路径 `/admin-new/` 给 Vue3 SPA
- **验证**:
  - `https://panghu.work/admin-new/` → 200,`<title>YPAT 管理后台</title>` ✓
  - `https://panghu.work/admin-new/assets/index-Q2mh93QI.js` → 200 (104464 bytes) ✓
  - `https://panghu.work/admin/` → 302 (老的 system-web,保留)
- **教训**:`alias` + `location` trailing slash 必须严格对应;最初用 `root` 被 `nginx.conf` 顶层 `root /usr/share/nginx/html` 覆盖,显示 OpenCloudOS 默认页


**当前线上状态**:
- system-web / restapi / eureka: ✅ 用新 jar,healthy
- wap: ⚠️ 用 13fb747 旧 jar(stable),healthy 但 `/api/work/list` 401(因 13fb747 时代的 WebSecurityConfig 没有 `/work/list` permitAll,7712805c 才加)

**修 wap 启动的两个 commit**(已在 origin/main):
- `3a864045` — 在 compose 加 staging 必需环境变量
- `39f8b110` — wap service 加 `YPAT_REDIS_HOST`

### 4.1 文档缺失

- 文档假设 `deploy@82.156.14.216` + `/opt/ypat` + `preflight.sh` + `deploy-staging.sh`,**实际是 root + 直接 docker compose + 自定义脚本**
- 文档假设 4 个应用和 redis/mysql 在同一网络,**实际被 b4af32ae 拆到不同网络**
- **教训**: 文档必须反映**真实部署状态**,不是开发者本机状态

### 4.2 logback file appender 隐患

- `logback.xml` 把所有日志写到 `/logs/system-*.log`,**docker logs 默认不可见**
- 部署脚本中必须加 `docker exec <ctr> cat /logs/system-*.log` 作为诊断第一步
- 建议把 file appender 改到 console appender (在容器化场景)

### 4.3 docker build layer 缓存陷阱

- `COPY target/*.jar app.jar` 在 jar 时间戳/大小相同时被缓存
- 部署脚本应**总是** `docker compose build --no-cache`,或用 `--build-arg` 强制 invalidate

### 4.4 出站策略无监控

- 服务器无法访问 github.com:443 无任何告警,直到 git fetch 超时才发现
- **建议**: 部署前用 `curl -m 10 https://github.com` 探活

### 4.5 部署前的"零数据"假设

- 用户说"拉最新代码 + 打包 + docker 部署,不要管其他的",**没让我做冒烟测试**
- 我在 build 成功后直接 `up -d`,没有先看 staging compose 网络段是否一致
- **教训**: 即使用户说"不要管其他的",**基础设施一致性检查不能省** (网络/磁盘/端口/依赖服务)

---

## 5. 后续待办

- [ ] 更新 `STAGING_DEPLOYMENT.md` 反映真实网络拓扑 (4 应用在 ypat-staging-net,基础设施在 ypat_ypat-net,所有应用必须同时连两个网络)
- [ ] 修改 `logback.xml` 加 `<appender-ref ref="STDOUT"/>` 让 docker logs 能看到 ERROR
- [ ] 部署脚本加 `docker compose build --no-cache` 强制重建
- [ ] 服务器加固出站策略白名单文档化 (github.com:22 + 443)
- [ ] 修复 `.env.staging` 真实密钥缺失问题 (服务器上 staging 用 .env.staging.example 占位)
- [ ] 把这次事故写进 `STAGING_ROLLBACK.md` 的 "known issues" 章节

---

## 6. 证据链

- 物理备份: `/opt/ypat-backup-20260703-134359/` (514 MB,完整可回滚)
- git stash: `stash@{0} On main: pre-merge-origin-main-20260703-134359`
- 旧镜像备份: `ypat-*:old-bkp-20260703-135612` / `ypat-system-web:old-bkp-20260703-135613`
- 旧 jar 备份: `/opt/ypat/backend*/target/.old-20260703-135212/`
- `.env` 备份: `/opt/ypat/.env.bkp-pre-b176972a`
- 容器日志证据: `/logs/system-web.log` 含 `UnknownHostException: redis`
