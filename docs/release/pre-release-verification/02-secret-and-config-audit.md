# 密钥与配置审计

更新时间：2026-06-28 15:10 +0800

## 结论

状态：`ROTATION_REQUIRED`。

本轮发现后端存在历史硬编码 JWT(JSON Web Token，令牌)、OAuth(OAuth 授权协议) client secret(客户端密钥)、MySQL(MySQL 数据库) 密码、Redis(Redis 缓存) 密码、RabbitMQ(RabbitMQ 消息队列) 密码、FastDFS(FastDFS 文件服务) anti-steal secret(防盗链密钥) 和 SSO(Single Sign-On，单点登录) keystore(密钥库) 文件。已完成源码侧外置和 keystore 删除，但由于这些值曾进入 Git 历史，必须由运维或平台负责人在真实环境完成密钥轮换，不能仅靠代码删除宣称已安全。

## 本轮修改

- WAP 和 Web 管理后台 JWT secret 改为环境变量注入，开发默认值使用明确的 `development-only-*` 占位。
- SSO JWT 签名密钥和 OAuth client secret 改为配置项/环境变量注入，删除 dev/pro keystore 文件。
- 后端 dev/pro application 配置中的 MySQL、Redis、Eureka、RabbitMQ 改为环境变量占位。
- FastDFS 防盗链历史密钥从 WAP/Web 配置中移除，保留空值并要求部署环境显式配置。
- 新增 `backend/.env.staging.example` 和 `frontend/.env.staging.example`，只包含占位符。
- 新增 `SecretExternalizationSourceTest`，阻止历史密钥和 keystore 文件重新进入源码。
- CI(持续集成) security job(安全任务) 增加私钥、keystore 和历史密钥扫描。

## 环境变量映射

| 变量名 | 用途 | 使用模块 | 是否必填 | 开发默认值 | 预发配置方式 | 生产配置方式 | 是否需要轮换 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `YPAT_WAP_JWT_SECRET` | WAP JWT 签名 | `system-wap` | 是 | `development-only-*` | 密钥管理或 `.env` 注入 | 密钥管理系统注入 | 是 |
| `YPAT_WEB_JWT_SECRET` | 管理后台 JWT 签名 | `system-web` | 是 | `development-only-*` | 密钥管理或 `.env` 注入 | 密钥管理系统注入 | 是 |
| `YPAT_SSO_JWT_SIGNING_KEY` | SSO JWT 签名 | `system-sso` | 是 | dev 占位 | 密钥管理注入 | 密钥管理系统注入 | 是 |
| `YPAT_SSO_CLIENT0_SECRET` | SSO client0 密钥 | `system-sso` | 是 | dev 占位 | 密钥管理注入 | 密钥管理系统注入 | 是 |
| `YPAT_SSO_CLIENT1_SECRET` | SSO client1 密钥 | `system-sso` | 是 | dev 占位 | 密钥管理注入 | 密钥管理系统注入 | 是 |
| `YPAT_MYSQL_HOST` | MySQL 主机 | 多个后端模块 | 是 | `localhost` | 预发数据库地址 | 生产数据库地址 | 否 |
| `YPAT_MYSQL_PORT` | MySQL 端口 | 多个后端模块 | 是 | `3306` | 预发数据库端口 | 生产数据库端口 | 否 |
| `YPAT_MYSQL_DATABASE` | MySQL 库名 | 多个后端模块 | 是 | `ypat` | 预发库 | 生产库 | 否 |
| `YPAT_MYSQL_USERNAME` | MySQL 用户 | 多个后端模块 | 是 | `root` | 最小权限账号 | 最小权限账号 | 是 |
| `YPAT_MYSQL_PASSWORD` | MySQL 密码 | 多个后端模块 | 是 | 本地占位 | 密钥管理注入 | 密钥管理系统注入 | 是 |
| `YPAT_REDIS_HOST` | Redis 主机 | WAP/Web | 是 | `localhost` | 预发 Redis | 生产 Redis | 否 |
| `YPAT_REDIS_PORT` | Redis 端口 | WAP/Web | 是 | `6379` | 预发 Redis | 生产 Redis | 否 |
| `YPAT_REDIS_PASSWORD` | Redis 密码 | WAP/Web | 否/按环境 | 空 | 密钥管理注入 | 密钥管理系统注入 | 是 |
| `YPAT_RABBITMQ_URL` | RabbitMQ 地址 | backend-base | 是 | `amqp://localhost` | 预发 MQ | 生产 MQ | 否 |
| `YPAT_RABBITMQ_USERNAME` | RabbitMQ 用户 | backend-base | 是 | 本地占位 | 最小权限账号 | 最小权限账号 | 是 |
| `YPAT_RABBITMQ_PASSWORD` | RabbitMQ 密码 | backend-base | 是 | 本地占位 | 密钥管理注入 | 密钥管理系统注入 | 是 |
| `YPAT_WX_APP_ID` / `YPAT_WX_APP_SECRET` | 微信小程序登录 | WAP | 是 | 空/占位 | 微信测试或预发应用配置 | 正式小程序配置 | 是 |
| `YPAT_WX_MCH_ID` / `YPAT_WX_PAY_KEY` | 微信支付 | WAP | 生产必填 | 空/占位 | 沙箱或预发支付配置 | 正式商户配置 | 是 |
| `YPAT_SMS_ACCESS_KEY_ID` / `YPAT_SMS_ACCESS_KEY_SECRET` | 短信服务 | WAP/Web | 按登录方式 | 空/占位 | 短信测试配置 | 正式短信配置 | 是 |
| `YPAT_FASTDFS_HTTP_SECRET_KEY` | 文件服务防盗链 | WAP/Web | 按文件服务策略 | 空 | 预发文件服务配置 | 正式文件服务配置 | 是 |

## 扫描结果

- 未提交新的真实 `.env` 文件。
- 未保留 JKS(Java KeyStore，Java 密钥库) 文件。
- 未在示例文件中写入真实 AppSecret(AppSecret 应用密钥)、支付密钥、短信密钥或证书私钥。
- 代码、CI 和 preflight(上线前检查) 中保留的历史密钥匹配规则使用字符串拆分，避免重新提交完整密钥。

## 待轮换清单

| 项目 | 状态 | 说明 |
| --- | --- | --- |
| JWT 签名密钥 | `SECURITY_BLOCKED` | 曾在源码中硬编码，生产必须轮换。 |
| SSO client secret | `SECURITY_BLOCKED` | 曾在源码或 keystore 中出现，生产必须轮换。 |
| MySQL/Redis/RabbitMQ 密码 | `SECURITY_BLOCKED` | 曾有硬编码配置，生产必须轮换。 |
| FastDFS 防盗链密钥 | `SECURITY_BLOCKED` | 曾有硬编码配置，生产必须轮换。 |
| 微信/支付/短信密钥 | `MANUAL_REQUIRED` | 需要平台后台确认是否曾泄露；若曾用于真实环境，必须轮换。 |
