# 03 接口契约矩阵 (API Contract Matrix)

> **契约事实源**: 后端 `backend/system-wap` Controllers (前端网关层)。
> `system-restapi` 的 `/service/*` 为内部微服务,前端不直接调用。
> 关键事实: **所有 wap 端点均为表单绑定 (form / query),无一使用 `@RequestBody` JSON**。
> 响应包: `ResponseApiBody { code:int, msg:String, res:Object }`,成功码 `200`,data 字段为 **`res`**。
> 部分端点直接返回原始 JSON 字符串(Feign 透传,无包裹)。
> Token: 请求头 `Token`(裸 JWT,无 Bearer)。`GET /**` 在 SecurityConfig 中 permitAll,鉴权由各 handler 自行 `UserUtil.getUserId()`。

## A. 请求层对比

| 维度 | 旧版 91pai | 新版 frontend | 后端要求 | 结论 |
|---|---|---|---|---|
| 封装文件 | common/vmeitime-http/interface.js | src/api/request.ts | — | — |
| dev Base URL | http://localhost:8088 (H5: /dpc 代理) | http://localhost:8088 | — | 一致 |
| prod Base URL | https://www.91qupaier.com | http://82.156.14.216:8088 | — | **差异(见gap)** |
| 默认 Content-Type | x-www-form-urlencoded | form (x-www-form-urlencoded) | form | ✅ 一致且正确 |
| Token 头 | `Token` | `Token` | `Token` | ✅ |
| Token 存储 key | UNI_LOCAL_token | ypat_token | — | key 不一致(见04) |
| 401/刷新 | 无(空 case 401) | 有(401/1001→/user/token?mobile→重试) | /user/token GET+mobile | 新版更完善 ✅ |
| 响应成功判定 | res.code===200 | String(code)==='200' | code==200 | ✅ |
| data 字段 | res.res | res(优先) 否则 result,含二次JSON.parse | res | ✅ |
| 超时 | 无 | 15000ms | — | 新版更好 |
| 防重复提交 | 无 | pendingRequests Set | — | 新版更好 |

## B. 端点契约表(后端核验)

方法/绑定列以后端 Controller 为准。状态: ✅一致 / ⚠️待确认 / ❌不一致(需修复)

| 模块 | 场景 | 路径 | 后端方法 | 后端绑定 | 新版方法 | 新版函数 | 状态 |
|---|---|---|---|---|---|---|---|
| 登录 | 微信code换openid | /user/code | GET | query `code` | GET | user.wxLogin | ✅ |
| 登录 | 发短信验证码 | /user/sms/code | POST | form `mobile` | POST | user.sendH5LoginCode | ✅ |
| 登录 | 登录(微信/手机号) | /user/login | POST | form UserQo | POST | user.login / h5PhoneLogin | ✅ |
| 登录 | 刷新token | /user/token | GET | query `mobile` | GET | request.refreshToken | ✅ |
| 用户 | 获取资料 | /user/get | GET | query `id`(可空) | GET | user.getUserInfo | ✅ |
| 用户 | 更新资料 | /user/upd | POST | form UserQo+pics | POST | user.updateUser | ✅ |
| 用户 | 联系方式解锁 | /user/linkway/get | GET | query `userid`,`messid` | GET | user.getLinkWay | ✅ |
| 用户 | 同城同职业 | /user/findByCityAndProfess | **NOT FOUND(wap)** | 仅 /service 内部 | GET | user.findByCityAndProfess | ❌ **端点不存在** |
| 约拍 | 推荐列表 | /ypat/tc/list | GET | query YpatInfoQo | GET | ypat.getRecommendList | ✅ |
| 约拍 | 最新列表 | /ypat/zx/list | GET | query | GET | ypat.getLatestList | ✅ |
| 约拍 | 详情 | /ypat/get | GET | query `id` | GET | ypat.getDetail | ✅ |
| 约拍 | 发布 | /ypat/submit | POST | form YpatInfoQo(pics base64) | POST | ypat.submit | ✅ |
| 约拍 | 浏览量+1 | /ypat/yd/add | POST/PUT | form `ypatid` | PUT | ypat.addReadCount | ✅ (后端允许PUT) |
| 我的 | 报名 | /my/ypat/rec/add | POST/PUT | form MessInfoQo | PUT | ypat.applyYpat | ✅ (后端允许PUT) |
| 我的 | 收藏(只增) | /my/ypat/sc/add | POST/PUT | form `userid`,`ypatid` | PUT | ypat.addFavorite | ✅ (后端允许PUT) |
| 我的 | 我的发布列表 | /my/ypat/pub/list | **GET** | query YpatInfoQo | **POST** | ypat.getMyPublishList | ❌ **方法不符→405** |
| 我的 | 我的收藏列表 | /my/ypat/sc/list | **GET** | query YpatInfoQo | **POST** | ypat.getMyFavoriteList | ❌ **方法不符→405** |
| 我的 | 我收到的(报名我) | /my/ypat/rec/list | **GET** | query MessInfoQo | **POST** | ypat.getMyReceivedList | ❌ **方法不符→405** |
| 我的 | 我发出的(我报名) | /my/ypat/send/list | **GET** | query MessInfoQo | **POST** | ypat.getMySentList | ❌ **方法不符→405** |
| 我的 | 未读总数 | /my/ypat/unread/count | GET | query | GET | ypat.getUnreadCount | ✅ |
| 我的 | 收到未读 | /my/ypat/rec/unread/count | GET | query `type`,`userid`? | GET | message.getRecUnreadCount | ✅ |
| 我的 | 发出未读 | /my/ypat/send/unread/count | GET | query | GET | message.getSendUnreadCount | ✅ |
| 我的 | 拍拍豆记录 | /my/ppd/list | GET | query RecordQo | — | (新版用 /record/findPage) | ⚠️ 见下 |
| 消息 | 消息详情 | /mess/get | GET | query `id` | GET | message.getMessageDetail | ✅ |
| 实名 | OCR | /oauth/ocr | POST | form `cardfront` | POST | oauth.ocrIdCard | ✅ |
| 实名 | 提交 | /oauth/add | POST | form OauthQo(pics base64) | POST | oauth.submitAuth | ✅ |
| 实名 | 我的实名 | /oauth/get | GET | token only | GET | oauth.getAuthDetail | ✅ |
| 实名 | 按id查实名 | /oauth/detail | GET | query `id` | GET | oauth.getAuthDetailByUserId | ✅ |
| 钱包 | 产品列表 | /product/list | GET | query ProductQo | GET | payment.getProductList | ✅ |
| 钱包 | 创建订单 | /order/create | POST | form OrderQo | POST | payment.createOrder | ✅ |
| 钱包 | 订单状态 | /order/status | GET | query `out_trade_no` | GET | payment.getOrderStatus | ✅ |
| 钱包 | 账单分页 | /bill/findPage | POST | form BillQo | POST | payment.getBillList | ✅ |
| 钱包 | 收支分页 | /record/findPage | GET | query RecordQo | GET | payment.getRecordList | ✅ |
| 内容 | Banner | /banner/list | GET | query | GET | content.getBannerList | ✅ |
| 内容 | 文章列表 | /article/list | GET | query | GET | content.getArticleList | ✅ |
| 内容 | 文章详情 | /article/get | GET | query `id` | GET | content.getArticleDetail | ✅ |
| 内容 | 地区 | /area/list | GET | none | GET | content.getAreaList | ✅ |
| 内容 | 模板id | /tmplid/list | GET | none | GET | content.getTemplateIds | ✅ |
| 内容 | 参数(realname开关) | /param/list | GET | none | GET | content.getParams | ✅ |

## C. 需修复的接口问题(汇总到 06-gap-register)

- **GAP-API-01 (P1)**: `getMyPublishList`/`getMyFavoriteList`/`getMyReceivedList`/`getMySentList` 使用 POST,后端为 `@GetMapping`,会 405 失败。→ 改为 GET + query。已验证 `MypatInfoController.java:48/90/97/112`。
- **GAP-API-02 (P2)**: `user.findByCityAndProfess` 调用的 `/user/findByCityAndProfess` 在 wap 网关**不存在**(仅 /service 内部,且唯一调用点在 YpatInfoController:175 被注释)。→ 确认调用点,移除或改造。
- **GAP-API-03 (P1/安全)**: `.env.production` 使用 `http://82.156.14.216:8088`,而 `env.ts` 在 prod 强制 HTTPS 会抛错 → 生产运行/构建崩溃。需决策(见 05/06)。
- **后端安全(超出本次范围,记录)**: `/user/token` 凭裸 `mobile` 即可签发 JWT,无旧 token / 凭证校验;`GET /**` permitAll。建议后端修复,不在本前端任务改动。

## D. 旧版独有/未迁移端点(参考)
`/bd/code` `/bd/login`(百度小程序登录)、`/ypat/audit` `/oauth/audit` `/ypat/upRecom`(管理员审核,旧版 adminList 驱动)、`/my/frd/list`(好友)、`/my/ypat/app/list`、`/my/ypat/head/list`、`/qr/code`。→ 模块J/各模块确认是否前端需要。
