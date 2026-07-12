# 后台审核效率优化实施计划

> **面向执行代理：** 必须使用 superpowers:executing-plans(按计划执行)逐项完成；每项使用复选框跟踪，严格执行 TDD(测试驱动开发)。

**目标：** 为后台实名列表补齐准确的提交日期、待审核优先排序和支付订单详情，同时统一订单金额与支付业务文案，并确保约拍审核弹窗加载完整图片详情。

**架构：** 在 `t_user`(用户表)增加最近实名认证提交时间，由实名提交事务写入；后台列表在服务端完成日期过滤和全局排序。管理端复用现有支付订单、实名详情和约拍详情接口，不新增重复接口，只补齐类型、加载状态与展示逻辑。

**技术栈：** Java 8、Spring Data JPA(数据持久层)、MySQL(关系型数据库)、Vue 3(前端框架)、TypeScript(类型脚本)、Element Plus(后台组件库)、JUnit(后端测试)、Vitest(前端测试)。

后端 Maven(构建工具)命令均以 `backend`(后端目录)作为工作目录；管理端命令均以 `frontend-admin`(后台前端目录)作为工作目录。

---

## 文件结构

- 新增 `docs/sql/pending/V_realname_submit_time.sql`：实名提交时间字段与组合索引迁移。
- 修改 `backend/system-domain/src/main/java/com/ypat/entity/User.java`：映射实名提交时间。
- 修改 `backend/system-object/src/main/java/com/ypat/UserQo.java`：跨服务传递实名提交时间与日期条件。
- 修改 `backend/system-domain/src/main/java/com/ypat/service/UserService.java`：提交时间写入、日期过滤和待审核优先排序。
- 修改 `backend/system-wap/src/main/java/com/ypat/controller/AdminUserController.java`：接收并严格解析提交日期。
- 修改后端相关测试：覆盖写入、迁移、过滤和排序契约。
- 修改 `frontend-admin/src/api/modules/user.ts`：补齐实名提交时间与查询类型。
- 修改 `frontend-admin/src/views/manage/user-list/index.vue`：默认日期、提交时间列、详情与审核入口。
- 修改 `frontend-admin/src/views/manage/user-list/UserAuditDialog.vue`：加载并展示当前用户支付订单。
- 修改 `frontend-admin/src/views/manage/user-list/__tests__/UserAuditDialog.test.ts`：覆盖订单与审核按钮状态。
- 新增 `frontend-admin/src/views/manage/user-list/__tests__/index.test.ts`：覆盖默认日期和操作入口。
- 修改 `frontend-admin/src/views/order/list/index.vue`：旧订单金额分转元。
- 修改 `frontend-admin/src/views/payment/order/index.vue`：业务中文加英文码。
- 新增对应源码测试：锁定金额和业务展示契约。
- 修改 `frontend-admin/src/views/manage/ypat-list/AuditDialog.vue`：打开时加载完整详情并优化局部状态。
- 新增约拍审核弹窗测试：覆盖详情图片优先和失败回退。

---

### 任务 1：实名提交时间数据链路

**文件：**

- 新增：`docs/sql/pending/V_realname_submit_time.sql`
- 修改：`backend/system-domain/src/main/java/com/ypat/entity/User.java`
- 修改：`backend/system-object/src/main/java/com/ypat/UserQo.java`
- 修改：`backend/system-domain/src/main/java/com/ypat/service/UserService.java`
- 修改：`backend/system-wap/src/main/java/com/ypat/controller/AdminUserController.java`
- 测试：`backend/system-domain/src/test/java/com/ypat/service/UserServiceRealnameSecuritySourceTest.java`
- 测试：`backend/system-wap/src/test/java/com/ypat/controller/AdminUserAuditSourceTest.java`

- [ ] **步骤 1：先补实名提交时间失败测试**

在 `UserServiceRealnameSecuritySourceTest`(实名安全测试)中断言成功提交后存在时间：

```java
assertNotNull(capture.savedUser.getRealnameSubmitAt());
```

并新增源码契约断言：

```java
assertTrue(userService.contains("old.setRealnameSubmitAt(new Date())"));
assertTrue(userService.contains("queryQo.getRealnameSubmitAt()"));
assertTrue(userService.contains("criteriaBuilder.selectCase()"));
```

在 `AdminUserAuditSourceTest`(后台实名控制器测试)中断言控制器使用严格日期解析：

```java
assertTrue(source.contains("realnameSubmitDate"));
assertTrue(source.contains("setLenient(false)"));
assertTrue(source.contains("throw new SysException(ResponseCode.FAIL_PARA"));
```

- [ ] **步骤 2：运行测试并确认因功能缺失失败**

```bash
mvn -pl system-domain -Dtest=UserServiceRealnameSecuritySourceTest test
mvn -pl system-wap -Dtest=AdminUserAuditSourceTest test
```

预期：前者因 `getRealnameSubmitAt`(获取实名提交时间)不存在而编译失败，后者因控制器缺少日期参数断言失败。

- [ ] **步骤 3：实现数据库字段和实体映射**

迁移脚本通过 `information_schema`(数据库元数据)分别守卫字段和索引，核心 DDL(数据定义语句)为：

```sql
ALTER TABLE `t_user`
  ADD COLUMN `realname_submit_at` DATETIME NULL COMMENT '实名认证最近提交时间';

ALTER TABLE `t_user`
  ADD INDEX `idx_user_realname_review` (`status`, `realname_submit_at`, `id`);
```

在 `User`(用户实体)新增映射字段：

```java
@Column(name = "realname_submit_at")
@Temporal(TemporalType.TIMESTAMP)
private Date realnameSubmitAt;
```

在 `UserQo`(用户传输对象)新增不带持久化注解的普通字段：

```java
private Date realnameSubmitAt;
```

两个类型均增加标准 getter/setter(读取器/写入器)。

- [ ] **步骤 4：实现提交写入、日期过滤和全局排序**

在 `UserService.oauth`(实名认证提交服务)保存用户前增加：

```java
old.setRealnameSubmitAt(new Date());
```

在查询规格中增加当天过滤：

```java
if (CommonUtils.isNotNull(queryQo.getRealnameSubmitAt())) {
    Date startDay = TimeUtil.getStartDay(queryQo.getRealnameSubmitAt());
    Date endDay = TimeUtil.getEndDay(queryQo.getRealnameSubmitAt());
    predicatesList.add(criteriaBuilder.between(root.get("realnameSubmitAt"), startDay, endDay));
}
```

使用 Criteria API(条件查询接口)设置服务端顺序：待审核权重升序、提交时间倒序、编号倒序。`PageRequest`(分页请求)不再附加会覆盖条件排序的 ID 排序。

控制器新增 `realnameSubmitDate` 参数，使用 `yyyy-MM-dd`、`setLenient(false)` 严格解析；解析失败抛出参数错误，不静默忽略。

- [ ] **步骤 5：运行后端定向测试**

运行步骤 2 的两条命令。预期：全部通过。

- [ ] **步骤 6：提交实名时间数据链路**

```bash
git add docs/sql/pending/V_realname_submit_time.sql backend/system-domain backend/system-object backend/system-wap
git commit -m "feat: track realname submission time"
```

---

### 任务 2：实名列表和充值订单详情

**文件：**

- 修改：`frontend-admin/src/api/modules/user.ts`
- 修改：`frontend-admin/src/views/manage/user-list/index.vue`
- 修改：`frontend-admin/src/views/manage/user-list/UserAuditDialog.vue`
- 修改：`frontend-admin/src/views/manage/user-list/__tests__/UserAuditDialog.test.ts`
- 新增：`frontend-admin/src/views/manage/user-list/__tests__/index.test.ts`

- [ ] **步骤 1：先补实名列表失败测试**

源码测试断言：

```ts
expect(source).toContain('realnameSubmitDate: getTodayText()')
expect(source).toContain('label="提交日期"')
expect(source).toContain('prop="realnameSubmitAt"')
expect(source).toContain('v-if="row.status === \'1\'"')
expect(source).toContain('详情')
```

弹窗组件测试补充 `getPaymentOrders`(查询支付订单)模拟，并断言请求参数：

```ts
expect(getPaymentOrdersMock).toHaveBeenCalledWith({ userId: 12, page: 0, size: 10 })
expect(wrapper.text()).toContain('充值订单')
expect(wrapper.text()).toContain('实名认证(REALNAME)')
expect(wrapper.text()).toContain('¥29.00')
```

再将详情状态设为 `'2'`，断言弹窗和列表不显示审核操作但仍显示详情。

- [ ] **步骤 2：运行前端测试并确认预期失败**

```bash
pnpm exec vitest run src/views/manage/user-list/__tests__/index.test.ts src/views/manage/user-list/__tests__/UserAuditDialog.test.ts
```

预期：因默认日期、详情入口和订单区域不存在而失败。

- [ ] **步骤 3：实现列表默认日期和操作入口**

新增本地日期函数，避免 UTC(协调世界时)跨日：

```ts
function getTodayText(): string {
  const now = new Date()
  const offset = now.getTimezoneOffset() * 60_000
  return new Date(now.getTime() - offset).toISOString().slice(0, 10)
}
```

查询初始值与重置值均设置 `realnameSubmitDate: getTodayText()`；日期选择器使用 `value-format="YYYY-MM-DD"`。操作列始终提供详情，仅待审核状态提供审核，两个入口都复用 `openUserDialog`(打开用户弹窗)。

- [ ] **步骤 4：实现弹窗支付订单区域**

复用支付模块：

```ts
const orders = ref<PaymentOrder[]>([])
const orderLoading = ref(false)
const orderLoadFailed = ref(false)

const [detailResult, orderResult] = await Promise.allSettled([
  getUserDetail(userId),
  getPaymentOrders({ userId, page: 0, size: 10 }),
])
```

详情成功时更新实名资料；订单成功时写入列表，失败时只设置订单区失败状态。业务映射覆盖四类，金额使用 `(amountFen / 100).toFixed(2)`。弹窗宽度使用响应式约束，订单表在弹窗内容区内横向可读，不嵌套卡片。

- [ ] **步骤 5：运行实名管理测试**

运行步骤 2 命令。预期：全部通过。

- [ ] **步骤 6：提交实名管理界面改动**

```bash
git add frontend-admin/src/api/modules/user.ts frontend-admin/src/views/manage/user-list
git commit -m "feat: improve admin realname review workflow"
```

---

### 任务 3：订单金额与支付业务文案

**文件：**

- 修改：`frontend-admin/src/views/order/list/index.vue`
- 修改：`frontend-admin/src/views/payment/order/index.vue`
- 新增：`frontend-admin/src/views/order/list/__tests__/index.test.ts`
- 新增：`frontend-admin/src/views/payment/order/__tests__/index.test.ts`

- [ ] **步骤 1：先补格式化失败测试**

订单测试断言：

```ts
expect(source).toContain('function fenText')
expect(source).toContain('fenText(row.total_fee)')
expect(source).not.toContain('prop="total_fee" label="金额"')
```

支付流水测试断言：

```ts
expect(source).toContain("DEPOSIT: '保证金'")
expect(source).toContain("MEMBER: '会员'")
expect(source).toContain("PPD: '拍拍豆'")
expect(source).toContain("REALNAME: '实名认证'")
expect(source).toContain('businessText(row.businessType)')
```

- [ ] **步骤 2：运行测试并确认预期失败**

```bash
pnpm exec vitest run src/views/order/list/__tests__/index.test.ts src/views/payment/order/__tests__/index.test.ts
```

预期：两个页面均因缺少目标格式化调用而失败。

- [ ] **步骤 3：实现最小展示格式化**

两个页面统一采用：

```ts
function fenText(value?: number): string {
  return Number.isFinite(value) ? `¥${(Number(value) / 100).toFixed(2)}` : '-'
}
```

支付业务采用：

```ts
const businessNameMap: Record<string, string> = {
  DEPOSIT: '保证金',
  MEMBER: '会员',
  PPD: '拍拍豆',
  REALNAME: '实名认证',
}

function businessText(value?: string): string {
  if (!value) return '-'
  const name = businessNameMap[value]
  return name ? `${name}(${value})` : value
}
```

业务筛选下拉同步补齐四类选项。

- [ ] **步骤 4：运行订单展示测试**

运行步骤 2 命令。预期：全部通过。

- [ ] **步骤 5：提交订单展示改动**

```bash
git add frontend-admin/src/views/order/list frontend-admin/src/views/payment/order
git commit -m "fix: normalize admin order displays"
```

---

### 任务 4：约拍审核详情图片与布局

**文件：**

- 修改：`frontend-admin/src/views/manage/ypat-list/AuditDialog.vue`
- 新增：`frontend-admin/src/views/manage/ypat-list/__tests__/AuditDialog.test.ts`

- [ ] **步骤 1：先补详情加载失败测试**

模拟 `getYpatDetail`(查询约拍详情)返回包含两张图片的完整数据，断言：

```ts
expect(getYpatDetailMock).toHaveBeenCalledWith(21)
expect(wrapper.findAllComponents({ name: 'ElImage' })).toHaveLength(2)
expect(wrapper.text()).toContain('完整详情描述')
```

再让详情接口拒绝，断言弹窗仍展示列表行描述和图片，证明失败回退有效。

- [ ] **步骤 2：运行测试并确认预期失败**

```bash
pnpm exec vitest run src/views/manage/ypat-list/__tests__/AuditDialog.test.ts
```

预期：因弹窗未调用详情接口而失败。

- [ ] **步骤 3：实现详情优先与失败回退**

新增：

```ts
const detail = ref<YpatInfo | null>(null)
const detailLoading = ref(false)
const displayData = computed(() => detail.value || props.data)
```

监听弹窗与编号变化，打开时先以列表行作为回退，再请求详情；请求成功且弹窗仍对应同一编号时更新详情。模板所有字段、图片、理由初值均读取 `displayData`(展示详情)，并在内容区应用局部加载态。

布局保持现有顶部资料、紧凑字段、描述、图片和审核区结构；将图片网格设置稳定最小宽度和 `aspect-ratio`(宽高比)，弹窗宽度增加 `max-width`(最大宽度)约束，确保窄屏不溢出。

- [ ] **步骤 4：运行约拍审核测试**

运行步骤 2 命令。预期：全部通过。

- [ ] **步骤 5：提交约拍审核弹窗改动**

```bash
git add frontend-admin/src/views/manage/ypat-list/AuditDialog.vue frontend-admin/src/views/manage/ypat-list/__tests__/AuditDialog.test.ts
git commit -m "fix: load complete ypat audit details"
```

---

### 任务 5：定向回归验证

**文件：** 本计划涉及的全部文件。

- [ ] **步骤 1：运行全部相关前端测试**

```bash
pnpm exec vitest run \
  src/views/manage/user-list/__tests__/index.test.ts \
  src/views/manage/user-list/__tests__/UserAuditDialog.test.ts \
  src/views/order/list/__tests__/index.test.ts \
  src/views/payment/order/__tests__/index.test.ts \
  src/views/manage/ypat-list/__tests__/AuditDialog.test.ts
```

预期：全部通过。

- [ ] **步骤 2：运行相关文件代码规范检查**

```bash
pnpm exec eslint \
  src/api/modules/user.ts \
  src/views/manage/user-list/index.vue \
  src/views/manage/user-list/UserAuditDialog.vue \
  src/views/manage/user-list/__tests__/index.test.ts \
  src/views/manage/user-list/__tests__/UserAuditDialog.test.ts \
  src/views/order/list/index.vue \
  src/views/order/list/__tests__/index.test.ts \
  src/views/payment/order/index.vue \
  src/views/payment/order/__tests__/index.test.ts \
  src/views/manage/ypat-list/AuditDialog.vue \
  src/views/manage/ypat-list/__tests__/AuditDialog.test.ts
```

预期：退出码为 0。

- [ ] **步骤 3：运行后端相关测试和差异检查**

```bash
mvn -pl system-domain -Dtest=UserServiceRealnameSecuritySourceTest test
mvn -pl system-wap -Dtest=AdminUserAuditSourceTest test
git diff --check
```

预期：测试通过，差异检查没有输出。按用户要求不执行依赖下载和完整构建。
