# 微信支付 APIv3 保证金与会员支付设计

日期：2026-07-08
工作树：`/Users/lizhenwei/workspace/vueworkspace/ypat-workspace`
分支：`dev6`

## 背景

新版 `frontend(新版小程序前端)` 的 `pages-sub/user/credit.vue(信用担保页)` 目前在缴纳保证金时调用 `POST /order/create`，请求参数为 `type=2&productid=0&total_fee=19900`。线上/本地调试返回 `{"code":1002,"msg":null,"res":null}`，前端无法知道真实失败原因。

现有支付链路有几个明显问题：

- 保证金金额在前端写死为 `DEPOSIT_FEE_FEN = 19900`，后台无法配置，也无法临时切换为测试金额 `0.01` 元。
- `/order/create` 使用旧 `APIv2(第二版接口)` XML(可扩展标记语言)统一下单，和当前准备接入的 `APIv3(第三版接口)`、微信支付公钥模式不匹配。
- `/order/create` 和 `/member/order/create` 各自拼接微信支付参数，缺少统一支付门面，后续接入 `H5(手机网页支付)`、`APP(安卓/苹果原生支付)` 会继续分散。
- 后端捕获和透出微信下单错误不足，导致 `1002/msg=null` 这类问题难以定位。
- 微信支付回调、业务订单状态、用户保证金/会员权益发放之间必须强化事务一致性、幂等和并发锁。

本设计选择 `APIv3(第三版接口) + 微信支付公钥模式`，使用官方 Java SDK(软件开发工具包)接入。首期实现 `MINIAPP(微信小程序支付)` 和 `H5(手机网页支付)`，同时保留 `APP(安卓/苹果原生支付)` 的服务端扩展位。

## 目标

1. 保证金支付改为可配置：后台可配置保证金金额、测试金额、启停、退款规则文案；默认测试金额为 `1` 分，也就是 `0.01` 元。
2. 新保证金支付和会员支付统一走微信支付 `APIv3(第三版接口)` 与微信支付公钥模式。
3. 支持小程序支付和 H5 支付：小程序返回调起支付参数，H5 返回 `h5_url(支付跳转链接)`。
4. 建立统一 `PaymentService(支付服务门面)`，封装下单、回调验签、支付流水、状态查询和业务分发。
5. 支付成功后可靠发放权益：保证金设置用户 `creditflag(信用担保标记)`，会员发放有效期和赠送拍拍豆。
6. 后台新增保证金配置页面，并增强订单/支付流水排障能力。
7. 关键路径具备事务一致性、幂等、乐观锁或条件更新，重复回调不会重复发放权益。
8. 不把商户私钥、APIv3密钥、APIv2密钥、Token(登录令牌)写入代码、SQL(结构化查询语言)脚本、日志或文档。

## 不做内容

- 不在本期实现 `APP(安卓/苹果原生支付)` 客户端调起，只保留后端 `channel=APP` 的抽象空间。
- 不实现自动退款、对账下载、资金账务清算和分账。
- 不删除旧 `/wxpay/notify` 和旧 `APIv2(第二版接口)` 代码，避免影响历史订单；新订单不再走旧链路。
- 不在后台保存或展示微信支付密钥、商户私钥、微信支付公钥文件内容。
- 不将用户手动输入金额作为支付金额事实源；金额必须由后端配置或套餐快照决定。

## 参考资料

- 微信支付商户文档：`https://pay.weixin.qq.com/doc/v3/partner/4012925323`
- 微信支付官方 Java SDK(软件开发工具包)：`https://github.com/wechatpay-apiv3/wechatpay-java`
- 本地技能参考：`wechat-miniprogram-toolkit(微信小程序工具包)` 的 `references/payment.md(支付参考)`
- 本地技能参考：`wechat-pay-integration(微信支付集成)`

## 总体架构

新增统一支付门面，业务订单和支付流水分离：

```text
frontend(新版小程序/H5)
  -> /deposit/config
  -> /deposit/order/create(channel=MINIAPP/H5)
  -> /member/order/create(channel=MINIAPP/H5)

system-wap(用户端接口层)
  -> DepositController(保证金控制器)
  -> MemberController(会员控制器)
  -> PaymentNotifyController(支付回调控制器)
  -> PaymentService(支付服务门面)

system-domain(领域层)
  -> DepositService(保证金服务)
  -> MemberService(会员服务)
  -> PaymentOrderService(支付流水服务)

微信支付 APIv3
  -> JSAPI/小程序下单
  -> H5 下单
  -> 支付结果通知
```

职责边界：

- `DepositService(保证金服务)`：读取保证金配置，创建保证金业务订单，支付成功后开通信用担保。
- `MemberService(会员服务)`：管理套餐、会员订单和会员权益，支付成功后开通/续费会员。
- `PaymentService(支付服务门面)`：选择支付渠道，调用微信支付 `APIv3(第三版接口)`，创建统一支付流水，返回前端支付参数。
- `PaymentNotifyController(支付回调控制器)`：接收微信通知，用微信支付公钥验签，提取支付结果，交给领域服务做幂等处理。
- `PaymentConfig(支付配置)`：从环境变量读取商户号、应用编号、商户私钥路径、APIv3密钥、微信支付公钥 ID、公钥文件路径、回调地址等。

## 微信支付 APIv3 公钥模式

后端使用微信支付官方 Java SDK(软件开发工具包)。

配置项从环境变量读取：

| 配置项 | 说明 |
| --- | --- |
| `YPAT_WX_PAY_MODE` | 支付模式，首期固定 `PUBLIC_KEY` |
| `YPAT_WX_APP_ID` | 小程序 `appid(应用编号)` |
| `YPAT_WX_H5_APP_ID` | H5 场景使用的公众号或应用 `appid(应用编号)`，如与小程序一致可复用 |
| `YPAT_WX_MCH_ID` | 商户号 |
| `YPAT_WX_MCH_SERIAL_NO` | 商户 API 证书序列号 |
| `YPAT_WX_MCH_PRIVATE_KEY_PATH` | 商户私钥文件路径 |
| `YPAT_WX_API_V3_KEY` | APIv3 密钥 |
| `YPAT_WX_PAY_PUBLIC_KEY_ID` | 微信支付公钥 ID |
| `YPAT_WX_PAY_PUBLIC_KEY_PATH` | 微信支付公钥文件路径，例如 `/Users/lizhenwei/Downloads/001yp/pub_key.pem` 在本机开发可用 |
| `YPAT_WX_NOTIFY_URL` | 新版支付通知地址，例如 `https://域名/payment/wechat/notify` |
| `YPAT_WX_H5_SCENE_INFO` | H5 场景信息 JSON(数据交换格式)，生产环境必须按微信支付要求填写 |

安全约束：

- APIv2(第二版接口)密钥已在对话中出现，必须视为已暴露，建议立即到商户平台轮换。
- 用户 Token(登录令牌)已在对话中出现，必须视为已暴露，建议让该用户重新登录或轮换 JWT(令牌)签发密钥。
- 日志只允许打印 `out_trade_no(商户订单号)`、状态码、微信错误码和脱敏 appid/mchid，不打印密钥、公钥内容、私钥内容、Token(登录令牌)。

## 数据模型

### 保证金配置

新增 `t_deposit_config(保证金配置表)`：

| 字段 | 说明 |
| --- | --- |
| `id` | 主键，一期只使用一条全局配置 |
| `enabled` | 是否启用 |
| `amount_fen` | 正式保证金金额，单位分，默认 `19900` |
| `test_enabled` | 是否启用测试金额 |
| `test_amount_fen` | 测试金额，单位分，默认 `1` |
| `refund_wait_days` | 可申请退款等待天数，默认 `90` |
| `early_refund_fee_rate` | 提前退款管理费率，默认 `15` |
| `agreement_summary` | 保证金协议摘要 |
| `updated_at` | 更新时间 |

真实下单金额计算：

```text
payAmountFen = testEnabled ? testAmountFen : amountFen
```

### 保证金业务订单

新增 `t_deposit_order(保证金订单表)`：

| 字段 | 说明 |
| --- | --- |
| `id` | 主键 |
| `out_trade_no` | 商户订单号，唯一索引 |
| `user_id` | 用户 ID |
| `amount_fen` | 下单锁定金额，单位分 |
| `channel` | `MINIAPP` 或 `H5` |
| `status` | `PENDING`、`PAID`、`CANCELLED`、`CLOSED`、`REFUNDED` |
| `prepay_id` | 微信预支付 ID，可空 |
| `transaction_id` | 微信支付订单号 |
| `paid_at` | 支付成功时间 |
| `version` | 乐观锁版本 |
| `created_at` | 创建时间 |
| `updated_at` | 更新时间 |

索引：

- `uk_deposit_order_out_trade_no(out_trade_no)` 唯一。
- `idx_deposit_order_user_status(user_id, status)` 用于复用待支付订单。

### 统一支付流水

新增 `t_payment_order(统一支付流水表)`：

| 字段 | 说明 |
| --- | --- |
| `id` | 主键 |
| `payment_no` | 支付流水号，唯一 |
| `business_type` | `DEPOSIT` 或 `MEMBER` |
| `business_order_no` | 业务订单号 |
| `out_trade_no` | 商户订单号，唯一 |
| `user_id` | 用户 ID |
| `channel` | `MINIAPP`、`H5`，预留 `APP` |
| `amount_fen` | 支付金额，单位分 |
| `status` | `PENDING`、`PAID`、`FAILED`、`CLOSED`、`REFUNDED` |
| `prepay_id` | 微信预支付 ID |
| `h5_url` | H5 支付跳转链接 |
| `transaction_id` | 微信支付订单号 |
| `wechat_trade_state` | 微信交易状态 |
| `notify_event_id` | 微信通知 ID，用于排重 |
| `notify_digest` | 回调原文摘要，不保存完整敏感载荷 |
| `paid_at` | 支付成功时间 |
| `version` | 乐观锁版本 |
| `created_at` | 创建时间 |
| `updated_at` | 更新时间 |

索引：

- `uk_payment_order_out_trade_no(out_trade_no)` 唯一。
- `uk_payment_order_payment_no(payment_no)` 唯一。
- `idx_payment_order_business(business_type, business_order_no)`。
- `idx_payment_order_user_created(user_id, created_at)`。

### 会员订单调整

保留 `t_member_order(会员订单表)`。新增或确认字段：

- `channel(支付渠道)`。
- `prepay_id(预支付 ID)`。
- `version(乐观锁版本)`，如果表中没有则补充。

会员订单仍保存套餐快照，支付金额以 `price_fen(售价分)` 为准。

## 接口设计

### 用户端保证金接口

`GET /deposit/config`

返回：

```json
{
  "enabled": true,
  "amountFen": 1,
  "displayAmountFen": 1,
  "refundWaitDays": 90,
  "earlyRefundFeeRate": 15,
  "agreementSummary": "保证金用于约拍诚信担保..."
}
```

`POST /deposit/order/create`

请求：

```json
{
  "channel": "MINIAPP"
}
```

小程序返回：

```json
{
  "outTradeNo": "D202607081234560001",
  "businessType": "DEPOSIT",
  "channel": "MINIAPP",
  "amountFen": 1,
  "payParams": {
    "timeStamp": "1783499261",
    "nonceStr": "xxx",
    "package": "prepay_id=xxx",
    "signType": "RSA",
    "paySign": "xxx"
  }
}
```

H5 返回：

```json
{
  "outTradeNo": "D202607081234560001",
  "businessType": "DEPOSIT",
  "channel": "H5",
  "amountFen": 1,
  "h5Url": "https://wx.tenpay.com/cgi-bin/mmpayweb-bin/checkmweb?prepay_id=xxx"
}
```

`GET /deposit/order/status?out_trade_no=...`

当前用户只能查询自己的保证金订单。

### 用户端会员接口

保留现有 `POST /member/order/create`，增加 `channel(支付渠道)` 参数：

```json
{
  "planId": 1,
  "channel": "MINIAPP"
}
```

返回统一 `PaymentCreateResult(支付创建结果)`。旧字段 `packageValue` 可临时兼容一版前端，但新前端优先读取 `payParams.package`。

### 支付回调接口

新增 `POST /payment/wechat/notify`。

处理要求：

1. 读取原始请求体。
2. 读取 `Wechatpay-Timestamp`、`Wechatpay-Nonce`、`Wechatpay-Signature`、`Wechatpay-Serial` 请求头。
3. 使用微信支付公钥 ID 和公钥验签。
4. 用 APIv3 密钥解密 resource(资源数据)。
5. 通过 `out_trade_no(商户订单号)` 定位 `t_payment_order(统一支付流水)`。
6. 在事务内做条件状态推进和业务权益发放。
7. 成功返回微信支付要求的成功响应；失败返回失败响应，并保留排障日志。

旧 `POST /wxpay/notify` 保留，只处理旧 `APIv2(第二版接口)` XML(可扩展标记语言)订单。

### 后台接口

新增保证金配置：

- `GET /admin/deposit/config`
- `PUT /admin/deposit/config`

新增保证金订单：

- `GET /admin/deposit/orders`

新增统一支付流水：

- `GET /admin/payment/orders`
- `GET /admin/payment/orders/{id}`

后台仅能配置业务金额和开关，不能上传或编辑密钥。

## 前端设计

### 小程序保证金页

`frontend/src/pages-sub/user/credit.vue(信用担保页)` 调整：

- 删除硬编码 `DEPOSIT_FEE_FEN = 19900`。
- 进入页面调用 `GET /deposit/config`，展示后端返回的金额。
- 点击支付调用 `POST /deposit/order/create`，`channel=MINIAPP`。
- 用后端返回的 `payParams(支付参数)` 调用 `uni.requestPayment(统一支付 API)`。
- 支付成功后轮询 `GET /deposit/order/status`，以服务端状态为准，不只信前端支付回调。
- 如果配置关闭，展示“保证金服务暂未开放”。

### H5 保证金页

同一页面编译到 H5 时：

- 调用 `POST /deposit/order/create`，`channel=H5`。
- 后端返回 `h5Url(支付跳转链接)` 后跳转。
- 支付完成返回后展示“正在确认支付结果”，轮询订单状态。
- 若不在微信/手机浏览器可展示提示，避免桌面浏览器误操作。

### 会员页

`frontend/src/pages-sub/user/member/index.vue(会员中心页)` 调整：

- 创建会员订单时传入 `channel=MINIAPP/H5`。
- 小程序使用 `payParams(支付参数)` 调起支付。
- H5 使用 `h5Url(支付跳转链接)` 跳转。
- 继续使用服务端订单状态轮询发放结果。

### 管理后台

新增页面：

- `frontend-admin/src/views/deposit/config/index.vue(保证金配置页)`：配置正式金额、测试金额、测试开关、服务启停、退款规则文案。
- `frontend-admin/src/views/deposit/order/index.vue(保证金订单页)`：查询保证金订单状态、用户、金额、支付渠道、微信交易号。
- `frontend-admin/src/views/payment/order/index.vue(支付流水页)`：统一排障视图，查询业务类型、渠道、状态、商户订单号、微信交易号、错误信息。

页面风格保持现有 Element Plus(组件库)后台风格，不做营销式布局。

## 事务一致性、幂等与锁

这是本设计的硬约束。

### 创建订单

保证金订单创建：

1. 后端读取保证金配置并锁定金额快照。
2. 查询当前用户最近未支付保证金订单。
3. 若 5 分钟内存在同金额同渠道待支付订单，可复用；否则将旧待支付订单标记为 `CLOSED` 或创建新订单。
4. 创建 `t_deposit_order(保证金订单)` 和 `t_payment_order(统一支付流水)`。
5. 调用微信支付 APIv3 下单。
6. 回写 `prepay_id` 或 `h5_url`。

会员订单创建：

1. 后端读取套餐并锁定套餐快照。
2. 复用现有 5 分钟内同套餐待支付订单。
3. 创建或复用 `t_member_order(会员订单)`。
4. 创建或复用 `t_payment_order(统一支付流水)`。
5. 调用微信支付 APIv3 下单。

### 支付回调

支付回调处理必须在一个领域事务内完成：

```text
begin transaction
  select payment_order by out_trade_no
  update payment_order set status = PAID where out_trade_no = ? and status = PENDING
  if updated == 0:
      return success as duplicate notification

  if business_type == DEPOSIT:
      update deposit_order set status = PAID where out_trade_no = ? and status = PENDING
      update user set creditflag = '1' where id = ?

  if business_type == MEMBER:
      update member_order set status = PAID where out_trade_no = ? and status = PENDING
      grant member duration
      grant gift ppd and write record
      write member operation log
commit
```

关键点：

- `out_trade_no(商户订单号)` 必须唯一。
- 状态推进使用 `where status = PENDING` 条件更新，确保只有第一次成功回调发放权益。
- 可叠加 `version(乐观锁版本)`，防止后台人工操作或定时任务并发冲突。
- 回调重复、微信重试、前端重复轮询都不能重复发放保证金或会员权益。
- 如果微信通知金额与本地 `amount_fen(金额分)` 不一致，拒绝发放权益并记录异常。
- 如果业务订单不存在、用户不存在、金额不一致，支付流水标记异常，不发放权益。

### 主动查单

如果前端支付成功但回调延迟：

- 前端只展示“确认中”，不直接开通。
- 后端可提供 `PaymentQueryService(支付查询服务)` 主动查微信订单。
- 主动查单确认 `SUCCESS(成功)` 后，复用同一套 `markPaidIfPending(待支付转已支付)` 逻辑，不新增第二条发放路径。

## 错误处理与可观测性

错误码建议：

- `2001`：微信服务通信失败。
- `2002`：微信下单失败，响应 message(消息) 包含脱敏后的微信错误描述。
- `2003`：微信支付状态确认失败。
- `2010`：支付配置缺失。
- `2011`：支付金额不一致。
- `2012`：支付回调验签失败。

日志要求：

- 下单失败日志包含 `businessType(业务类型)`、`channel(渠道)`、`outTradeNo(商户订单号)`、微信错误码、微信错误描述。
- 回调失败日志包含 `Wechatpay-Serial(微信支付序列号)`、`outTradeNo(商户订单号)`、失败阶段。
- 任何日志不得打印密钥、私钥、公钥全文、Token(登录令牌)、用户手机号完整值。

## 测试策略

后端单元测试：

- 保证金金额配置：测试金额开启时使用 `1` 分，关闭时使用正式金额。
- 保证金订单复用：5 分钟内重复点击不生成多条有效待支付订单。
- 支付回调幂等：重复回调只更新一次用户 `creditflag(信用担保标记)`。
- 会员回调幂等：重复回调只延长一次会员有效期，只赠送一次拍拍豆。
- 金额不一致：不发放权益。
- 状态条件更新：`PENDING -> PAID` 成功一次，后续重复更新返回 0。

后端集成/接口测试：

- `POST /deposit/order/create` 分别测试 `MINIAPP` 和 `H5`。
- `POST /member/order/create` 分别测试 `MINIAPP` 和 `H5`。
- `POST /payment/wechat/notify` 验签失败返回失败，验签成功推进状态。
- 管理后台保证金配置保存和读取。

前端测试：

- 保证金页展示后端金额，不再展示硬编码 `199`。
- 小程序支付参数缺失时提示“支付参数不完整”。
- H5 收到 `h5Url(支付跳转链接)` 后跳转。
- 会员页按平台选择支付渠道。

手工验收：

1. 后台确认保证金配置测试金额为 `0.01` 元。
2. 小程序端进入信用担保页，展示 `0.01` 元。
3. 小程序下单成功并调起微信支付。
4. 支付回调后，用户 `creditflag` 变为 `1`。
5. H5 下单返回 `h5Url`，跳转微信支付页面。
6. 会员小程序支付后会员状态生效。
7. 会员 H5 支付后会员状态生效。
8. 重复发送同一回调，不重复发放会员时长或拍拍豆。

## 上线顺序

1. 先部署数据库脚本，新增表和字段。
2. 配置生产环境变量，确保公钥 ID、公钥文件、商户私钥和回调地址可用。
3. 部署后端，保留旧 `/wxpay/notify`。
4. 部署管理后台，先将保证金测试金额设置为 `0.01` 元。
5. 部署新版小程序/H5 前端。
6. 使用测试用户完成保证金与会员支付闭环。
7. 验证通过后，将保证金正式金额从 `1` 分切回 `19900` 分。

## 回滚策略

- 如果新版 APIv3 下单失败，可通过前端开关暂时关闭保证金入口，会员入口保留但提示维护。
- 旧 APIv2 `/wxpay/notify` 和历史订单列表保留，不影响旧订单查询。
- 数据库新增表字段不破坏旧字段；回滚应用不需要立即回滚 DDL(数据库结构变更)。
- 支付回调接口可在网关层切回旧地址，但新订单不应混用旧回调。

## 安全补充

当前对话中出现过 APIv2(第二版接口)密钥和用户 Token(登录令牌)。这些信息应视为已泄露：

- 轮换微信支付 APIv2 密钥；即使新链路使用 APIv3，也应避免旧密钥继续可用。
- 让对应用户重新登录，或在后端轮换 JWT(令牌)签发密钥使旧 Token 失效。
- 不把这些敏感值写入任何提交。

## 开放问题与默认决定

- H5 支付域名和场景信息由生产环境变量提供；代码只做配置校验。
- 首期 H5 支付使用微信支付 H5 下单返回的 `h5_url(支付跳转链接)`，不做公众号 JSAPI(网页内调起支付)。
- APP 支付首期不实现，但 `PaymentChannel(支付渠道)` 枚举预留 `APP`。
- 保证金退款只做配置展示，不做自动退款流程。
