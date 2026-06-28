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
