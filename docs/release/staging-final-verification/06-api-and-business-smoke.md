# API 与业务冒烟 — 待部署后填入

## 状态

**未执行 — STATEFUL_MIGRATION_REQUIRED**

## 计划项

使用 staging 专用测试账号执行。**禁止**真实支付、真实身份证、真实微信密钥、真实用户隐私数据。

| # | 用例 | 期望 | 结果 |
| --- | --- | --- | --- |
| 1 | 首页加载 | 200 | MANUAL_REQUIRED |
| 2 | Banner 列表 API | 200 + 非空 JSON | MANUAL_REQUIRED |
| 3 | 约拍列表 | 分页/筛选可用 | MANUAL_REQUIRED |
| 4 | 约拍详情 | 详情字段完整 | MANUAL_REQUIRED |
| 5 | 图片加载 | FastDFS URL 200 | MANUAL_REQUIRED |
| 6 | 登录页 | 渲染正常 | MANUAL_REQUIRED |
| 7 | 登录获取 Token | JWT 返回 | MANUAL_REQUIRED |
| 8 | 消息接口鉴权 | 401 / 200 分支正确 | MANUAL_REQUIRED |
| 9 | 意见反馈鉴权 | 401 / 200 分支正确 | MANUAL_REQUIRED |
| 10 | 管理后台登录页 | 渲染正常 | MANUAL_REQUIRED |
| 11 | 发布测试约拍 | 创建 + DB 落库 | MANUAL_REQUIRED |
| 12 | 报名 | 报名状态变化 | MANUAL_REQUIRED |
| 13 | 消息跳转 | 跳转目标正确 | MANUAL_REQUIRED |
| 14 | 联系方式 | 数据加密/脱敏 | MANUAL_REQUIRED |
| 15 | 两照实名认证测试 | 测试用证件可走通 | MANUAL_REQUIRED |

完整执行后填入实际结果。
