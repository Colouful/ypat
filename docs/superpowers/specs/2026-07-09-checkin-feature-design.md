# 签到功能完整链路设计

## 背景

在 `frontend/src/pages/mine/index.vue` 的我的页顶部增加每日签到入口。用户未签到时展示主题色签到 icon(图标)，点击后居中展示 confirm(确认弹窗)，确认签到后获得拍豆奖励。签到成功后需要同步用户拍豆余额、拍豆获取记录，并在后台管理系统提供签到规则维护和签到记录查询能力。

本设计采用独立签到表加复用拍豆流水的方案，避免把签到业务状态混入现有 `t_record` 拍豆流水表，同时保持钱包、拍豆记录和后台追踪链路一致。

## 目标

- 小程序我的页新增签到入口，位置在 `.mine-top__icon` 右侧一点。
- 未签到展示主题色签到 icon(图标)，已签到展示灰色签到 icon(图标)和“已签到”文案。
- 点击签到入口展示居中的 confirm(确认弹窗)，确认后执行签到。
- 签到成功获得后台配置的拍豆数，默认每天 1 拍豆。
- 签到成功同步更新 `t_user.ppd` 用户拍豆余额和 `t_record` 拍豆流水。
- 后台管理系统支持维护签到规则，并查询签到记录。
- 每日签到边界固定使用 `Asia/Shanghai`(北京时间)。

## 非目标

- 不做连续签到、补签、撤销签到、人工补发拍豆。
- 不把钱包页所有静态“获得拍豆”任务改成通用任务系统。
- 不改造现有支付充值、邀请奖励、发布消耗等拍豆链路。
- 不引入客户端传 `userid` 代签能力。

## 总体架构

小程序端新增签到状态和执行签到 API(接口)调用。`pages/mine/index.vue` 在 `onShow`(页面显示)时拉取今日签到状态，点击签到入口后调用签到接口。签到成功后刷新 `userStore.updateUserInfo()`，让我的页和钱包页展示最新拍豆余额。

后端新增签到领域能力，建议放在 `backend/system-domain` 的 `CheckinService`(签到服务)中集中处理状态查询、规则查询、签到事务和后台查询。`backend/system-restapi` 提供服务间接口，`backend/system-wap` 暴露小程序接口和管理端接口。

后台管理端在 `frontend-admin` 新增“签到管理”菜单，页面包含规则维护表单和签到记录查询表格。规则维护只影响后续签到；历史签到记录保存当次奖励拍豆数。

拍豆流水继续使用现有 `t_record`，新增 `RecordType.CHECKIN = "6"`，文案为“每日签到”。钱包页和拍豆记录页通过枚举映射展示“每日签到 +X”。

## 数据模型

### `t_checkin_rule`

签到规则表只保留单条全局规则。

字段：

- `id`: 主键。
- `enabled`: 是否启用，建议使用现有 `YesNo`(是否枚举)风格，`1` 启用、`0` 关闭。
- `reward_ppd`: 每日奖励拍豆数，默认 `1`。
- `confirm_title`: confirm(确认弹窗)标题，默认“每日签到”。
- `confirm_content`: confirm(确认弹窗)内容，默认“签到成功可获得 1 拍豆”。
- `created_at`: 创建时间。
- `updated_at`: 更新时间。

约束：

- `reward_ppd >= 0`，后台表单和后端都校验。
- 标题、内容限制长度，避免后台误填导致小程序弹窗过长。

### `t_checkin_record`

签到记录表记录每个用户每天一次签到。

字段：

- `id`: 主键。
- `userid`: 用户 ID。
- `checkin_date`: 签到日期，按 `Asia/Shanghai`(北京时间)生成。
- `reward_ppd`: 当次奖励拍豆数。
- `record_id`: 对应 `t_record.id` 拍豆流水 ID，事务内先为空，写入拍豆流水后回填。
- `created_at`: 创建时间。

约束：

- 唯一索引：`uniq_user_checkin_date(userid, checkin_date)`。
- 普通索引：`idx_checkin_date(checkin_date)`、`idx_checkin_userid(userid)`。

### 拍豆流水

`RecordType`(记录类型)新增：

- 后端：`CHECKIN("6", "每日签到")`。
- 前端：`RecordType.CHECKIN = "6"`，`RECORD_TYPE_LABELS["6"] = "每日签到"`。

签到成功时写入 `t_record`：

- `userid`: 当前登录用户。
- `type`: `"6"`。
- `ppd`: 当次 `reward_ppd`。
- `credate`: 当前时间。

## 小程序接口

### `GET /checkin/today`

获取当前登录用户今日签到状态。

返回字段：

- `enabled`: 是否启用签到规则。
- `checkedIn`: 今日是否已签到。
- `rewardPpd`: 当前规则奖励拍豆数。
- `confirmTitle`: confirm(确认弹窗)标题。
- `confirmContent`: confirm(确认弹窗)内容。
- `checkinDate`: 按 `Asia/Shanghai`(北京时间)计算的日期。

如果未登录，沿用现有鉴权拦截。前端点击入口前也使用 `requireLogin()` 做登录跳转。

### `POST /checkin/do`

执行当前登录用户今日签到。

返回字段：

- `checkedIn`: true。
- `rewardPpd`: 本次实际新增拍豆数；重复签到返回 `0`。
- `currentPpd`: 当前用户拍豆余额。
- `recordId`: 本次签到对应的 `t_record.id`；重复签到可为空。
- `message`: “签到成功”或“今日已签到”。

重复签到不返回红色错误态，返回成功结构并提示“今日已签到”。真正异常，如规则关闭、未登录、数据库失败，按现有错误响应处理。

## 后台接口

### `GET /admin/checkin/rule`

获取签到规则。若数据库没有规则，服务端返回默认规则，也可在初始化脚本插入默认数据。

### `PUT /admin/checkin/rule`

保存签到规则。校验：

- `rewardPpd` 不能为空且大于等于 0。
- `confirmTitle`、`confirmContent` 不能为空且长度受限。
- `enabled` 必须是合法值。

### `GET /admin/checkin/records`

分页查询签到记录。

查询条件：

- `userid`: 用户 ID。
- `mobile`: 用户手机号。
- `dateFrom`: 开始日期。
- `dateTo`: 结束日期。
- `page`、`size`: 分页参数。

列表展示：

- 记录 ID。
- 用户 ID。
- 用户昵称。
- 手机号。
- 签到日期。
- 奖励拍豆数。
- 拍豆流水 ID。
- 创建时间。

## 前端交互

### 我的页

在 `.mine-top__icon` 右侧新增签到入口。建议将原顶部左侧结构调整为 `mine-top__left`，内部包含菜单按钮和签到按钮，保持右侧区域布局不受影响。

状态：

- 未登录：点击签到入口走 `requireLogin()`，跳转登录。
- 已登录未签到：显示主题色签到 icon(图标)。
- 已登录已签到：显示灰色签到 icon(图标)和“已签到”。
- 规则关闭：隐藏签到入口。
- 提交中：禁用重复点击，避免连续触发。

交互：

1. `onShow`(页面显示)调用 `GET /checkin/today`。
2. 点击签到入口展示 `uni.showModal` confirm(确认弹窗)。
3. 用户点击确认后调用 `POST /checkin/do`。
4. 签到成功后更新本地状态为已签到。
5. 调用 `userStore.updateUserInfo()` 刷新拍豆余额。
6. 用 `uni.showToast` 展示“签到成功，获得 X 拍豆”。

异常：

- 网络失败：提示“签到失败，请稍后重试”，不改变本地签到状态。
- 今日已签到：置为已签到并提示“今日已签到”。
- 规则关闭：隐藏入口或提示“签到活动暂未开启”，推荐隐藏。

### 钱包页

`frontend/src/pages-sub/user/wallet.vue` 里的“每日签到”从静态已完成改成读取同一签到状态。

- 未签到：按钮显示“去签到”，触发签到弹窗或跳回我的页签到。
- 已签到：按钮显示“已签到”并禁用。
- 记录页通过新增 `RecordType.CHECKIN` 自动展示“每日签到”流水。

## 后端事务

`CheckinService.doCheckin`(执行签到)在单个事务中完成：

1. 获取当前用户 ID。
2. 读取签到规则；规则关闭则返回关闭状态。
3. 按 `Asia/Shanghai`(北京时间)计算 `checkinDate`。
4. 查询 `t_checkin_record` 是否已有 `userid + checkin_date`。
5. 已存在则返回“今日已签到”，不加豆。
6. 未存在则先创建 `CheckinRecord` 签到记录，`record_id` 暂为空，并触发唯一约束校验。
7. 创建 `Record` 拍豆流水。
8. 更新 `User.ppd = oldPpd + rewardPpd`。
9. 回填 `CheckinRecord.record_id`。
10. 返回当前余额和记录 ID。

并发安全由唯一索引兜底。若插入 `t_checkin_record` 发生唯一约束冲突，服务端捕获后按“今日已签到”返回，不重复加豆。先创建签到记录占位，再写流水和回填 `record_id`，可以避免并发冲突时已经写入拍豆流水。整个流程仍在同一事务内，任一步失败都会整体回滚。

## 管理后台页面

新增菜单建议放在“运营系统”或“查询系统”下。当前菜单已有审核、查询、订单、保证金、会员、内测数据等分组；为了不扩大菜单结构，可先放在“查询系统”下，标题为“签到管理”。

页面结构：

- 上半部分：签到规则表单，包含启用开关、每日奖励拍豆数、弹窗标题、弹窗内容、保存按钮。
- 下半部分：签到记录查询，包含用户 ID、手机号、日期范围、查询/重置按钮。
- 表格字段：用户 ID、昵称、手机号、签到日期、奖励拍豆、拍豆流水 ID、创建时间。
- 分页沿用现有 Element Plus(后台组件库)表格分页风格。

## SQL 脚本

按项目现有习惯新增 SQL(数据库脚本)到 `backend/dev/mysql`，并视发布流程同步放入 `docs/sql/pending`。

脚本包含：

- 创建 `t_checkin_rule`。
- 创建 `t_checkin_record`。
- 插入默认签到规则。
- 创建唯一索引和查询索引。

开发环境 `system-restapi` 和 `system-wap` 当前存在 `ddl-auto: update`，但生产和发布仍应依赖显式 SQL(数据库脚本)，避免隐式建表遗漏索引和默认数据。

## 验证策略

按用户要求，不下载依赖、不启动完整构建，由用户手动运行体验验证。实现阶段只做改动相关的轻量检查：

- 后端：新增服务方法的源码级检查，必要时补 `CheckinService` 单元测试，覆盖首次签到、重复签到、规则关闭、奖励为 0。
- 前端：补源码契约测试，确认我的页包含签到入口、钱包页每日签到不再静态已完成、枚举包含“每日签到”。
- 管理后台：补 API(接口)模块或页面源码契约测试，确认菜单和接口路径存在。
- SQL(数据库脚本)：检查表名、索引名、默认数据和字段命名一致。

## 风险和处理

- **重复发豆风险**：用 `userid + checkin_date` 唯一索引和前端提交锁双重防护。
- **余额和记录不一致**：签到记录、用户余额、拍豆流水放在同一事务。
- **运营关闭后用户误点**：前端按 `enabled` 隐藏入口，服务端再次校验规则。
- **时区不一致**：服务端统一用 `Asia/Shanghai`(北京时间)，前端只展示服务端返回状态。
- **历史奖励被规则变更影响**：签到记录保存当次 `reward_ppd`，历史数据不重算。

## 实施顺序

1. 新增后端 Qo(数据传输对象)、entity(实体)、repository(仓储)、service(服务)和枚举。
2. 新增小程序端 `/checkin/today`、`/checkin/do` 接口。
3. 新增后台端 `/admin/checkin/rule`、`/admin/checkin/records` 接口。
4. 补 SQL(数据库脚本)。
5. 接入我的页签到入口和弹窗。
6. 接入钱包页每日签到任务和拍豆记录枚举。
7. 新增后台签到管理页面和菜单。
8. 做轻量源码契约测试和相关单元测试。
