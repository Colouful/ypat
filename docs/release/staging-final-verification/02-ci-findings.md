# 旧工作区差异分类

旧 `/opt/ypat` HEAD = `13fb747` (单提交,可能是 `--depth=1` clone)。
新 main HEAD = `f7df2e2` (50+ 提交,含 PR #6/#7 隔离强化)。

## 分类标准

| 类别 | 含义 | 处理 |
| --- | --- | --- |
| A. `ALREADY_UPSTREAM` | 旧目录内容在新 main 中已存在 (相同或被更全面版本替代) | 无需操作,部署后被覆盖 |
| B. `SERVER_RUNTIME_CONFIG` | 服务器特有的密钥 / 真实域名 / 真实端口 | 不入 git,迁移到 `/opt/ypat/.env` 或 `/opt/ypat-config/` |
| C. `UPSTREAM_HOTFIX_REQUIRED` | 服务器有,但新 main 仍缺失的修复 | **本轮 = 0**,无需操作 |
| D. `GENERATED_OR_TEMPORARY` | 构建产物 / 临时文件 | 不入 git |

## A. ALREADY_UPSTREAM (17 项)

### A.1 Tracked,内容完全相同 (7)

| 文件 |
| --- |
| `.dockerignore` |
| `backend/system-wap/src/main/java/com/ypat/controller/UserController.java` |
| `backend/system-wap/src/main/java/com/ypat/util/FastDFSClient.java` |
| `backend/system-wap/src/main/resources/conf/fdfs_client.properties` |
| `backend/system-web/src/main/java/com/ypat/util/FastDFSClient.java` |
| `backend/system-web/src/main/resources/conf/fdfs_client.properties` |
| `frontend/.env.staging.example` |

### A.2 Tracked,旧版为过时草稿 (4) — 部署后覆盖即可

| 文件 | 旧→新 行数 | placeholder 旧→新 |
| --- | --- | --- |
| `docker-compose.yml` | 159 → 246 | 9 → 17 |
| `docker/nginx/default.conf` | 72 → 56 | 0 → 0 |
| `backend/dev/fastdfs/docker-compose.yml` | 43 → 38 | 0 → 0 |
| `backend/system-wap/src/main/resources/conf/sys_conf.properties` | 32 → 36 | 15 → 17 |

新 main 更全面或更外部化。

### A.3 Untracked,服务器与新 main 内容完全相同 (10)

旧目录的 "未跟踪" 文件实际上等于 PR 引入的新文件:

| 文件 |
| --- |
| `backend/dev/fastdfs/docker-compose.staging.yml` |
| `backend/system-restapi/src/main/resources/pre/application.yml` |
| `backend/system-restapi/src/main/resources/pre/bootstrap.yml` |
| `backend/system-restapi/src/main/resources/pre/logback.xml` |
| `backend/system-wap/src/main/resources/pre/bootstrap.yml` |
| `backend/system-wap/src/main/resources/pre/logback.xml` |
| `backend/system-web/src/main/resources/pre/bootstrap.yml` |
| `backend/system-web/src/main/resources/pre/logback.xml` |
| `backend/system-web/src/main/resources/pre/systemprop.yml` |

(实际 9 项,加上 staging compose 共 10)

### A.4 Untracked,服务器版为更早草稿 (6) — 部署后覆盖

| 文件 | 旧→新 行数 |
| --- | --- |
| `backend/system-wap/src/main/resources/pre/application.yml` | 37 → 41 |
| `backend/system-wap/src/main/resources/pre/fdfs_client.properties` | 10 → 10 (1 行差异) |
| `backend/system-wap/src/main/resources/pro/fdfs_client.properties` | 10 → 10 (7 行差异,placeholder 0→1) |
| `backend/system-web/src/main/resources/pre/application.yml` | 40 → 44 |
| `backend/system-web/src/main/resources/pre/fdfs_client.properties` | 11 → 10 |
| `backend/system-web/src/main/resources/pro/fdfs_client.properties` | 11 → 10 |
| `docker-compose.staging.yml` | 178 → 246 (placeholder 14→17) |
| `docker/nginx/panghu.work.conf` | 60 → 84 |

新版引入了更多 `${VAR}` placeholder,说明新 main 已经把硬编码值改为 env 注入。

## B. SERVER_RUNTIME_CONFIG (5)

| 文件 | 在新 main 中 | 说明 |
| --- | --- | --- |
| `frontend/.env.production` | **被删除** | 服务器特有的真实生产 env;由 .env.production.example 模板替代 |
| `.env.production` (untracked) | 不存在 | 服务器侧生产环境 env (内容未读以避免泄露) |
| `.env.example.new` (untracked) | 不存在 | 服务器侧 env 模板草稿 |
| `.claude/settings.local.json` | 不存在 | 服务器侧 Claude Code 工具状态 |
| `frontend/.claude/settings.local.json` | 不存在 | 同上,前端目录 |

## C. UPSTREAM_HOTFIX_REQUIRED (0)

无。所有服务器 tracked 修改都已通过 PR #6/#7 进入 main,且新版本更全面。

## D. GENERATED_OR_TEMPORARY (12)

12 个 `target/` 目录(各 Maven 模块构建产物),已在 `.gitignore` 内。
未发现 `node_modules` / `dist` (说明前端最近构建并未在 `/opt/ypat` 中执行)。

## 结论

旁路部署**不会丢失任何 server-only fix**。所有有价值的修改都已在新 main 中。
B 类的 5 个服务器 runtime 文件只是密钥/工具状态,不应进入 git。
