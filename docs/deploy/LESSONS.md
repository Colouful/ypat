# YPAT 部署 Lessons Learned

> 真实事故复盘 → 防止再犯。每条都写"事件 + 根因 + 长期防御"。
> 新事故追加在尾部，旧条目按需要修订。

---

## 1. 镜像里跑的是旧 jar：本地能 build 成功但部署"没生效" (2026-07-03)

| 项 | 值 |
| --- | --- |
| 事故 ID | LESSONS-2026-07-03-01 |
| 触发操作 | 服务器 `docker compose build wap` 重建 ypat-wap 镜像 |
| 现象 | 前端发版调试时一切正常，服务器返回 401 / "Could not write JSON: JsonObject" |
| 业务影响 | admin 后台与预发小程序作品列表约 4 小时无法正常工作 |
| 已恢复 | 是 (rsync 本地 jar → 服务器 rebuild → recreate container) |

### 事件

给 `system-wap` 增加了 `GsonJacksonBridgeConfig` 让 admin 接口不再 500，本地 `mvn package` 通过。服务器上 `docker compose build wap` 也"成功"完成，但容器跑的还是 6 月底的旧 jar (17:44 时间戳)，新加的 class 没出现在 BOOT-INF 里。

### 根因

旧 Dockerfile 假设构建上下文里 `target/*.jar` 已经存在：

```dockerfile
FROM eclipse-temurin:8-jre
COPY target/system-wap-1.0-SNAPSHOT.jar app.jar
```

部署链路是：
1. 改 java 代码 → mvn package → 生成 jar
2. 提交代码到 git
3. 服务器 git pull → docker compose build → 用 target/*.jar 起镜像

第 1 步的 jar 留在开发者本地，**步骤 1 和步骤 3 之间靠开发者"记得"rsync**。这次漏了，服务器上的 target/ 还是上次成功的 jar。`docker compose build` 没有任何校验会发现这件事——它只检查 Dockerfile 文件是否变化，上层 jar 没动就缓存命中。

### 长期防御 (2026-07-03 修复)

**改 Dockerfile 为 multi-stage**：build 阶段在镜像内跑 `mvn package`，运行时阶段只 COPY 出来的 jar。这样无论部署方传什么 target/，build 出来都是当前源码。

```dockerfile
FROM maven:3.8-eclipse-temurin-8 AS build
WORKDIR /src
COPY pom.xml ./
COPY <module>/pom.xml <module>/
RUN mvn -pl <module> -am dependency:go-offline || true
COPY . .
RUN mvn -pl <module> -am package -DskipTests
FROM eclipse-temurin:8-jre
COPY --from=build /src/<module>/target/<module>-1.0-SNAPSHOT.jar app.jar
```

**配合**：
- docker-compose `build.context` 必须指向**包含父 pom 的目录**（`./backend` 不是 `./backend/system-wap`），否则 Maven 反应堆解析失败。
- `backend/.dockerignore` 加 `**/target` 避免 build 上下文意外包含老 jar。
- 缓存层策略：先只 COPY 所有 `pom.xml`，跑 `dependency:go-offline` 让依赖下载被 Docker 层缓存；再 COPY 源码并 mvn package。这样只有改代码才会失效最后一层。

### 部署手册更新

- `docs/deploy/STAGING_DEPLOYMENT.md` 也要把"先 mvn package"那步删掉，说明**现在直接 `docker compose build wap` 即可**。
- CI 如果未来引入，应该建一个独立 registry 镜像 build job，而不是指望开发者手工 mvn。

---

## 2. docker compose `--force-recreate` 把依赖一起 recreate + Redis 数据卷格式不兼容 (2026-07-03)

| 项 | 值 |
| --- | --- |
| 事故 ID | LESSONS-2026-07-03-02 |
| 触发操作 | `docker compose up -d --force-recreate wap`（带 mysql/redis 重启） |
| 现象 | Redis crashloop：`Can't handle RDB format version 12` |
| 业务影响 | redis 状态清空（生产 staging 实际空白，影响小；本地开发数据有损） |
| 已恢复 | 是 (`docker compose stop mysql restapi wap system-web eureka` → up mysql → 其他；手动 redis 删掉用 compose 拉起) |

### 事件

修完 401 后想强制重建 wap 容器，命令里带了 `--force-recreate`，结果 compose 把整个 stack (mysql / redis / eureka / restapi / wap / system-web) 都 recreate 了。其中 redis 容器换了镜像版本，从 `redis:7-alpine` (RDB format v12) 切到 `redis:7.2-alpine`，老 AOF 文件读不了。

### 根因

- `docker compose up -d --force-recreate <service>` 的语义是 **整个 stack 都 recreate**，不是只有那个 service。这跟直觉相反。官方文档里有说明但容易被忽略。
- 数据库/缓存镜像版本没钉死也加剧了：compose 里写 `redis:7.2-alpine`，部署主机可能在不同时间点拉取不同的 patch 版本。

### 长期防御 (2026-07-03 修复)

1. **永远用 `--no-deps`** 隔离单服务重建：
   ```bash
   docker compose up -d --no-deps --force-recreate wap
   ```
2. **`docker-compose.yml` 里把 redis 镜像钉成 `redis:7-alpine`**，匹配数据卷里实际写入的 RDB 版本（注释里写明原因）。
3. **生产/staging 改完后用 `docker compose up -d --no-deps`** 重建默认行为，不再用 `--force-recreate` 全栈。

### 部署手册更新

`STAGING_DEPLOYMENT.md` 与 onboarding 文档必须把"单服务重建必须用 `--no-deps`"写成强提醒，否则下次还会有人踩。

---

## 3. 小程序 staging 上 401：容器里跑的是 6 月底旧镜像，没作品模块代码 (2026-07-03)

| 项 | 值 |
| --- | --- |
| 事故 ID | LESSONS-2026-07-03-03 |
| 触发操作 | 用户发布小程序 staging 包，前端请求 `/api/work/list` 返回 401 |
| 现象 | 容器内 jar BOOT-INF 字符串里没有 `/work/list` 白名单 |
| 业务影响 | staging 作品流列表 ~30 分钟不可用 |
| 已恢复 | 是 (rebuild wap 镜像 → up -d --no-deps wap) |

### 事件

用户报告微信开发者工具访问 panghu.work/api/work/list 返回 `{"code":"401","msg":"unauthorized"}`。检查服务器 `ypat-wap:stable` 镜像 ID：`4151cb15b879`，创建日期 2026-06-28，比作品模块提交 `ca2a4653` (feat work: 完整作品模块) 早。

### 根因

`docker image tag ypat-wap:latest ypat-wap:stable` 的稳定标签没有自动同步机制——只在新代码 deploy 时手动维护。这次没部署新 wap，stable 一直指着 6 月底的镜像。

### 长期防御

- **生产部署脚本里"重新打 stable 标签"必须 atomic**：build + tag + up 在一个 shell 里执行，避免漏改。
- **考虑把 `:stable` 删掉**：只保留 `:latest` 和带 commit-sha 的 immutable tag（如 `ypat-wap:abc1234`）。`rollback` 改成指向历史 commit-sha，更可靠。
- **加一个 weekly 检查脚本**：自动扫描各服务 `:stable` 与 `:latest` 的差距，超过 N 天报警，提醒人工确认是否过时。

> 真正根治靠 multi-stage + immutable tags，这条防御属于"过渡期缓解"。

---

## 4. utf8mb4 双编码：seed 数据从一开始就写乱了，Jackson 序列化层假装成元凶 (2026-07-03)

| 项 | 值 |
| --- | --- |
| 事故 ID | LESSONS-2026-07-03-04 |
| 触发操作 | 后台 `/admin/product/list` 显示中文乱码 (`å©šçº±...`) |
| 现象 | 数据库里 `婚纱摄影套餐` 被存成 `C3A5C2A9C5A1C3A7C2BAC2B1C3A6...` |
| 业务影响 | 后台所有列表/详情/搜索中文列不可读 |
| 已恢复 | 是 (mysql 服务端 + 客户端统一 utf8mb4 + 修复存量数据) |

### 事件

产品名、用户昵称、文章标题都呈"半乱码"——UTF-8 字节被当 latin1 解读再 UTF-8 写入。修 Jackson 时以为是序列化问题，结果发现是数据从种子阶段就坏了，只是当时没界面让人看出来。

### 根因

- MySQL 客户端默认 `character_set_client=latin1`，dev seed 文件 (UTF-8) 跑进来时把多字节 UTF-8 当单字节 latin1 解读，再用 utf8mb4 列存进去——形成一次错误编码（`E5A99A` → `C3A5C2A9`）。
- Hibernate / 项目代码都没显式 `SET NAMES`，依赖 MySQL 客户端默认行为。

### 长期防御 (2026-07-03 修复)

1. **MySQL 服务端强制 utf8mb4**：
   ```bash
   --character-set-server=utf8mb4
   --collation-server=utf8mb4_unicode_ci
   --skip-character-set-client-handshake   # 关键：禁掉客户端降级
   --init-connect='SET NAMES utf8mb4'      # 关键：每个连接上来都强制
   ```
2. **dev-init.sql / dev-seed.sql 顶部加 `SET NAMES utf8mb4;`**，双保险（即使有人绕开 docker compose 手动跑也安全）。
3. **存量数据在线修复**（一次性脚本，谨慎使用）：
   ```sql
   UPDATE t_product SET name =
     CONVERT(BINARY CONVERT(name USING latin1) USING utf8mb4)
     WHERE name REGEXP '[ãåç]';   -- 找典型 double-encoded 字节的前兆字符
   ```
   修复后检查结果，必要时再分别给 t_user / t_banner / t_article 跑。

### 部署手册更新

`LOCAL_DEV_GUIDE.md` 要说明 seed 跑完后，立刻 `SELECT id, name, HEX(name)` 抽样验证中文不是双编码，作为开发环境 sanity check。

---

## 5. Spring Security + Jackson + Gson 三种序列化机制混用 (2026-07-03)

| 项 | 值 |
| --- | --- |
| 事故 ID | LESSONS-2026-07-03-05 |
| 触发操作 | Admin* Controller 把 Feign 返回的 JSON parse 成 Gson `JsonElement` 再塞进 `ResponseApiBody.res` |
| 现象 | Spring 用 Jackson 序列化时把 Gson 对象当 POJO 反射 getter，命中 `asDouble()`，全部 admin 列表接口 500 |
| 业务影响 | 后台 `/admin/ypat/list` 等 8 个接口全部不可用 |
| 已恢复 | 是 (`GsonJacksonBridgeConfig` 给 Jackson 注册 `JsonElement` serializer) |

### 事件

业务代码模式：
```java
String json = feignClient.findPage(qo);
JsonElement pageData = JsonParser.parseString(json);
return ResponseApiBody.success(pageData);  // res 字段是 Gson JsonElement
```
响应序列化器是 Spring MVC 默认的 Jackson，根本不知道 Gson 的 `JsonElement`。`JsonElement` 类确实有 `asDouble()`/`asString()`/`getAsJsonObject()` 等 getter，Jackson 直接拿到 `asDouble()` 调出来抛 "Could not write JSON"。

### 根因 / 设计债务

旧后台 `system-web` 是直接 Spring MVC + Jackson，前后端 json 字符串全走 Jackson；新 `system-wap` 把旧 `system-web` 的 controller 迁过来后，本来是返回字符串给前端 (`return GsonUtils.toJson(...)`)，但 admin 模块为了少写一层 map，要求返回 `ResponseApiBody` 对象图，于是出现 **Controller 内部用 Gson，对外用 Jackson** 的混用，Jackson 不能"内省"Gson。

### 长期防御 (2026-07-03 修复)

1. **新增 `GsonJacksonBridgeConfig`**：
   ```java
   SimpleModule module = new SimpleModule();
   module.addSerializer(JsonElement.class, (value, gen, _) -> {
     gen.writeRawValue(value.toString());  // JsonElement.toString() 本来就是合法 JSON
   });
   ```
2. **业务层继续用 Gson** 不影响——admin controller 多了 8 处 `JsonParser.parseString(...)` 直接受益，零代码改动。

### 真正治本（未来工作）

admin controller 这种模式是迁过来的临时形态。最终应该：
- Feign 客户端接口返回类型改成强类型 Java 对象（不是 String），业务层不再需要 Gson.parseString；
- 这样 Jackson 就能正常序列化，移除 bridge 配置。

短期 bridge 是 OK 的，但要写在 issue 里——迁移成本大概 1 周。

---

## 6. 小程序 dev 调试页"无故"被 nginx /api/ 前缀反代截走 (2026-07-03)

| 项 | 值 |
| --- | --- |
| 事故 ID | LESSONS-2026-07-03-06 |
| 触发操作 | 用户跑小程序 dev 调试，IDE 上看不到接口流量 |
| 现象 | 前端 `print envConfig` 打出 `https://panghu.work/api` 不是 `http://127.0.0.1:8080/api` |
| 业务影响 | 调试命中 staging 而非本地后端，但功能能用，没业务阻断 |
| 经验价值 | 日后接入测试同学会被这个坑 |

### 事件

`.env.development` 一直用 `localhost`，但小程序 IDE 在某些场景下会按当前登录宿主机的代理走（特别在企业网内），导致实际请求被 ng 路由到 staging。

### 长期防御

- `.env.development` 改用 `127.0.0.1`：`VITE_API_BASE_URL=http://127.0.0.1:8080/api` — 通过 IP 直连，避开 IDE 的 host 代理规则。
- 启动时打印 `[YPAT] envConfig loaded: { apiBaseUrl, env }`，调试时一眼能确认命中的是哪个环境。
- 小程序 IDE 内"不校验合法域名"必须关，避免本地调试被 HTTPS-only 拦。

---

## 7. 微信开发者工具"主包未使用的 JS 文件" warning (2026-07-03)

| 项 | 值 |
| --- | --- |
| 事故 ID | LESSONS-2026-07-03-07 |
| 触发操作 | scan 后列出 `api/modules/feedback.js` 等主包里有但仅被分包页面使用的模块 |
| 现象 | 只是 warning，不是错误 |
| 业务影响 | 无；不影响上包、不影响审核，不影响性能（编译器正确生成 chunk，主包体积不变） |
| 体验影响 | 强迫症不太舒服 |

### 根因

uni-app 3.0 (vite) 对 `api/modules/`、`stores/`、`constants/` 这种跨页面模块不分包下沉——只要 `pages-sub/foo/Bar.vue` 里 import 它，编译器就把模块打进 `common/` 主包 chunk，对应 warning。

### 长期防御（待办）

压缩主包到 2MB 以下或评分高时再处理：
- 物理迁移：把这些模块移到 `pages-sub/<domain>/xxx.ts` 同目录，编译器自然按分包打。
- 升级到 uni-app 最新版，看新版是否改进分包策略。
- Vite 的 `manualChunks` 对 mp-weixin 不起作用（mp-weixin 走 webpack-style 切片），运行时换框架才能彻底解决。

短期忽略。

---

## 工程规范沉淀 (2026-07-03 后)

把以下规范写进 onboarding 文档，避免踩老坑：

1. **单服务重建**：`docker compose up -d --no-deps --force-recreate <service>`，永远不要用 `--force-recreate` 在没加 `--no-deps` 的时候。
2. **镜像 tag**：每次 `docker compose build` 后立即 `docker tag ypat-<svc>:latest ypat-<svc>:stable`，atomic 写在一行。
3. **mysql 字符集**：服务端加 `--skip-character-set-client-handshake --init-connect='SET NAMES utf8mb4'`；客户端 SQL 顶部必须 `SET NAMES utf8mb4`。
4. **Jackson + Gson 混用**：admin 列表接口不要走 ResponseApiBody 强类型对象图，直接用字符串规避；或者迁移到强类型 Feign 客户端（见 LESSONS 5）。
5. **Dokcerfile multi-stage**：业务项目都必须 multi-stage，不再依赖外部预先 mvn。`build.context` 必须指向父 pom 所在目录。

---

## 8. 首次 multi-stage build 拉 maven 镜像很慢 (2026-07-03 follow-up)

| 项 | 值 |
| --- | --- |
| 状态 | **遗留观察，待优化** |
| 观察 | 第一次 `docker compose build wap` 从 docker.io 拉 maven:3.8-eclipse-temurin-8（约 1.2 GB），本机外网慢时 10 分钟起步；多数团队靠 GFW 内网，走其它 registry |
| 业务影响 | 首次部署到新环境阻塞；CI 上没暖缓存第一次 build 慢 |

### 建议（评估中）

1. 把 maven 镜像 base 锁成项目内网 registry 镜像，例如 `registry.cn-hangzhou.aliyuncs.com/ypat/maven:3.8-jdk8`，先推到内网再用。
2. 写一个暖缓存 CI job，每天构建一次并把镜像保留 N 天。
3. 极端情况：换用 `gcr.io/distroless/java:8` 之类的更小 base，压缩拉取量。

短期不解决也可以——团队成员本地 build 过一次后，Docker 层缓存就能复用，下次只下增量。

---

## 9. fastdfs server v6 + java client v5 协议不兼容：admin 上传 1015 (2026-07-04)

| 项 | 值 |
| --- | --- |
| 事故 ID | LESSONS-2026-07-04-01 |
| 触发操作 | 前端 admin `/admin/ypat/upload` 与 `/admin/upload` 上传图片 |
| 现象 | HTTP 200，业务 `{"code":1015, "msg":"作品图片上传失败"}`；本地 wap 日志 `recv package size -1 != 10`；服务器 wap 日志 `MyException: getStoreStorage fail, errno code: 28` |
| 业务影响 | 后台约拍作品编辑页、banner 文章图片上传全部失败，本地与服务器同症状 |
| 已恢复 | 是 (回退 fastdfs server image 到 v5) |

### 事件

排查路径：

1. 错误码 1015 = FAIL_MARK = "水印失败"。但 controller 抛的 message 是 "作品图片上传失败" — 实际不是水印，是 controller 自己 throw（`fileId == null` 的分支）。
2. `FastDFSClient` 把 uploadFile 内部异常吞了变 null。要拿到原始错误必须看 wap 容器日志。
3. wap 日志分两条：本地 `recv package size -1 != 10`，服务器 `MyException: errno code: 28`（不同 stack trace 来自 Java client 异常封装差异，但同根因）。
4. fastdfs storage 日志：`WARNING - sf_nio.c:951, expect pkg length: 262144, recv pkg length: 12` — storage 期望 256 KB 协议缓冲区，client 只发 12 字节，storage 主动断开。
5. **ygqygq2/fastdfs-nginx 镜像 OCI 标签里写 `V6.15.3`，fastdfs-client-java 1.27 (cn.bestwu) 是 v5 协议 client。** v6 server 在 active_test 阶段用 256 KB buffer，v5 client 不识别 → 上传失败。

### 根因

两个 FastDFS 组件的"协议主版本"不匹配：

| 组件 | 当前版本 | 协议主版本 |
| --- | --- | --- |
| `ygqygq2/fastdfs-nginx@sha256:076ee9c1...` image | V6.15.3 | 6.x |
| `cn.bestwu:fastdfs-client-java:1.27` | 1.27 (2018 release) | 5.x |

这两个组件应在 2026-01 image 升级时一起升级 client，但代码仓只锁 image digest，没动 pom 版本，所以从那天起所有上传都是隐性失败的。用户报告这天才暴露是因为 admin 上传功能此前没人调试过。

### 长期防御

1. **临时 fix（2026-07-04 applied）**：dev/staging fastdfs image 钉到 `delron/fastdfs:latest` (FastDFS V5.11 + nginx)，与现有 client 1.27 协议匹配。验证：本地 /admin/ypat/upload 返回 url。
2. **彻底 fix（待办 TODO）**：升级 `cn.bestwu:fastdfs-client-java` 到 1.29+ 系列（v6 兼容），同时把 image 留回 ygqygq2 v6。**风险评估**：升级需要 mvn 拉到新 client jar（项目内网不一定通）；升级后客户端有破坏性改动（StorageClient1 API 调整）需要联调测试。综合估算 1-2 天工作量。
3. **CI 检查**：dev/staging 启动后跑一条"上传测试图 → 期待 200" 的冒烟，把 fail 暴露在部署环节而不是用户上传图片时才暴露。
4. **doc 同步**：`docs/release/STAGING_DEPLOYMENT.md` 的 `install-nginx-config.sh` / `db-migrate-staging.sh` 描述里写明 fastdfs 协议版本兼容性是硬约束。

### 修复 checklist

- [ ] dev docker-compose already `delron/fastdfs:latest` (2026-07-04)
- [ ] 服务器 fastdfs image 替换（待进行）— 通过 `docker compose ... build` 重新拉新 image / 改环境变量 `YPAT_FASTDFS_IMAGE=delron/fastdfs:latest`
- [ ] 服务器 fastdfs tracker+storage 数据卷保留，data 目录预初始化 mark 用新 IP — 重启即可
- [ ] 长期 fix：升级 java client 到 v6 兼容版本

