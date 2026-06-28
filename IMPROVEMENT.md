# IMPROVEMENT.md — 踩坑记录 & 历史教训

> 本文件记录开发过程中遇到的坑、已知问题和反模式。
> AI Agent 在开发时应阅读本文件，避免重蹈覆辙。

---

## 前端踩坑

### [2020] 91pai-master 旧代码问题

| 问题 | 详情 | 教训 |
|------|------|------|
| Vue 2 + UniApp 2.0 技术栈过旧 | 2020 年的代码，依赖 vue-cli，无法使用 Vite 等现代工具链 | 新版已升级 Vue 3 + UniApp 3.0 + Vite |
| city.data.js 体积 987KB | 全量城市数据打包进前端，严重影响加载性能 | 新版应按需加载或从后端获取 |
| Vuex 状态管理混乱 | 旧版 store 结构不清晰，缺少模块化 | 新版考虑 Pinia 或组合式 API |
| 旧版 H5 编译需要 `--openssl-legacy-provider` | Node 17+ 不兼容旧 webpack | 新版用 Vite 彻底解决 |
| `mine的副本` 目录残留 | 代码目录出现中文命名副本，说明开发流程不规范 | 统一使用英文命名，代码审查时注意 |

### [旧] 接口联调问题

| 问题 | 教训 |
|------|------|
| request.js 无统一错误处理 | 新版必须封装统一的请求拦截器，处理 401/403/500 |
| 接口无 TypeScript 类型定义 | 新版所有 API 响应必须定义 interface |
| 图片上传无进度反馈 | 新版需加上传进度条和失败重试 |

---

## 后端踩坑

### [旧] Spring Cloud 微服务问题

| 问题 | 教训 |
|------|------|
| Eureka 注册中心单点故障 | 生产环境需部署集群模式 |
| Hystrix 已停止维护 | 新版考虑迁移到 Sentinel 或 Resilience4j |
| 配置中心无版本管理 | 配置变更需记录，方便回滚 |
| FastDFS 文件存储无 CDN | 图片加载慢，需加 CDN 加速 |

### [2026-06-28] /user/upd 接口返回 `{code:1002, msg:null, res:null}` — 症状掩盖型 bug

**症状**：用户编辑资料（含换头像）时，前端调用 `POST /user/upd`，后端返回 `{"code":1002,"msg":null,"res":null}`，前端 toast 提示"参数错误"，无法定位真实原因。

**根因链（4 层，全在一次提交修复）**：

| # | 问题 | 位置 | 影响 |
|---|------|------|------|
| 1 | `fdfs_client.properties` 配 `fastdfs.tracker_servers = 127.0.0.1:22122`，Docker 容器内 `127.0.0.1` 指向容器自己 | `backend/system-wap/src/main/resources/conf/fdfs_client.properties`<br>`backend/system-web/src/main/resources/conf/fdfs_client.properties` | FastDFS 上传 → `IOException: recv package size -1 != 10` → `uploadFile()` 返回 null |
| 2 | `FastDFSClient.uploanFile1()` 对 `null` 返回值无检查，直接 `result[0]+File.separator+result[1]` 抛 NPE | `backend/system-wap/src/main/java/com/ypat/util/FastDFSClient.java`<br>`backend/system-web/src/main/java/com/ypat/util/FastDFSClient.java` | NPE 上抛 |
| 3 | `UserController.upd()` / `upd2()` 同样不检查 `uploanFile1` 返回值，且 `pics.split(",")` 没限制切分次数（`OauthController` 的正确写法是 `split(",", 2)`） | `backend/system-wap/src/main/java/com/ypat/controller/UserController.java` | NPE 上抛到 controller，未被业务异常包装 |
| 4 | `SysExceptionHandler` 把所有未预期异常（包括 NPE）都归类为 `FAIL_PARA(1002)`，且 `exception.getMessage()` 对 NPE 是 null | `backend/system-wap/src/main/java/com/ypat/handler/SysExceptionHandler.java` | **前端只看到 1002 + null msg，完全无法定位是 FastDFS / controller / 校验 哪个环节出错** |

**修复**：
- (1) `127.0.0.1:22122` → `ypat-fastdfs-tracker:22122`（容器名走 Docker DNS）
- (2) `uploanFile1` 加 null 检查，返回 null 让调用方处理
- (3) `UserController.upd/upd2` 改 `split(",", 2)`；null 检查后抛 `SysException(FAIL_MARK)` 而不是 NPE
- (4) 暂未改 SysExceptionHandler（范围控制，但**应该**加 `NPE` 专门处理并打 ERROR 日志 + 返回 500）

**教训**：
- ❌ **症状式错误码是调试灾难**：`SysExceptionHandler` 的"未预期异常全归 1002"模式让所有非业务异常都变成"参数错误"，前端无法区分"参数真错"和"内部错误"。**新业务异常必须用专用 code（如 1015=水印失败），绝不能复用 1002**。
- ❌ **裸 NPE 上抛到 controller = 架构缺陷**：底层工具类的失败必须用异常或 Result 对象表达，不允许返回 null 让调用方自己处理。
- ❌ **配置文件不能用 `127.0.0.1` 表示"本机服务"**：在 Docker / k8s / 容器化部署中，`127.0.0.1` 只指向容器自己。**必须用服务名（DNS）或主机 IP**。

---

## 调试方法论教训

### [2026-06-28] AI 调试浪费 35% token 的反思

**症状**：用户报告"单个接口 1002 错误"，最终定位涉及 4 层 bug + 1 个环境问题，消耗 35% 单次会话 token。

**错在哪**：
1. **没有 tight feedback loop**：上来就 `search_files` 全项目搜 `1002` 错误码，从 `ResponseCode.java` 一路读回 controller，再读前端 API，**没有先发一个 curl 复现确认能 100% 复现再分析代码**。
2. **Token 显示渲染陷阱**：bash 输出里 JWT 中间段被显示成 `eyJhbG...9OqA`，误以为是后端截断，**反复用 base64/xxd/od 验证**，实际 token 一直完整。**正确的应对是 `od -c` 或 `wc -c` 直接看字节数，不要相信省略号**。
3. **场景没聚焦**：发了一组 5-6 个不同测试（form/JSON/空 body/含 pics/含 undefined 字符串），应该**先发一个最接近用户实际场景的请求**确认能复现，再发变体。
4. **修改范围失控**：根因只是 properties 一行 `127.0.0.1`，但同时改了 5 个文件（properties ×2 + FastDFSClient ×2 + UserController）。**应该先小步修复-验证-再扩展**。
5. **没先看环境状态**：用户报告"1002"时，应该**先 `docker ps` 确认所有服务在跑**，发现 restapi 处于 unhealthy 状态（mysql 密码问题），能省掉至少 50% 的 token。

**对（应该怎么做）**：
1. **30 秒内复现**：用用户提供的请求（URL + method + body）发 curl，确认能 100% 复现。
2. **5 分钟内读后端日志**：`docker logs <容器>` 看 ERROR 堆栈，定位代码行。
3. **改 1 个文件，验证 1 次**：小步走。
4. **Chat 显示省略号时立即用字节级工具确认**：`od -c`、`wc -c`、`xxd`。
5. **复杂环境先 `docker ps` / `docker stats` 看状态**。

**对项目本身的教训**：
- ✅ **SysExceptionHandler 应该按异常类型分桶**：`BindException` / `MethodArgumentNotValidException` / `ConstraintViolationException` 才归 1002；NPE / IOException / RuntimeException 归 500（FAIL_SER）；并在日志中打印完整堆栈（当前代码只打 `exception.getMessage()`，丢了堆栈）。
- ✅ **所有调用 FastDFS / 外部服务的代码必须 try-catch**，抛业务异常而非裸 NPE。
- ✅ **`split(",")` 一律加 `, 2`**，避免 base64 数据里含逗号时被切错。

---

## 部署踩坑

| 问题 | 教训 |
|------|------|
| 服务器资源有限 (5台2核4G) | 微服务需精简，非核心组件考虑合并部署 |
| 无 CI/CD 流程 | 需搭建自动构建部署流水线 |
| 数据库无备份策略 | 必须配置定时备份 |

---

## 反模式清单 (禁止)

- ❌ 不要直接在页面中写 `uni.request()`，必须使用封装的请求工具
- ❌ 不要在 template 中写复杂逻辑，抽成 computed 或方法
- ❌ 不要使用 `any` 类型，所有数据必须有明确类型
- ❌ 不要硬编码颜色值，使用 uni.scss 中的 CSS 变量
- ❌ 不要跳过 TypeScript 类型检查 (`// @ts-ignore`)
- ❌ 不要在一个页面文件超过 300 行，及时拆分组件
- ❌ 不要在旧代码 (91pai-master) 上直接修改，新版在 frontend/ 中独立开发
- ❌ **不要让底层工具类返回 null 后让调用方裸用** — 必须用异常或 `Result<T>` 包装（参考 `FastDFSClient.uploanFile1` 的 1002 bug 教训）
- ❌ **不要把未预期异常（NPE/IOException）归类为"参数错误"** — `SysExceptionHandler` 必须按异常类型分桶，避免掩盖真因
- ❌ **不要在容器化部署的配置文件里用 `127.0.0.1` 表示"本机服务"** — 用服务名走 Docker DNS（如 `ypat-fastdfs-tracker:22122`）
- ❌ **不要用 `split(",")` 处理 base64 数据** — 一律用 `split(",", 2)`，避免 base64 里含逗号时被切错段

---

## 已知待解决问题

1. **后端接口文档不完整** — 部分接口缺少入参/出参说明
2. **无自动化测试** — 前后端均缺少单元测试和集成测试
3. **微信支付配置** — 需要申请商户号才能联调
4. **百度 AI OCR** — 需要申请 API Key 才能使用实名认证功能
5. **FastDFS 部署** — 服务器上需配置文件存储服务
6. **[2026-06-28] SysExceptionHandler 异常分桶** — 当前把所有未预期异常归 1002，建议按异常类型分桶（NPE/IOException→500，校验异常→1002）并打印完整堆栈
7. **[2026-06-28] OauthController/YpatInfoController/FileUploadController 也有 NPE 风险** — 同样的 `uploanFile1` 调用未做 null 检查，本次未改（范围控制），但应该一起修
8. **[2026-06-28] Colima/Lima + FastDFS 的 storage IP 注册问题** — 容器间 NAT 导致 storage 注册的 IP 客户端不可达，文件上传到 storage 那一步会失败。生产环境（直连 IP）不会有此问题；本地 dev 环境考虑在 fastdfs storage 容器配置里**显式指定 TRACKER_SERVER 用容器名**且 storage 用 `STORE_SERVER_IP` 绑固定 IP

### [2026-06-28] MySQL 密码同步问题（环境坑）

**症状**：`docker compose up -d` 后 restapi 容器启动失败，ERROR 日志：
```
java.sql.SQLException: Access denied for user 'root'@'172.18.0.5' (using password: YES)
```

**根因**：MySQL data volume (`codex-ypat-keep-ui-redesign_mysql_data`) 里的 root 密码是**历史值**，但 `docker-compose.yml` 里的 `MYSQL_ROOT_PASSWORD: ${YPAT_LOCAL_MYSQL_ROOT_PASSWORD:-ypat_dev_password_change_me}` 在 `.env` 或 shell env 中被改过 → 新 mysql 容器**用新密码启动**，但 volume 里 init 的还是**旧密码** → 远程连接 access denied。

**检测方法**：
```bash
# 看容器内 root 实际密码能不能进
docker exec <mysql-container> mysql -u root -p"ypat_dev_password_change_me" -e "SELECT 1"
# 如果 Access denied，就是 password mismatch
```

**修复方法（保留数据）**：
```bash
# 1. 停原 mysql
docker stop <mysql-container>
# 2. 起临时容器 --skip-grant-tables 挂同一 volume
docker run -d --name mysql-skip-grant \
  -v <volume-name>:/var/lib/mysql \
  --network <network> \
  --entrypoint "" mysql:8.0 \
  bash -c "mysqld --skip-grant-tables --skip-networking=0"
# 3. 改密码（同时改 root@localhost 和 root@%，mysql 8 是独立账号）
docker exec mysql-skip-grant mysql -e "
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'ypat_dev_password_change_me';
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'ypat_dev_password_change_me';
FLUSH PRIVILEGES;"
# 4. 停临时容器，重启原 mysql
docker rm -f mysql-skip-grant
docker compose -p <project> up -d mysql
# 5. 重启依赖 restapi
docker restart <restapi-container>
```

**教训**：
- ❌ **不要通过 `.env` 临时改 MySQL 密码** — 这会破坏 data volume 里的初始化数据。要么固定密码（写进 `docker-compose.yml`），要么用 docker secret / vault 管理。
- ✅ **修改 mysql 密码必须在容器内 `ALTER USER`**，不能改 env 然后重建（volume 数据是按老密码 init 的）。
- ✅ **MySQL 8 必须同时改 `root@localhost` 和 `root@%`** — 远程连 `172.18.0.5` 实际匹配的是 `root@%`，本地连匹配 `root@localhost`，是两个独立账号。
