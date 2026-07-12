# 实名认证审核费配置实施计划

> **执行要求：** 使用 executing-plans(执行计划) 技能在当前任务内逐项实施，不使用 subagent(子代理)。每一步使用复选框跟踪。

**目标：** 让新版实名认证支付费用可在后台“保证金配置”页面维护，测试阶段默认 0.01 元，并确保展示金额与实际下单金额一致。

**架构：** 复用 `t_deposit_config`(保证金配置表)和现有配置接口，新增 `realnameAuditFeeFen`(实名认证审核费分值)。新版后端下单读取该配置，新版小程序读取同一公开配置；旧版小程序及旧通用下单入口保持不变。

**技术栈：** Java 8、Spring Boot(Spring Boot 框架)、JPA(Java 持久化接口)、Vue 3(Vue 前端框架)、TypeScript(TypeScript 类型语言)、uni-app(跨端小程序框架)、Element Plus(Element Plus 组件库)、MySQL(MySQL 数据库)。

---

### 任务一：配置模型与默认值

**文件：**

- 修改：`backend/system-domain/src/test/java/com/ypat/service/DepositServiceTest.java`
- 修改：`backend/system-domain/src/main/java/com/ypat/entity/DepositConfig.java`
- 修改：`backend/system-object/src/main/java/com/ypat/DepositConfigQo.java`
- 修改：`backend/system-domain/src/main/java/com/ypat/service/DepositService.java`

- [ ] 在 `getConfigUsesSqlDefaultsWhenConfigMissing`(配置缺失时使用默认值测试)中断言 `realnameAuditFeeFen`(实名认证审核费分值)等于 1，并补充保存零值被拒绝的测试。
- [ ] 定向运行 `DepositServiceTest`(保证金配置服务测试)，确认新增测试因字段或校验缺失失败。
- [ ] 为实体和传输对象增加 `realnameAuditFeeFen`(实名认证审核费分值)字段及访问方法。
- [ ] 默认配置写入 1 分；读取与保存配置时校验该金额大于 0，无效时抛出 `FAIL_PAY_AMOUNT`(支付金额错误)。
- [ ] 再次定向运行该测试，确认通过。

### 任务二：新版实名认证下单读取配置

**文件：**

- 修改：`backend/system-wap/src/test/java/com/ypat/controller/RealnamePaymentV3SourceTest.java`
- 修改：`backend/system-wap/src/main/java/com/ypat/controller/RealnamePaymentController.java`

- [ ] 将源代码约束测试改为要求注入 `DepositServiceClient`(保证金配置服务客户端)、读取 `realnameAuditFeeFen`(实名认证审核费分值)，并禁止 `REALNAME_AUDIT_FEE_FEN = 2900`(实名认证费用硬编码)。
- [ ] 定向运行 `RealnamePaymentV3SourceTest`(实名认证支付源代码测试)，确认因仍存在硬编码而失败。
- [ ] 下单时调用一次配置接口并校验费用，将局部变量 `realnameAuditFeeFen`(实名认证审核费分值)用于业务订单、统一支付订单和微信支付指令。
- [ ] 再次定向运行该测试，确认通过。

### 任务三：数据库脚本

**文件：**

- 修改：`docs/sql/pending/V_wechat_pay_v3_deposit_member.sql`
- 新增：`docs/sql/pending/V_wechat_pay_v3_realname_fee_config.sql`

- [ ] 在首次建表脚本的 `t_deposit_config`(保证金配置表)中加入非空字段，默认值为 1。
- [ ] 新增增量脚本，通过 `information_schema`(数据库元数据表)检查字段是否存在，仅在缺失时执行 `ALTER TABLE`(修改表结构)，保证重复执行不覆盖已配置金额。
- [ ] 使用文本检查确认两个脚本字段名、默认值和注释一致。

### 任务四：新版后台配置页面

**文件：**

- 修改：`frontend-admin/src/api/types.ts`
- 修改：`frontend-admin/src/views/deposit/config/index.vue`
- 修改：`frontend-admin/src/api/__tests__/deposit-payment.test.ts`

- [ ] 在接口测试保存参数中加入 `realnameAuditFeeFen: 1`(实名认证审核费 1 分)，作为字段传输约束。
- [ ] 定向运行后台接口测试，记录当前测试对字段还没有类型与页面约束。
- [ ] 在 `DepositConfig`(保证金配置类型)增加必填字段；配置页面新增元单位状态，加载时分转元、保存时元转分。
- [ ] 在保证金配置表单加入“实名认证审核费”输入框，最小 0.01 元、两位小数，默认 0.01 元。
- [ ] 对该页面和接口文件执行定向 ESLint(代码规范检查)或 TypeScript(TypeScript 类型检查)；不执行构建。

### 任务五：新版小程序动态展示并阻止未知金额支付

**文件：**

- 修改：`frontend/src/api/types/index.ts`
- 修改：`frontend/src/api/modules/oauth.ts`
- 修改：`frontend/src/pages-sub/user/__tests__/realname-source.test.ts`
- 修改：`frontend/src/pages-sub/user/realname.vue`

- [ ] 在源代码测试中要求调用 `getDepositConfig`(获取保证金配置)、使用动态费用文案、费用缺失时提示并阻止支付，同时禁止 29 元和 `REALNAME_AUDIT_FEE_YUAN`(实名认证审核费元值常量)。
- [ ] 定向运行 `realname-source.test.ts`(实名认证页面源代码测试)，确认因动态费用尚未实现而失败。
- [ ] 在小程序 `DepositConfig`(保证金配置类型)增加 `realnameAuditFeeFen`(实名认证审核费分值)，删除认证接口中的 29 元常量。
- [ ] 页面初次加载时并行读取认证详情和费用配置；使用分转元格式化结果渲染页面说明与确认弹窗。
- [ ] 当费用缺失、非整数或小于 1 分时提示“认证费用加载失败，请稍后重试”并停止创建订单；重新提交免支付流程不受影响。
- [ ] 再次定向运行该测试，确认通过。

### 任务六：差异复核与针对性验证

**文件：**

- 复核：上述全部文件

- [ ] 搜索新版小程序和新版实名认证支付控制器，确认不再存在 29 元或 2900 分硬编码。
- [ ] 搜索 `realnameAuditFeeFen`(实名认证审核费分值)及数据库字段，确认模型、接口、页面和脚本命名一致。
- [ ] 运行无需下载依赖的定向前端测试、后端测试或静态检查；若现有环境无法运行，记录具体原因，不改为完整构建。
- [ ] 检查 `git diff --check`(Git 差异格式检查)和最终差异，确认没有旧版前端或需求外逻辑改动。

