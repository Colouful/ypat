# Banner 跳转字段设计

日期：2026-07-08
工作区：`/Users/lizhenwei/workspace/vueworkspace/ypat-workspace/.worktrees/codex-banner-link-fields`
目标端：`frontend-admin`（管理后台）、`frontend`（新版 UniApp 前端）、`backend`（Spring Cloud 后端）

## 背景

当前 `frontend-admin`（管理后台）已经支持 banner（横幅）新增、编辑、发布和撤回，但只能维护标题和图片。`frontend`（新版 UniApp 前端）的首页 `HomeBanner`（首页横幅组件）已经展示 `/banner/list`（横幅列表接口）返回的数据，并且存在临时的链接字段猜测逻辑，会从 `url`、`linkurl`、`linkUrl`、`jumpurl`、`jumpUrl`、`path` 中尝试找跳转地址。

后端 `BannerQo`（横幅传输对象）和 `t_banner`（横幅表）目前没有正式跳转字段，导致后台无法配置“是否可跳转”“跳转类型”“小程序页面”“外部地址”等运营能力。本次设计把这个扩展点收敛为明确契约，打通后台录入、后端存储、公开接口和新版小程序点击行为。

## 范围

本次包含：

1. `t_banner`（横幅表）新增跳转相关字段，并提供 MySQL（关系型数据库）DDL（数据定义语言）脚本。
2. `Banner`（横幅实体）和 `BannerQo`（横幅传输对象）新增字段，`/admin/banner/*`（后台横幅接口）和 `/banner/list`（公开横幅列表接口）透传这些字段。
3. `frontend-admin`（管理后台）横幅列表和编辑弹窗增加跳转配置。
4. `frontend`（新版 UniApp 前端）首页横幅点击支持小程序页面跳转和外部地址 `web-view`（小程序网页容器）跳转。
5. 当外部地址不能通过 `web-view`（小程序网页容器）打开时，降级为复制链接并提示用户。
6. 补充后端、管理后台和新版前端的关键测试或静态验证。

本次不包含：

1. 不修改 `91pai-master`（旧 UniApp Vue2 小程序）。
2. 不新增运营投放系统、点击统计、AB test（实验分流）或多端差异化投放。
3. 不做域名白名单管理后台；微信小程序业务域名仍由微信公众平台配置。
4. 不让 banner（横幅）跳转到任意 JavaScript（脚本语言）协议或非 HTTP（超文本传输协议）地址。

## 方案选择

采用方案 B：`web-view`（小程序网页容器）优先，复制链接降级。

理由：

1. 运营侧可以维护真实外部落地页，微信小程序业务域名配置正确时体验完整。
2. 未配置白名单或运行端不支持 `web-view`（小程序网页容器）时，复制链接不会让用户点击后无反馈。
3. 该方案只增加一个通用 `web-view`（小程序网页容器）页面，不需要为每个外部链接单独开发页面。

未采用方案：

1. 只复制链接：实现最简单，但无法承载活动落地页。
2. 后台允许录入但小程序不跳转：会让运营配置不可见，验收标准不清晰。

## 数据模型

新增字段使用现有项目的低驼峰缺省风格和历史字段习惯，API（接口）字段保持简单小写，避免破坏旧字段：

| 字段 | 类型 | 默认值 | 含义 |
| --- | --- | --- | --- |
| `jumpflag` | `VARCHAR(1)` | `0` | 是否可跳转：`0` 否，`1` 是 |
| `jumptype` | `VARCHAR(20)` | `miniapp` | 跳转类型：`miniapp` 小程序页面，`web` 外部地址 |
| `jumpurl` | `VARCHAR(500)` | `NULL` | 跳转目标。小程序页面保存页面路径，外部地址保存 `http` 或 `https` URL（统一资源定位符） |

数据库脚本放在 `backend/dev/mysql/20260708_alter_banner_link_fields.sql`。脚本需要使用 `ADD COLUMN`（增加列）方式，便于本地和测试环境执行。由于当前项目没有统一 Flyway（数据库迁移工具）落地到主线，本次延续 `backend/dev/mysql`（开发数据库脚本目录）的现有做法。

## 后端设计

### 对象字段

`backend/system-domain/src/main/java/com/ypat/entity/Banner.java` 新增：

- `jumpflag`
- `jumptype`
- `jumpurl`

`backend/system-object/src/main/java/com/ypat/BannerQo.java` 新增同名字段和 getter/setter（访问器/设置器）。

`CopyUtil`（对象复制工具）当前按字段名复制，实体和传输对象同名即可通过现有 `BannerService`（横幅服务）保存和返回。

### 保存规则

`backend/system-wap/src/main/java/com/ypat/controller/AdminBannerController.java` 在 `save`（保存）时增加轻量校验：

1. `jumpflag` 为空时视为 `0`。
2. `jumpflag=0` 时清空 `jumptype` 和 `jumpurl`，banner（横幅）点击回退为预览图片。
3. `jumpflag=1` 时 `jumptype` 必须为 `miniapp` 或 `web`，`jumpurl` 必填。
4. `jumptype=miniapp` 时，`jumpurl` 必须以 `/pages/` 或 `/pages-sub/` 开头。
5. `jumptype=web` 时，`jumpurl` 必须以 `http://` 或 `https://` 开头。
6. URL（统一资源定位符）长度不超过 500 字符。

后端不校验微信业务域名白名单，因为该配置属于微信平台运行时能力。小程序端负责在失败时降级复制链接。

## 管理后台设计

`frontend-admin/src/api/modules/banner.ts` 扩展 `Banner`（横幅类型）：

- `jumpflag?: string`
- `jumptype?: 'miniapp' | 'web' | string`
- `jumpurl?: string`

`frontend-admin/src/views/banner/list/index.vue` 列表新增“跳转”列：

- `jumpflag=1` 且 `jumptype=miniapp` 显示“小程序页面”。
- `jumpflag=1` 且 `jumptype=web` 显示“外部地址”。
- 其他显示“不跳转”。

`frontend-admin/src/views/banner/list/BannerEditDialog.vue` 编辑弹窗新增表单：

1. “是否跳转”：开关控件。
2. “跳转类型”：单选或下拉，选项为“小程序页面”“外部地址”。
3. “跳转目标”：输入框。
   - 小程序页面提示示例：`/pages/work/index`、`/pages-sub/work/detail?id=1`。
   - 外部地址提示示例：`https://example.com/activity`。

前端提交前做与后端一致的基础校验，减少无效请求，但以后端校验为准。

## 新版小程序设计

### 跳转解析

在 `frontend/src/components/business/HomeBanner.vue` 中把临时猜测逻辑替换为正式字段优先：

1. `jumpflag !== '1'`：不跳转，预览当前 banner（横幅）图片。
2. `jumptype === 'miniapp'`：打开 `jumpurl` 指向的小程序页面。
3. `jumptype === 'web'`：打开通用 `web-view`（小程序网页容器）页面，并把外部 URL（统一资源定位符）编码后作为参数传入。
4. 兼容历史或测试数据：如果正式字段缺失，可继续识别 `jumpUrl`、`linkUrl` 等旧候选字段，但不作为后台主契约。

### 页面打开规则

新增 `frontend/src/utils/banner-link.ts`：

- `resolveBannerAction`（解析横幅动作）：把 `Banner`（横幅）转换为 `preview`（预览）、`miniapp`（小程序页面）、`web`（外部网页）或 `copy`（复制链接）动作。
- `openBannerAction`（打开横幅动作）：执行 `uni.navigateTo`（普通页面跳转）、`uni.switchTab`（底部栏页面切换）、`uni.setClipboardData`（设置剪贴板）等平台 API（接口）。

新增 `frontend/src/pages-sub/content/web-view.vue`：

- 接收 `url` 参数。
- 使用 `<web-view :src="decodedUrl" />` 渲染外部地址。
- 若参数为空或非法，展示简洁错误状态，并提供复制按钮。

`frontend/src/pages.json` 在 `pages-sub/content`（内容分包）注册 `web-view`（小程序网页容器）页面。

### 降级策略

外部地址打开链路：

1. `HomeBanner`（首页横幅组件）点击外部地址。
2. 跳转到 `/pages-sub/content/web-view?url=<encoded>`。
3. 如果 `navigateTo`（普通页面跳转）失败，直接复制原链接并提示“链接已复制，请在浏览器打开”。
4. 如果 `web-view`（小程序网页容器）页面加载失败或 URL（统一资源定位符）非法，页面内展示复制入口。

## 错误处理

后台：

- 跳转开启但目标为空：提示“请输入跳转目标”。
- 小程序页面路径不合法：提示“请输入 /pages 或 /pages-sub 开头的小程序页面路径”。
- 外部地址不合法：提示“请输入 http 或 https 开头的外部地址”。

后端：

- 参数错误继续使用 `SysException`（系统异常）和 `ResponseCode.FAIL_PARA`（参数失败响应码）风格。
- 不合法配置不落库。

新版小程序：

- banner（横幅）列表加载失败仍静默隐藏，不影响首页。
- 单个 banner（横幅）跳转配置非法时退回图片预览或复制提示，不阻断其他 banner（横幅）。

## 测试策略

后端：

- 扩展 `AdminPublishControllerTest`（后台发布控制器测试），覆盖：
  - `jumpflag=1` 且 `jumptype=miniapp` 保存成功。
  - `jumpflag=1` 且 `jumptype=web` 保存成功。
  - 开启跳转但目标为空时失败。
  - 外部地址非 HTTP（超文本传输协议）时失败。

管理后台：

- 至少运行 `pnpm --dir frontend-admin type-check`（类型检查）。
- 如时间允许新增 API（接口）类型或组件行为测试，锁定字段名。

新版前端：

- 新增 `frontend/src/utils/__tests__/banner-link.test.ts`，覆盖：
  - 未开启跳转返回预览。
  - 小程序页面返回 `miniapp` 动作。
  - 外部地址返回 `web` 动作。
  - 非法外部地址返回 `copy` 或 `preview` 降级。
- 运行 `pnpm --dir frontend type-check`（类型检查）和相关 Vitest（单元测试框架）测试。

## 验收标准

1. 后台 `/banner/index`（横幅管理页）可以新增和编辑是否跳转、跳转类型、跳转目标。
2. 后台列表能看出每条 banner（横幅）是否配置跳转以及跳转类型。
3. `/banner/list`（公开横幅列表接口）返回 `jumpflag`、`jumptype`、`jumpurl`。
4. 新版小程序首页点击 `miniapp` 类型 banner（横幅）能进入配置的小程序页面。
5. 新版小程序首页点击 `web` 类型 banner（横幅）优先进入 `web-view`（小程序网页容器），失败时复制链接并提示。
6. 未配置跳转的 banner（横幅）保持当前图片预览行为。
7. 不修改 `91pai-master`（旧小程序）代码。
