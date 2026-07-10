# 意见反馈链路完善设计

## 背景

新版小程序当前已有 `pages-sub/user/feedback.vue` 意见反馈页，用户只能提交文本内容和联系方式。后端已有 `/feedback/add`、`/service/feedback/add` 和 `t_feedback` 入库链路，但表结构只覆盖 `userid`、`content`、`contact`、`status`、创建/更新时间。新版管理后台目前只有作品投诉治理页面，没有独立的意见反馈记录管理入口，因此运营侧看不到用户提交的反馈记录，也无法按类型、状态筛选或做处理闭环。

旧版前端项目仅作参考，不作为本次实现目标。所有开发基于 `origin/main` 创建的独立 worktree（独立工作区）完成，避免影响当前主工作区状态。

## 目标

本次优化把意见反馈从“只提交文本”升级为完整的轻量治理闭环：

- 小程序意见反馈页支持反馈类型、内容、联系方式、最多 3 张反馈图片。
- 后端保存反馈类型、图片、状态、处理备注、处理时间等字段，并保持已有提交接口兼容。
- 管理后台新增意见反馈记录页面，可查询、查看详情、预览图片、处理反馈。
- 反馈类型、状态、字段命名在小程序、后端、管理后台保持一致。
- 不引入复杂工单系统、客服会话、消息通知等超出本次范围的能力。

## 非目标

- 不改旧版 `91pai-master` 前端业务逻辑。
- 不做用户端“我的反馈历史”列表。
- 不做后台回复后的小程序站内信或模板消息推送。
- 不做反馈类型的后台动态配置，先使用固定枚举。
- 不重构通用上传、鉴权、菜单、分页体系，只复用现有模式。

## 方案对比

### 方案 A：轻量补字段

只在现有提交接口加 `type` 和 `pics` 字段，后台临时做只读列表。

优点是改动小、风险低。缺点是后台仍然没有处理闭环，状态字段价值有限，后续还会再次补处理接口和页面。

### 方案 B：完整轻量治理闭环（推荐）

新增固定反馈类型、图片上传/提交字段、后端列表/详情/处理接口、管理后台独立菜单页面。状态覆盖待处理、已处理、已忽略，后台支持备注和图片预览。

优点是一次打通用户提交到后台处理的最小完整链路，符合当前“没有看到反馈记录、增加类型、上传反馈图片”的核心诉求。缺点是涉及前端、小程序、后端、数据库脚本多处文件，需要按计划分步验证。

### 方案 C：工单化反馈系统

把反馈做成可分派、可回复、可通知、可多轮沟通的工单系统。

优点是能力完整。缺点是明显超过当前需求，会引入权限、通知、会话、工单生命周期等额外复杂度，不适合本次交付。

本次采用方案 B。

## 反馈类型

固定枚举如下，前端显示中文，后端存储稳定编码：

| 编码 | 展示名 | 说明 |
| --- | --- | --- |
| `function` | 功能异常 | 页面报错、按钮无响应、流程卡住等 |
| `experience` | 体验建议 | 交互、布局、文案、使用体验建议 |
| `account` | 账号/资料 | 登录、资料、认证、头像等账号相关问题 |
| `payment` | 支付/订单 | 充值、会员、保证金、订单支付等问题 |
| `content` | 内容/用户举报 | 平台内容、用户行为、违规线索 |
| `other` | 其他 | 无法归类的问题 |

默认选中 `function`。后端接受空类型时兼容为 `other`，但新版小程序必须传类型。

## 状态模型

反馈状态沿用字符串编码：

| 编码 | 展示名 | 说明 |
| --- | --- | --- |
| `0` | 待处理 | 用户新提交，后台未处理 |
| `1` | 已处理 | 后台确认已处理或已记录 |
| `2` | 已忽略 | 无效反馈、重复反馈、无法处理 |

状态只允许从 `0` 变为 `1` 或 `2`。已处理记录再次处理时返回明确错误，避免覆盖处理结论。

## 数据设计

在 `t_feedback` 基础上新增字段：

- `type`：`varchar(32)`，反馈类型编码，默认 `other`。
- `pics`：`varchar(1000)`，英文逗号分隔的图片 URL，最多 3 张。
- `handle_reason`：`varchar(500)`，后台处理备注。
- `handled_by`：`bigint(20)`，处理人 ID，当前若后台登录态无法稳定获取，则允许为空。
- `handled_at`：`datetime`，处理时间。

索引调整：

- 保留 `idx_feedback_userid`。
- 保留或重建 `idx_feedback_status_credate(status, credate)`。
- 新增 `idx_feedback_type_credate(type, credate)`，支持按类型筛选后按时间倒序查看。

数据库脚本采用新增迁移脚本，不直接改写旧脚本语义：

- 新增 `backend/dev/mysql/20260710_alter_feedback_chain.sql`。
- 新增对应回滚脚本 `backend/dev/mysql/20260710_alter_feedback_chain_rollback.sql`。
- 可同步更新 `20260628_create_feedback.sql` 作为全量新环境建表参考，但运维执行仍以新增迁移脚本为准。

## 小程序设计

页面：`frontend/src/pages-sub/user/feedback.vue`。

页面结构：

- 顶部保留 `KeepPageNav`。
- 第一块为反馈类型，使用 chip（选项标签）网格展示 6 个固定类型。
- 第二块为反馈内容，保留 10 到 500 字限制和字数计数。
- 第三块为反馈图片，最多 3 张，支持选择、上传中遮罩、删除、预览。
- 第四块为联系方式，继续选填，最大 100 字。
- 底部提交按钮在类型、内容、图片上传状态校验通过后可点击。

上传策略：

- 复用 `chooseImages` 和上传重试工具的交互模式。
- 反馈图片不使用带水印的作品上传语义；计划新增独立 `uploadFeedbackImage` 或后端新增 `/feedback/upload/image`，上传路径归属反馈业务。
- 图片上传成功后提交 URL 列表，提交字段使用 `pics`。
- 若图片上传失败，保留本地预览并提示重试或删除；提交时不允许存在上传失败或上传中的图片。

提交参数：

```ts
interface FeedbackAddParams {
  type: 'function' | 'experience' | 'account' | 'payment' | 'content' | 'other'
  content: string
  contact?: string
  pics?: string
}
```

## 后端设计

### 小程序侧入口

文件：`backend/system-wap/src/main/java/com/ypat/controller/FeedbackController.java`。

职责：

- 读取当前登录用户。
- 校验 `type`、`content`、`contact`、`pics`。
- 内容和联系方式继续做基础 XSS 字符替换。
- 图片字段最多 3 张，单条 URL 最大长度受总字段限制保护。
- 保留 60 秒限频。
- 组装 `FeedbackQo` 转发到 `SYSTEM-API`。

### 反馈图片上传

新增小程序反馈图片上传入口：

- `POST /feedback/upload/image`
- 请求字段：`file`
- 返回：`{ id?, url }`

实现优先复用现有存储服务和图片类型校验，不绑定作品媒体表，不加作品水印。若现有 `StorageBizPath` 没有反馈路径，则新增 `FEEDBACK` 业务路径；如该枚举不存在于当前基线，则按现有存储路径实现最小扩展。

### 内部服务入口

文件：`backend/system-restapi/src/main/java/com/ypat/controller/FeedbackController.java`。

新增接口：

- `GET /service/feedback/admin/list`
- `GET /service/feedback/admin/detail`
- `POST /service/feedback/admin/handle`

继续保留：

- `POST /service/feedback/add`

### 领域服务

文件：`backend/system-domain/src/main/java/com/ypat/service/FeedbackService.java`。

职责：

- `add`：保存新增字段，默认状态 `0`，默认类型 `other`。
- `adminList`：按状态、类型、用户 ID 分页查询，按 `credate` 倒序。
- `adminDetail`：返回单条反馈详情。
- `adminHandle`：仅允许处理待处理记录，写入状态、处理备注、处理时间、更新时间。

返回给后台的记录字段：

- `id`
- `userId`
- `userNickname`
- `type`
- `typeText`
- `content`
- `contact`
- `pics`
- `status`
- `statusText`
- `handleReason`
- `handledBy`
- `handledAt`
- `createdAt`
- `updatedAt`

昵称通过 `UserRepository` 查询，查询不到时只展示用户 ID。

## 管理后台设计

新增页面：`frontend-admin/src/views/manage/feedback-list/index.vue`。

新增接口模块：`frontend-admin/src/api/modules/feedback.ts`。

菜单：

- 分组：审核系统。
- 菜单名：意见反馈。
- 路径：`/manage/feedback/index`。
- 组件：`manage/feedback-list/index`。

列表筛选：

- 状态：全部、待处理、已处理、已忽略。
- 类型：全部、功能异常、体验建议、账号/资料、支付/订单、内容/用户举报、其他。
- 用户 ID。

列表字段：

- 反馈 ID。
- 用户。
- 类型。
- 内容摘要。
- 图片数量。
- 状态。
- 提交时间。
- 操作：详情、处理、忽略。

详情弹窗：

- 展示完整内容、联系方式、用户、类型、状态、提交时间。
- 图片宫格预览，支持 `el-image` 图片预览。
- 展示历史处理备注和处理时间。

处理弹窗：

- 状态选择：已处理或已忽略。
- 处理备注：选填，最多 500 字。
- 提交成功后刷新列表。

## 兼容性和安全

- 已有只传 `content`、`contact` 的旧调用仍可入库，类型按 `other` 保存，图片为空。
- 新增字段使用可空或默认值，避免迁移后旧数据无法读取。
- 后端校验是最终边界，小程序和后台校验只提升体验。
- 后台处理接口只允许管理端路径访问，复用现有后台鉴权链路。
- 图片上传沿用已有文件类型和大小校验，不接受任意 URL 作为上传结果。
- 图片 URL 存储使用后端上传返回值，提交接口只校验数量和长度，不重新下载图片。

## 测试策略

按用户要求，不做依赖下载和完整构建，重点做改动范围内的静态与单元级验证。

建议验证：

- 小程序反馈页测试：类型枚举、提交参数、图片上传状态、字数和联系方式限制。
- 小程序 API 契约测试：`addFeedback` 传递 `type`、`content`、`contact`、`pics`。
- wap 控制器测试：类型兼容、图片数量限制、限频、字段转发。
- domain 服务测试或源代码测试：新增字段保存、后台分页筛选、待处理状态处理。
- 管理后台类型检查或页面源测试：菜单注册、接口路径、状态/类型筛选、详情图片预览。
- `git diff --check origin/main...HEAD` 检查空白和格式风险。

## 交付顺序

1. 数据对象与数据库脚本：先补字段、Qo、实体和迁移脚本。
2. 后端提交与上传：补类型、图片字段和反馈图片上传。
3. 后台管理接口：补列表、详情、处理闭环。
4. 小程序页面：补类型选择、图片上传、提交参数。
5. 管理后台页面：补菜单、接口模块、列表、详情、处理弹窗。
6. 聚焦验证：跑与反馈链路直接相关的测试和格式检查。

## 完成标准

- 用户能在新版小程序意见反馈页选择类型、填写内容、上传最多 3 张图片并提交成功。
- 后端 `t_feedback` 能保存类型、图片、状态、处理备注和时间。
- 新版管理后台能看到反馈记录，按类型/状态/用户筛选，查看详情和图片，处理或忽略反馈。
- 旧版前端未被修改。
- 新增设计文档和后续计划文档均提交在 `codex/feedback-chain-upgrade` worktree（独立工作区）对应分支。
