# 消息推送子系统 — 技术实现文档

> 范围:`/Users/lizhenwei/workspace/vueworkspace/ypat-workspace`
> 后端 Spring Cloud 多模块 + 前端 3 套(uniapp-H5 / admin-Vue3 / website-Next.js)
> **当前架构定性:站外微信订阅消息(拉模式)+ 站内 HTTP 轮询(非实时),无 WebSocket / MQ**

---

## 1. 全局架构(一张图)

```
┌─────────────────── 业务事件层(谁触发)────────────────────────┐
│  用户发起约拍            YpatInfoController.submit          │
│  约拍信息发布审核通过/驳回 ManageController.audit           │
│  实名认证审核            OauthController                    │
│  对方查看联系方式        UserService.recordViewWriteMess   │
│  管理员收到新发布/认证   同上,推 Const.SYS_ADMIN          │
└──────────────────────────────┬──────────────────────────────┘
                               │ 直接同步调用
                               ▼
┌─────────────────── 推送网关层 ──────────────────────────────┐
│  WxMessClient.sendMsg(accessToken, openid, MessType, page, contentMap)│
│  ├─ 拼模板 JSON: WxMess.build() 按 MessType 选 template_id│
│  └─ POST https://api.weixin.qq.com/cgi-bin/message/subscribe/send│
└──────────────────────────────┬──────────────────────────────┘
                               │
              ┌────────────────┴────────────────┐
              ▼                                 ▼
┌──────────────────────┐         ┌─────────────────────────────┐
│ 微信侧(站外)        │         │ 数据库侧(站内)               │
│ 小程序订阅消息       │         │ t_mess_info 表               │
│ (受微信配额限制)    │         │ 永远是 source of truth       │
└──────────────────────┘         └─────────────────────────────┘
                                              ▲
                                              │
                              ┌───────────────┴────────────────┐
                              │ 前端 HTTP 拉模式               │
                              │ App.onShow → refreshUnreadCount│
                              │ 消息页 onShow → load(true)    │
                              │ /my/ypat/{rec|send}/unread/count│
                              │ /my/ypat/{rec|send}/list      │
                              └────────────────────────────────┘
```

**关键约束:**
1. **微信推送是"通知"不是"数据"** — 用户点开卡片跳转到小程序页面,但消息的持久化、列表、未读数全部走自家 `t_mess_info` 表
2. **失败隔离** — 所有 `sendMsg()` 调用都在 `try/catch` 里,微信侧失败不影响主业务流程
3. **没有 WebSocket / SSE / 长连接** — 站内消息实时性靠前端轮询(每次 App 切前台触发一次)

---

## 2. 后端实现

### 2.1 数据库 — `t_mess_info`

`backend/system-domain/src/main/java/com/ypat/entity/MessInfo.java`

| 字段 | 类型 | 作用 |
|---|---|---|
| `id` | Long PK | 自增主键 |
| `type` | String | 消息类型,对应 `MessType` 枚举的 `value`("1"~"5") |
| `content` | String | 站内展示文案(供站内消息中心列表使用) |
| `status` | String | 业务状态 |
| `messviewflag` | String | 接收方是否已读(`yes`/`no`) |
| `linkwayflag` | String | 接收方是否已查看联系方式(`yes`/`no`,与 PPD 扣费联动) |
| `sendperid` | FK→User | 发起方 |
| `recperid` | FK→User | 接收方 |
| `credate` | Date | 创建时间 |
| `ypatid` | FK→YpatInfo | 关联约拍(消息详情跳转目标) |

`@NamedEntityGraph(MessInfo.all)` 预加载 `sendper/recper/ypatInfo.user`,避免列表 N+1 查询。

### 2.2 消息类型枚举 — `MessType`

`backend/system-object/src/main/java/com/ypat/enums/MessType.java`

```java
public enum MessType {
    send ("1", "约拍"),          // A 向 B 发起约拍 → 推 B
    oauth("2", "实名认证审核"),   // 用户提交认证 → 推 SYS_ADMIN
    audit("3", "发布信息审核"),   // 约拍审核通过/驳回 → 推发布者
    view ("4", "已查看消息"),    // B 查看了联系方式 → 推 A(自动站内消息,无微信推送)
    order("5", "订单通知");      // 新发布/认证待审 → 推 SYS_ADMIN
}
```

### 2.3 推送客户端 — `WxMessClient`

`backend/system-wap/src/main/java/com/ypat/third/wxmess/WxMessClient.java`(system-web 有镜像副本)

职责:封装微信开放平台 4 个 HTTP 接口,**无重试/无降级/无队列**,靠外层 try/catch 兜底。

| 方法 | 微信 API | 用途 |
|---|---|---|
| `getAccessToken()` | `/cgi-bin/token?grant_type=client_credential` | 小程序 access_token |
| `getAccessTokenPub()` | 同上(公众号 appid) | 公众号 access_token |
| `sendMsg(token, openid, type, page, contentMap)` | `/cgi-bin/message/subscribe/send` | **核心:发订阅消息** |
| `qrCode(token, scene, page)` | `/wxa/getwxacodeunlimit` | 小程序码 |
| `pubQrCode(token, scene_id)` | `/cgi-bin/qrcode/create` | 公众号二维码 |

**`sendMsg` 内部流程:**
```
WxMessClient.sendMsg(token, openid, type, page, contentMap)
   └─ WxMess.build(openid, type, page, contentMap)  // 选 template_id + 填字段
        └─ switch (type):
             case send  → TEMP_0 + thing3/date2/thing4 (地区/时间/备注)
             case oauth → TEMP_1 + phrase3/phrase1/thing2 (类型/结果/备注)
             case audit → TEMP_2 + thing1/phrase2/date3/thing4 (内容/结果/时间/备注)
             case order → TEMP_3 + thing2/name3 (类型/对象)
   └─ GsonUtils.toJson(WxMess)
   └─ POST 微信 API,body 形如:
      {
        "touser": "oXXX",
        "template_id": "bcu2sYUoDPwIcB-1Jyx_HAoJVZtzgvuEyR8d0qf-dXE",
        "page": "pages-sub/content/message-detail?id=42",
        "data": {
          "thing3": {"value": "北京·朝阳"},
          "date2":  {"value": "2026-07-09 14:30"},
          "thing4": {"value": "小王向您发起了约拍"}
        }
      }
```

**模板 ID(`Const.java`):**
```
TEMP_0 = "bcu2sYUoDPwIcB-1Jyx_HAoJVZtzgvuEyR8d0qf-dXE"   // 约拍
TEMP_1 = "_uTkXi5VM9BOAXOMMUdibQPioXMwNfngC90CJgynSTg"   // 实名认证审核
TEMP_2 = "i6ydNmF4EKyAQomQuopOeCWHKmMytHDjP3W0anbEq4w"   // 发布审核
TEMP_3 = "Bv1tvnuGZeKpwRxTz-QtOt_btN0tlkDjMgyeP-Iz16s"   // 新订单通知
SYS_ADMIN = "o5ZmB4kyCVPskEOaO0PK1He0Kl7w"              // 管理员 openid(给管理员发)
```

### 2.4 触发点全景(5 处)

| # | 位置 | 触发条件 | 接收方 | MessType | 备注 |
|---|---|---|---|---|---|
| 1 | `system-wap/YpatInfoController.submit` | 用户发布约拍 | `SYS_ADMIN` | `order` | type="发布待审批",page=空 |
| 2 | `system-web/ManageController.audit` | 管理员审核通过/驳回 | 发布者 openid | `audit` | 通过跳 `PAGE_PUB_TG`,驳回跳 `PAGE_PUB_BTG` |
| 3 | `system-wap/OauthController.add` | 用户提交实名认证 | `SYS_ADMIN` | `order` | type="实名待审批" |
| 4 | `system-web/ManageController` oauth 审核段 | 实名认证审核完成 | 申请人 openid | `oauth` | 通过/驳回两种文案 |
| 5 | `system-wap/MypatInfoController.myRecAdd` | A 向 B 发起约拍 | B 的 openid | `send` | 含 A 昵称 + 约拍地区 + 时间 |

**调用模板:**
```java
// system-wap/MypatInfoController.java:117-142
@RequestMapping(value = "/my/ypat/rec/add", method = {POST, PUT})
public String myRecAdd(MessInfoQo messInfoQo) {
    String res = systemServiceClient.myRecAdd(messInfoQo);
    // 推送消息
    try {
        String accessToken = wxMessClient.getAccessToken();
        if (accessToken != null) {
            String page = Const.PAGE_MESS;
            // ... 拼 contentMap(area/time/note)
            wxMessClient.sendMsg(accessToken, ypatInfoQo.getUserQo().getOpenid(),
                                 MessType.send, page, contentMap);
        }
    } catch (Exception e) {
        logger.error("消息推送失败:", e);
    }
    return res;
}
```

**典型问题 — 短信降级旁路:**
`system-web/ManageController.audit` 第 155-161 行:审核通过时,如 `messflag=yes`,还会用 `SmsUtils.sendMsg()` 给同城同职业用户群发短信。这是一条**绕过 WxMessClient 的旁路推送**,如果未来要做推送中心化,要单独处理。

### 2.5 站内查询接口(`MessServiceClient` Feign → `MessInfoService`)

**所有"我的消息"查询都从 `system-domain/MessInfoService` 出:**

| Controller | URL | 用途 |
|---|---|---|
| `MessInfoController` (restapi) | `/service/mess/get` | Feign 服务间调用 |
| `MypatInfoController` (wap) | `/mess/get` | 消息详情 |
| `MypatInfoController` (wap) | `/mess/add` | 添加消息(测试/手工) |
| `MypatInfoController` (wap) | `/my/ypat/rec/unread/count` | **收到未读数(红点)** |
| `MypatInfoController` (wap) | `/my/ypat/send/unread/count` | **发出未读数(我发的被人查看)** |
| `MypatInfoController` (wap) | `/my/ypat/unread/count` | 旧版合并未读 |
| `MypatInfoController` (wap) | `/my/ypat/rec/list` | **收到列表**(`MessType.send`) |
| `MypatInfoController` (wap) | `/my/ypat/send/list` | **发出列表**(`MessType.view`) |
| `MypatInfoController` (wap) | `/my/ypat/sc/list` | 我的收藏 |
| `AdminMessController` (wap) | `/admin/mess/list` | 管理后台消息列表 |

**未读数查询 SQL(节选自 `MessInfoService`):**
```java
Long sendCount = messInfoRepository.countRecUnread(
    MessType.view.value, userid, YesNo.no.value);
```

---

## 3. 前端实现(uniapp-H5 + 小程序)

### 3.1 状态管理 — Pinia Store

`frontend/src/stores/message.ts`(完整 25 行)

```typescript
export const useMessageStore = defineStore('message', () => {
  const recUnreadCount = ref(0)
  const sendUnreadCount = ref(0)
  const totalUnread = computed(() => recUnreadCount.value + sendUnreadCount.value)

  async function refreshCounts(userid, type = '0') {
    const [received, sent] = await Promise.all([
      messageApi.getRecUnreadCount(type, userid),
      messageApi.getSendUnreadCount(type, userid),
    ])
    recUnreadCount.value = Number(received.data || 0)
    sendUnreadCount.value = Number(sent.data || 0)
  }
  return { recUnreadCount, sendUnreadCount, totalUnread, refreshCounts }
})
```

**注意:** 实际生产中,红点用的是 `useUserStore.refreshUnreadCount()`(`stores/user.ts:222`),它**直接调用相同的 2 个 API**,但合并成一个 `unreadCount`。`useMessageStore` 看似冗余,可能在重做中,目前未在 `App.vue` 中被 `onShow` 引用。

### 3.2 API 封装 — `frontend/src/api/modules/message.ts`

```typescript
getMessageDetail(id, userid)         → GET /mess/get
getRecUnreadCount(type, userid)      → GET /my/ypat/rec/unread/count
getSendUnreadCount(type, userid)     → GET /my/ypat/send/unread/count
```

### 3.3 页面结构

| 文件 | 路径 | 功能 |
|---|---|---|
| `pages/message/index.vue` | TabBar 第 4 栏 | 消息中心:收到/申请 双 Tab + 卡片列表 + 下拉刷新/触底加载 |
| `pages-sub/content/message-detail.vue` | 详情 | 头像/昵称/内容/查看联系方式(扣 PPD) |
| `utils/message-navigation.ts` | 导航逻辑 | `resolveMessageNavigation()` 收到 tab → 详情页;申请 tab → 约拍详情页;无 ypatid → 二次查详情 |

### 3.4 红点轮询 — `App.vue:onShow`

```typescript
onShow(() => {                       // 每次小程序从后台切回前台
  setTimeout(() => {
    const userStore = useUserStore()
    if (userStore.isLoggedIn) {
      userStore.refreshUnreadCount() // 并发拉 rec/send 未读数
    }
  }, 0)
})
```

**这是当前唯一的"实时"机制** — 切后台再切回触发一次。前台运行中**没有任何轮询 timer**。`stores/member.ts:61` 那个 `setInterval` 跟消息无关,是会员状态轮询。

### 3.5 消息详情页关键交互 — `message-detail.vue:96-135`

```
[用户点击"查看联系方式"]
   ├─ 检查 PPD 余额(需 VIEW_CONTACT_PPD = 3)
   ├─ 余额不足 → 弹窗 → 跳转充值页
   └─ 余额充足 → 弹确认框 → revealContact()
        ├─ userApi.getLinkWay(sendperid, messageId)
        │      └─ 后端:扣 PPD + 写 t_mess_info.linkwayflag=yes
        │      └─ 返回 LinkWay(mobile/wx/qq/wb)
        └─ userStore.updateUserInfo() // 刷新本地 ppd 余额
```

注意:这步**没有反向推送** — A 看不到 B 已读/B 已查联系方式(B 查联系方式时**有**写一条 `type=view` 的站内消息,但**没有触发微信推送**到 A,只在站内可见)。

---

## 4. 数据/事件流(以"用户 A 向用户 B 发起约拍"为例)

```
时序:
1. A 在小程序发起约拍
   POST /my/ypat/rec/add { ypatid: 42, content: "..." }
   ↓ (system-wap/MypatInfoController.myRecAdd)

2. Feign → system-service/system-user → system-domain/MessInfoService.myRecAdd
   ├─ 写 t_mess_info 表: type="1"(send), sendperid=A, recperid=B, ypatid=42,
   │   messviewflag=no, content=B 的约拍文案
   └─ 返回 res

3. 同 controller 同步推微信
   ├─ wxMessClient.getAccessToken()        // 调微信 1 次
   ├─ userClient.get(A.id) → 拿 A.nickname
   ├─ ypatClient.get(42) → 拿 42.city
   └─ wxMessClient.sendMsg(token, B.openid, MessType.send, page, contentMap)
        └─ POST 微信 subscribe/send 1 次

4. 用户 B 收到微信"服务通知"(前提:B 已订阅过该模板)

5. B 打开小程序(若之前在后台)
   onShow → refreshUnreadCount → /my/ypat/rec/unread/count → 看到红点 +1
   ↓
   切到消息 Tab → load() → /my/ypat/rec/list → 看到 A 的卡片

6. B 点击卡片 → /pages-sub/content/message-detail?id=123

7. B 点击"查看联系方式"(扣 3 PPD)
   └─ 后端:扣 PPD + linkwayflag=yes + 给 A 写一条 type="4"(view) 的站内消息
      ⚠️ 此时不给 A 推微信!A 只能下次切回消息 Tab 看到
```

---

## 5. 已知问题与改进建议

### 5.1 实时性
**问题:** 当前红点完全靠 `App.onShow` 拉模式 — 用户在站内浏览期间,对方发了消息,**不会立刻看到红点**(必须切后台再回前台)。
**改进方向:**
- 短期:在 TabBar 切换时、详情页返回时也调一次 `refreshUnreadCount`,成本低
- 中期:加 WebSocket(`/ws/message?token=xxx`),后端在 `MessInfoService.myRecAdd` 写表后推一条到 `recperid` 通道
- 长期:WebSocket + 微信订阅消息双轨(微信仍做离线到达率兜底)

### 5.2 access_token 管理
**问题:** `WxMessClient.getAccessToken()` 每次都调微信,微信侧限频 2000次/天(实际够用但浪费)。当前实现**没有缓存**(每次推送都重新拉)。
**改进:** 加 Redis 缓存 `access_token → expires_in`,剩余 < 5min 时刷新。

### 5.3 推送失败可观测性
**问题:** 所有推送都在 `try/catch` 里 `logger.error("消息推送失败", e)`,微信返回 `errcode` 也只是 `logger.info` 了一下响应 body,没有落库。
**改进:** 加一张 `t_push_log` 表,记录 (openid, template_id, errcode, errmsg, retry_count, time),失败重试机制也建在上面。

### 5.4 模板硬编码
**问题:** `Const.TEMP_0/1/2/3` 是 String 常量,模板字段映射写在 `WxMess.build()` 的 switch 里。新增模板需要改源码 + 重新部署。
**改进:** 数据库表 `t_msg_template(type, template_id, field_mapping_json)`,运行时动态拼。

### 5.5 订阅消息前置授权
**问题:** 微信小程序**一次性订阅**需要用户在触发场景弹窗授权。当前代码**没看到前端主动调 `wx.requestSubscribeMessage`**(全仓搜未命中)。意味着用户很可能从来没收到过订阅消息,因未授权。
**改进:** 在发起约拍按钮/审核通过事件**之前**触发 `wx.requestSubscribeMessage({ tmplIds: [TEMP_0] })`,把授权和推送解耦。

### 5.6 站内消息与微信推送不同步
**问题:** `MessType.view`(对方查看了联系方式)**只写站内表,不推微信**。`MessType.send`(发起约拍)**两者都做**,但用了 `MessType.send.value="1"` 作为站内 type,微信也是同一个 template,逻辑耦合。
**改进:** 拆分 "站内消息" 与 "微信推送" 为 2 个独立通道(都基于同一事件),各自配置开关。

### 5.7 多端一致性
**问题:** 当前只看了 `frontend`(uniapp-H5/小程序)的消息实现。`frontend-admin`(管理后台 Vue3)和 `frontend-website`(Next.js 官网)的消息代码未覆盖。
**建议:** 单独确认下管理后台的"消息推送监控"页面需求 — 上面 5.3 的 `t_push_log` 表可顺便给后台做一个"今日推送失败率"看板。

---

## 6. 关键文件清单(快速索引)

### 后端
| 文件 | 角色 |
|---|---|
| `system-domain/entity/MessInfo.java` | t_mess_info 实体 |
| `system-domain/repository/MessInfoRepository.java` | JPA Repository |
| `system-domain/service/MessInfoService.java` | 消息写表 + 未读数查询 |
| `system-domain/service/UserService.java:432` | `MessType.view` 写入点(B 查联系方式 → 给 A 写一条) |
| `system-object/MessInfoQo.java` | 查询条件 DTO |
| `system-object/enums/MessType.java` | 5 种消息类型 |
| `system-wap/third/wxmess/WxMessClient.java` | 微信推送主客户端 |
| `system-wap/third/wxmess/WxMess.java` | 模板 JSON builder |
| `system-wap/comm/Const.java` | TEMP_0/1/2/3 + SYS_ADMIN openid |
| `system-wap/controller/MypatInfoController.java` | 我的消息查询 + 触发推送 1 处 |
| `system-wap/controller/YpatInfoController.java` | 约拍发布 → 推 SYS_ADMIN |
| `system-wap/controller/OauthController.java` | 实名认证提交 → 推 SYS_ADMIN |
| `system-web/controller/ManageController.java` | 审核 → 推发布者 + 短信旁路 |
| `system-wap/controller/AdminYpatController.java` | 约拍审核(另一处,与 web 不同入口) |
| `system-restapi/controller/MessInfoController.java` | Feign 服务间消息接口 |

### 前端
| 文件 | 角色 |
|---|---|
| `frontend/src/stores/message.ts` | 消息 Pinia store(未读数) |
| `frontend/src/stores/user.ts:222` | `refreshUnreadCount()` 实际被 App.vue 调用的红点函数 |
| `frontend/src/api/modules/message.ts` | 3 个 API 封装 |
| `frontend/src/api/modules/ypat.ts` | `getMyReceivedList / getMySentList`(消息列表) |
| `frontend/src/pages/message/index.vue` | 消息中心主页(TabBar 第 4 栏) |
| `frontend/src/pages-sub/content/message-detail.vue` | 消息详情(查联系方式扣 PPD) |
| `frontend/src/utils/message-navigation.ts` | 卡片点击跳转路由解析 |
| `frontend/src/App.vue` | onShow 触发未读刷新 |

### 配置 / 文档
| 文件 | 说明 |
|---|---|
| `system-wap/application.yml`(或 .properties) | `wx_appid` / `wx_appsecret` |
| 微信公众平台 → 小程序后台 → 订阅消息 | 模板 ID 在此处申请,需与 `Const.TEMP_*` 一一对应 |

---

**文档生成时间:** 2026-07-09
**分析基线:** `main` 分支当前代码
**覆盖范围:** 后端 4 个模块 + 前端 uniapp-H5/小程序 4 个文件;admin/website 端的消息部分待补