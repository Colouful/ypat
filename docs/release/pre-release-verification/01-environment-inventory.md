# 环境盘点

更新时间：2026-06-28 15:10 +0800

## Git 基线

| 项目 | 值 |
| --- | --- |
| worktree(工作树) | `/Users/lizhenwei/workspace/vueworkspace/ypat-workspace-worktrees/pre-release-verify` |
| 分支 | `codex/pre-release-verification` |
| 基线 | `b3a5e433921d3f4d766caf398f1c4189506bd473` |
| 目标分支 | `main` |

## 代码结构

| 模块 | 路径 | 说明 |
| --- | --- | --- |
| 新版前端 | `frontend/` | UniApp(Vue 跨端应用框架) 前端。 |
| 后端业务 | `backend/` | Maven 多模块 Spring Boot(Spring 后端框架) 项目。 |
| 后端基础组件 | `backend-base/` | Config、Eureka、Gateway、Zipkin、Turbine 等基础服务。 |
| 旧版参考 | `91pai-master/` | 旧版小程序参考，非本轮修改目标。 |
| 发布文档 | `docs/release/` | 上线待办、收口证据和本轮预发验证证据。 |

## 工具版本

| 工具 | 版本 | 用途 |
| --- | --- | --- |
| Node.js(Node 运行时) | `v22.18.0` | 前端安装、测试和构建。 |
| pnpm(pnpm 包管理器) | `11.7.0` | 前端依赖安装。 |
| Maven(Maven 构建工具) | `3.6.3` | 后端测试和打包。 |
| Java(Java 运行时) | 测试使用 17；WAP 本地运行使用 Zulu 8 | 后端测试可在 Java 17 通过；Spring Boot 1.5 运行时在 Java 17 存在模块兼容问题，WAP 本地启动使用 Java 8。 |
| Docker(Docker 容器) | `29.4.0` | 本地 MySQL、Redis 和 migration 验证。 |

## 本地依赖验证

| 服务 | 端口 | 启动命令 | 结果 |
| --- | --- | --- | --- |
| MySQL(MySQL 数据库) | `3308 -> 3306` | `YPAT_LOCAL_MYSQL_PORT=3308 YPAT_LOCAL_REDIS_PORT=6380 docker compose up -d mysql redis` | `healthy`。 |
| Redis(Redis 缓存) | `6380 -> 6379` | 同上 | `healthy`，`PING` 返回 `PONG`。 |

证据：`docs/release/pre-release-verification/artifacts/service-health-checks.txt`。

## 后端运行验证

| 服务 | 端口 | 启动方式 | 结果 |
| --- | --- | --- | --- |
| `system-wap` | `18081` | `mvn -pl system-wap -am package -DskipTests` 后使用 Java 8 运行 jar | 启动成功，11 秒 ready，匿名私有接口返回业务码 `401`。 |

说明：直接在 Java 17 上启动 Spring Boot 1.5 WAP 服务会触发 CGLIB(Java 字节码生成库) 模块访问兼容问题，因此本轮使用 Java 8 验证运行时行为。CI(持续集成) 仍使用 Java 17 执行 Maven 测试。

## 当前环境限制

- 未连接生产数据库。
- 未启动完整预发注册中心、`system-restapi`、文件服务和微信平台依赖。
- 未执行真实微信登录、真实支付和实名审核后台人工联调。
- 本轮只能证明当前代码的构建、测试、migration 脚本和 WAP 鉴权层行为，不能替代完整预发环境验收。
