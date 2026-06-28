# YPAT 生产上线待办

更新时间：2026-06-28

## 1. 当前问题

- `VITE_API_BASE_URL` 当前是 HTTP IP。
- `VITE_IMAGE_BASE_URL` 当前是 HTTP IP。
- `production` 模式强制要求 HTTPS。
- 当前配置不能直接用于正式生产发布。
- 本轮不修改 `frontend/.env.production`，只记录正式上线前必须完成的配置。

## 2. 正式配置占位

```env
VITE_APP_ENV=production
VITE_API_BASE_URL=https://api.<正式域名>
VITE_IMAGE_BASE_URL=https://static.<正式域名>
```

## 3. 运维待办

- 正式 API 域名。
- 正式图片和文件域名。
- DNS。
- TLS 证书。
- 证书自动续期。
- Nginx 或网关反向代理。
- HTTPS 健康检查。
- H5 CORS。
- FastDFS 或对象存储外网访问。
- 微信支付回调域名。
- 登录相关回调。
- 日志。
- 监控。
- 告警。
- 限流。
- 备份。
- 回滚配置。

## 4. 微信小程序后台

- 配置 request 合法域名。
- 配置 uploadFile 合法域名。
- 配置 downloadFile 合法域名。
- 配置 socket 合法域名，如实际使用。
- 配置 web-view 业务域名，如实际使用。
- 所有域名必须为 HTTPS。
- 不允许正式环境使用 IP。
- 证书链必须完整。
- 域名备案和平台审核必须完成。

## 5. 上线前构建检查

必须执行：

```bash
cd frontend
pnpm install --frozen-lockfile
pnpm run check
pnpm run build:h5
pnpm run build:mp-weixin
```

检查构建产物：

```bash
grep -R "82.156.14.216" dist || true
grep -R "http://" dist || true
grep -R "localhost" dist || true
```

出现非预期地址时禁止发布。

## 6. 上线前人工联调

- 微信真实登录。
- H5 手机号登录。
- 发布约拍。
- 报名。
- 收藏。
- 消息列表。
- 消息详情。
- 联系方式解锁。
- 拍拍豆不足。
- 实名认证。
- 充值。
- 支付结果确认。
- 账单。
- 收支记录。
- 意见反馈。
- 退出登录。
- Token 失效。
- 接口异常。
- 网络异常。
- 微信审核版本测试。

## 7. 保证金上线前数据检查

本轮不恢复真实 199 元保证金支付，不自动执行生产 UPDATE。

上线前只读检查 SQL 示例：

```sql
SELECT creditflag, status, COUNT(*) AS total
FROM t_ypat_info
WHERE creditflag = 1
GROUP BY creditflag, status
ORDER BY status;

SELECT id, userid, describ, status, creditflag, pubdate
FROM t_ypat_info
WHERE creditflag = 1
ORDER BY pubdate DESC
LIMIT 100;
```

检查目标：

- 是否存在有效 `creditflag=1` 约拍。
- 数量。
- 状态。
- 是否仍允许报名。
- 是否需要业务下线或人工修改。

## 8. 反馈表数据库 migration

项目当前未发现 Flyway(flyway 数据库迁移工具)、Liquibase(liquibase 数据库迁移工具)或 Spring SQL 初始化自动执行链路；`backend/dev/mysql/20260628_create_feedback.sql` 是手工运维 SQL(sql 结构化查询语言)脚本，不会随服务启动自动执行。

执行要求：

- 执行人：生产 DBA 或具备生产数据库变更权限的后端负责人。
- 执行环境：目标生产数据库主库；禁止在本地或预发记录中冒充生产执行。
- 执行时间：正式发布维护窗口内，应用发布前完成。
- 备份要求：执行前完成全库备份，或至少备份即将新增/校验的 `t_feedback` 相关结构和变更记录。
- 执行脚本：`backend/dev/mysql/20260628_create_feedback.sql`。
- 回滚脚本：`backend/dev/mysql/20260628_drop_feedback_rollback.sql`，仅在确认本次上线回滚且已备份 `t_feedback` 数据后执行。

执行前校验：

```sql
SHOW TABLES LIKE 't_feedback';
```

执行后校验：

```sql
SHOW CREATE TABLE `t_feedback`;
SHOW INDEX FROM `t_feedback`;
SELECT COUNT(*) AS total FROM `t_feedback`;
```

回滚前备份示例：

```sql
CREATE TABLE `t_feedback_backup_yyyymmddhhmmss` AS
SELECT * FROM `t_feedback`;
```

## 9. 管理后台微信密钥环境变量

`system-web` 管理后台微信配置、`system-wap` 移动端后端第三方配置不得在源码中硬编码，正式环境需通过安全的部署配置注入：

```env
YPAT_WEB_WX_APP_ID=<微信应用 AppID>
YPAT_WEB_WX_APP_SECRET=<微信应用 AppSecret>
YPAT_WEB_WX_MCH_ID=<微信支付商户号>
YPAT_WEB_WX_PAY_KEY=<微信支付 API 密钥>
YPAT_WEB_BD_OCR_AK=<管理后台百度 OCR AK>
YPAT_WEB_BD_OCR_SK=<管理后台百度 OCR SK>

YPAT_WX_APP_ID=<微信小程序 AppID>
YPAT_WX_APP_SECRET=<微信小程序 AppSecret>
YPAT_WX_MCH_ID=<微信支付商户号>
YPAT_WX_PAY_KEY=<微信支付 API 密钥>
YPAT_WX_PUB_APP_ID=<微信公众号 AppID>
YPAT_WX_PUB_APP_SECRET=<微信公众号 AppSecret>

YPAT_BD_IDCARD_AK=<百度身份证 OCR AK>
YPAT_BD_IDCARD_SK=<百度身份证 OCR SK>
YPAT_BD_IDMATCH_AK=<百度身份核验 AK>
YPAT_BD_IDMATCH_SK=<百度身份核验 SK>
YPAT_BD_APP_KEY=<百度登录 App Key>
YPAT_BD_APP_SECRET=<百度登录 App Secret>

YPAT_SMS_MOCK_ENABLED=false
```

要求：

- 仅在部署平台、密钥管理系统或服务器环境变量中配置。
- 不写入 Git、镜像明文层、日志、文档示例真实值或前端产物。
- 变更后验证管理后台订阅消息和支付相关后台能力。
- 如旧密钥曾提交到仓库，必须按微信和支付平台流程轮换密钥。
- 正式环境 `YPAT_SMS_MOCK_ENABLED` 必须为 `false`，不得配置测试手机号和验证码。
