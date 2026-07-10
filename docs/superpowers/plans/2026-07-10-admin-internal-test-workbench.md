# 内测数据工作台懒人版升级 Implementation Plan(实施计划)

> **For agentic workers(给代理执行者):** REQUIRED SUB-SKILL(必需子技能): Use `superpowers:subagent-driven-development`(子代理驱动开发，推荐) or `superpowers:executing-plans`(执行计划) to implement this plan task-by-task(逐任务执行). Steps use checkbox(`- [ ]`) syntax for tracking(用复选框跟踪).

**Goal(目标):** 将新版管理后台内测数据模块升级为懒人版工作台，支持批量资源导入、作品组生成、资源占用、三类清晰生成入口、内测业务列表筛选和内测专属一键会员/认证/保证金。

**Architecture(架构):** 在现有 `t_internal_test_resource`(内测资源表) 和 `data_flag`(数据标识) 基础上扩展，不重建一套平行系统。后端继续沿用 `system-wap` -> `system-restapi` -> `system-domain` 分层，前端继续沿用 `frontend-admin` 的 Vue(前端框架) + TypeScript(类型脚本) + Element Plus(组件库) 页面模式。

**Tech Stack(技术栈):** Java 8、Spring Boot 1.5、Spring Data JPA、Feign(声明式 HTTP 客户端)、MySQL、Vue 3、TypeScript、Element Plus、Vitest(前端测试框架)、Maven(构建工具)。

---

## Spec Source(规格来源)

- Design(设计): `docs/superpowers/specs/2026-07-10-admin-internal-test-workbench-design.md`
- Existing base design(既有基础设计): `docs/superpowers/specs/2026-07-06-admin-internal-test-data-design.md`
- Workspace(工作区): `/Users/lizhenwei/workspace/vueworkspace/ypat-workspace`

## Scope Check(范围检查)

该规格跨后端数据、后端接口、前端页面和业务列表增强，但它们围绕同一个内测数据工作台闭环：资源导入 -> 资源占用 -> 内测数据生成 -> 管理列表识别 -> 内测专属治理。拆成多个独立规格会导致资源占用和清理逻辑难以验收，因此本计划保持为一个实施计划，并按垂直切片拆任务。

## File Structure(文件结构)

Database(数据库) 与文档：

- Modify: `docs/sql/pending/V_admin_internal_test_data.sql` - 补充资源作品组、占用状态、三级城市字段。

Backend object(后端传输对象)：

- Modify: `backend/system-object/src/main/java/com/ypat/InternalTestResourceQo.java` - 增加批量导入、作品组和占用字段。
- Modify: `backend/system-object/src/main/java/com/ypat/InternalTestGenerateQo.java` - 增加三类生成动作需要的字段。
- Create: `backend/system-object/src/main/java/com/ypat/InternalTestUserActionQo.java` - 一键会员、认证、保证金动作参数。
- Modify: `backend/system-object/src/main/java/com/ypat/UserQo.java` - 支持 `dataFlag` 查询。
- Modify: `backend/system-object/src/main/java/com/ypat/YpatInfoQo.java` - 支持 `dataFlag` 查询。
- Modify: `backend/system-object/src/main/java/com/ypat/WorkListQo.java` - 支持 `dataFlag` 查询。

Backend domain(后端领域层)：

- Modify: `backend/system-domain/src/main/java/com/ypat/entity/InternalTestResource.java` - 增加作品组、占用、三级城市字段。
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java` - 增加作品组、占用查询和 URL(资源地址) 精确查重。
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/UserRepository.java` - 增加内测用户搜索和真实/内测筛选。
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/DepositOrderRepository.java` - 增加内测保证金订单幂等查询。
- Modify: `backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java` - 支持批量导入、作品组查询、资源占用释放。
- Modify: `backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java` - 拆分新增用户、新增作品、新增约拍、清理释放资源。
- Create: `backend/system-domain/src/main/java/com/ypat/service/InternalTestUserActionService.java` - 内测用户一键会员、认证、保证金。
- Modify: `backend/system-domain/src/main/java/com/ypat/service/UserService.java` - 用户分页支持 `dataFlag` 筛选。
- Modify: `backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java` - 约拍分页支持 `dataFlag` 筛选。
- Modify: `backend/system-domain/src/main/java/com/ypat/service/WorkService.java` - 后台作品分页支持 `dataFlag` 筛选。
- Modify: `backend/system-domain/src/main/java/com/ypat/service/DepositService.java` - 支持内测保证金已支付记录。
- Modify: `backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java` - 后端源代码契约测试。

Backend restapi/wap(后端服务接口/管理端接口)：

- Modify: `backend/system-restapi/src/main/java/com/ypat/controller/InternalTestController.java` - 暴露新资源、生成、动作接口。
- Modify: `backend/system-wap/src/main/java/com/ypat/service/InternalTestServiceClient.java` - Feign(声明式 HTTP 客户端) 增加新接口。
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/AdminInternalTestController.java` - 管理端新接口包装。
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/AdminUserController.java` - 用户列表接收 `dataFlag`。
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java` - 约拍列表接收 `dataFlag`。
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/AdminWorkController.java` - 作品列表接收 `dataFlag`。
- Modify: `backend/system-wap/src/test/java/com/ypat/controller/AdminInternalTestControllerSourceTest.java` - 管理端接口源代码契约测试。

Frontend admin API(管理后台前端接口)：

- Modify: `frontend-admin/src/api/modules/internal-test.ts` - 增加批量导入、资源组、三类生成、搜索用户、内测动作 API(接口)。
- Modify: `frontend-admin/src/api/modules/user.ts` - 用户查询增加 `dataFlag`。
- Modify: `frontend-admin/src/api/modules/ypat.ts` - 约拍查询增加 `dataFlag`。
- Modify: `frontend-admin/src/api/modules/work.ts` - 作品查询和类型增加 `dataFlag/internalBatchNo`。
- Modify: `frontend-admin/src/constants/enums.ts` - 增加内测占用状态、生成动作枚举。

Frontend admin views(管理后台前端页面)：

- Modify: `frontend-admin/src/views/internal-test/resource/index.vue` - 批量资源导入、作品组、占用筛选、三级城市。
- Modify: `frontend-admin/src/views/internal-test/generator/index.vue` - 三类生成动作工作台。
- Modify: `frontend-admin/src/views/query/user-list/index.vue` - 内测列、筛选、专属按钮。
- Modify: `frontend-admin/src/views/query/ypat-list/index.vue` - 内测列和筛选。
- Modify: `frontend-admin/src/views/manage/work-list/index.vue` - 内测列和筛选。

Frontend admin tests(管理后台前端测试)：

- Modify: `frontend-admin/tests/unit/internal-test-api.test.ts` - 内测 API(接口) 路径契约。
- Modify: `frontend-admin/tests/unit/admin-enums.test.ts` - 内测枚举契约。
- Create: `frontend-admin/tests/unit/internal-test-workbench-source.test.ts` - 页面源代码契约测试。
- Modify: `frontend-admin/tests/unit/permission.test.ts` - 确认菜单路径仍存在。

---

### Task 1: 数据库、DTO(数据传输对象)、Entity(实体) 契约

**Files(文件):**

- Modify: `docs/sql/pending/V_admin_internal_test_data.sql`
- Modify: `backend/system-object/src/main/java/com/ypat/InternalTestResourceQo.java`
- Modify: `backend/system-object/src/main/java/com/ypat/InternalTestGenerateQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/InternalTestUserActionQo.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/entity/InternalTestResource.java`
- Modify: `backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java`

- [ ] **Step 1: Write the failing source regression test(编写失败的源代码回归测试)**

Append these methods to `backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java` before the final class closing brace:

```java
    @Test
    public void lazyWorkbenchMigrationAddsResourceGroupUsageAndRegionFields() throws Exception {
        String sql = read("docs/sql/pending/V_admin_internal_test_data.sql");

        assertTrue(sql.contains("ADD COLUMN `province`"));
        assertTrue(sql.contains("ADD COLUMN `area`"));
        assertTrue(sql.contains("ADD COLUMN `group_no`"));
        assertTrue(sql.contains("ADD COLUMN `group_title`"));
        assertTrue(sql.contains("ADD COLUMN `group_sort_no`"));
        assertTrue(sql.contains("ADD COLUMN `used_flag`"));
        assertTrue(sql.contains("ADD COLUMN `used_batch_no`"));
        assertTrue(sql.contains("ADD COLUMN `used_target_type`"));
        assertTrue(sql.contains("ADD COLUMN `used_target_id`"));
        assertTrue(sql.contains("ADD COLUMN `used_at`"));
    }

    @Test
    public void lazyWorkbenchDtosExposeBatchImportGenerationAndUserActionFields() throws Exception {
        String resourceQo = read("backend/system-object/src/main/java/com/ypat/InternalTestResourceQo.java");
        String generateQo = read("backend/system-object/src/main/java/com/ypat/InternalTestGenerateQo.java");
        String actionQo = read("backend/system-object/src/main/java/com/ypat/InternalTestUserActionQo.java");
        String entity = read("backend/system-domain/src/main/java/com/ypat/entity/InternalTestResource.java");

        assertTrue(resourceQo.contains("private java.util.List<String> urls;"));
        assertTrue(resourceQo.contains("private String province;"));
        assertTrue(resourceQo.contains("private String area;"));
        assertTrue(resourceQo.contains("private String groupNo;"));
        assertTrue(resourceQo.contains("private Integer groupSize;"));
        assertTrue(resourceQo.contains("private Integer usedFlag;"));
        assertTrue(resourceQo.contains("private String usedBatchNo;"));
        assertTrue(resourceQo.contains("private String usedTargetType;"));
        assertTrue(resourceQo.contains("private Long usedTargetId;"));
        assertTrue(generateQo.contains("private String actionType;"));
        assertTrue(generateQo.contains("private Long userId;"));
        assertTrue(generateQo.contains("private java.util.List<String> groupNos;"));
        assertTrue(generateQo.contains("private String wx;"));
        assertTrue(generateQo.contains("private String mobile;"));
        assertTrue(generateQo.contains("private java.util.List<String> styleCodes;"));
        assertTrue(actionQo.contains("private Long userId;"));
        assertTrue(actionQo.contains("private Integer days;"));
        assertTrue(actionQo.contains("private String reason;"));
        assertTrue(entity.contains("private String groupNo;"));
        assertTrue(entity.contains("private Integer usedFlag;"));
        assertTrue(entity.contains("@Column(name = \"used_target_id\")"));
    }
```

- [ ] **Step 2: Run the failing backend source test(运行失败的后端源代码测试)**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
```

Expected(预期): FAIL(失败)，提示新增字段或 `InternalTestUserActionQo` 文件不存在。

- [ ] **Step 3: Extend the SQL migration(扩展数据库脚本)**

Append idempotent guarded DDL(幂等数据库变更) to `docs/sql/pending/V_admin_internal_test_data.sql`:

```sql
SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_internal_test_resource` ADD COLUMN `province` VARCHAR(64) DEFAULT NULL COMMENT ''省''',
    'SELECT ''skip t_internal_test_resource.province'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_internal_test_resource'
    AND COLUMN_NAME = 'province'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_internal_test_resource` ADD COLUMN `area` VARCHAR(64) DEFAULT NULL COMMENT ''区县''',
    'SELECT ''skip t_internal_test_resource.area'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_internal_test_resource'
    AND COLUMN_NAME = 'area'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_internal_test_resource` ADD COLUMN `group_no` VARCHAR(64) DEFAULT NULL COMMENT ''作品组编号''',
    'SELECT ''skip t_internal_test_resource.group_no'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_internal_test_resource'
    AND COLUMN_NAME = 'group_no'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_internal_test_resource` ADD COLUMN `group_title` VARCHAR(128) DEFAULT NULL COMMENT ''作品组标题''',
    'SELECT ''skip t_internal_test_resource.group_title'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_internal_test_resource'
    AND COLUMN_NAME = 'group_title'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_internal_test_resource` ADD COLUMN `group_sort_no` INT NOT NULL DEFAULT 0 COMMENT ''作品组内排序''',
    'SELECT ''skip t_internal_test_resource.group_sort_no'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_internal_test_resource'
    AND COLUMN_NAME = 'group_sort_no'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_internal_test_resource` ADD COLUMN `used_flag` TINYINT NOT NULL DEFAULT 0 COMMENT ''是否已占用：0未占用，1已占用''',
    'SELECT ''skip t_internal_test_resource.used_flag'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_internal_test_resource'
    AND COLUMN_NAME = 'used_flag'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_internal_test_resource` ADD COLUMN `used_batch_no` VARCHAR(64) DEFAULT NULL COMMENT ''占用批次号''',
    'SELECT ''skip t_internal_test_resource.used_batch_no'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_internal_test_resource'
    AND COLUMN_NAME = 'used_batch_no'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_internal_test_resource` ADD COLUMN `used_target_type` VARCHAR(16) DEFAULT NULL COMMENT ''占用目标类型：user/ypat/work''',
    'SELECT ''skip t_internal_test_resource.used_target_type'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_internal_test_resource'
    AND COLUMN_NAME = 'used_target_type'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_internal_test_resource` ADD COLUMN `used_target_id` BIGINT DEFAULT NULL COMMENT ''占用目标编号''',
    'SELECT ''skip t_internal_test_resource.used_target_id'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_internal_test_resource'
    AND COLUMN_NAME = 'used_target_id'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_internal_test_resource` ADD COLUMN `used_at` DATETIME DEFAULT NULL COMMENT ''占用时间''',
    'SELECT ''skip t_internal_test_resource.used_at'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_internal_test_resource'
    AND COLUMN_NAME = 'used_at'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
```

- [ ] **Step 4: Add DTO(数据传输对象) and entity fields(实体字段)**

Add fields plus getters/setters to `InternalTestResourceQo` and `InternalTestResource` with JavaBean(爪哇对象) naming:

```java
private java.util.List<String> urls;
private String province;
private String area;
private String groupNo;
private String groupTitle;
private Integer groupSize;
private Integer groupSortNo;
private Integer usedFlag;
private String usedBatchNo;
private String usedTargetType;
private Long usedTargetId;
private java.util.Date usedAt;
```

Add fields plus getters/setters to `InternalTestGenerateQo`:

```java
private String actionType;
private Long userId;
private java.util.List<String> groupNos;
private String wx;
private String mobile;
private java.util.List<String> styleCodes;
private String patdate;
private String patslice;
private String describ;
private String target;
```

Create `backend/system-object/src/main/java/com/ypat/InternalTestUserActionQo.java`:

```java
package com.ypat;

public class InternalTestUserActionQo implements java.io.Serializable {
    private Long userId;
    private Integer days;
    private String reason;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
```

For `InternalTestResource`, map new columns with `@Column(name = "...")`, for example:

```java
@Column(name = "group_no")
private String groupNo;
@Column(name = "used_flag")
private Integer usedFlag;
@Column(name = "used_target_id")
private Long usedTargetId;
```

- [ ] **Step 5: Run the source test to verify it passes(运行源代码测试确认通过)**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
```

Expected(预期): PASS(通过).

- [ ] **Step 6: Commit(提交)**

Run:

```bash
git add docs/sql/pending/V_admin_internal_test_data.sql \
  backend/system-object/src/main/java/com/ypat/InternalTestResourceQo.java \
  backend/system-object/src/main/java/com/ypat/InternalTestGenerateQo.java \
  backend/system-object/src/main/java/com/ypat/InternalTestUserActionQo.java \
  backend/system-domain/src/main/java/com/ypat/entity/InternalTestResource.java \
  backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java
git commit -m "feat: extend internal test resource contract"
```

---

### Task 2: 后端资源批量导入、作品组、占用释放

**Files(文件):**

- Modify: `backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java`
- Modify: `backend/system-restapi/src/main/java/com/ypat/controller/InternalTestController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/service/InternalTestServiceClient.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/AdminInternalTestController.java`
- Modify: `backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java`
- Modify: `backend/system-wap/src/test/java/com/ypat/controller/AdminInternalTestControllerSourceTest.java`

- [ ] **Step 1: Write source tests for resource service contract(编写资源服务契约测试)**

Append to `InternalTestDataSourceTest.java`:

```java
    @Test
    public void resourceServiceSupportsBatchImportGroupsUsageAndRelease() throws Exception {
        String service = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java");
        String repo = read("backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java");

        assertTrue(service.contains("batchSave"));
        assertTrue(service.contains("listAvailableGroups"));
        assertTrue(service.contains("markResourcesUsed"));
        assertTrue(service.contains("releaseResourcesByBatch"));
        assertTrue(service.contains("splitWorkGroups"));
        assertTrue(service.contains("usedFlag"));
        assertTrue(repo.contains("findByUrl"));
        assertTrue(repo.contains("findByGroupNoInAndStatus"));
        assertTrue(repo.contains("findByUsedBatchNo"));
    }
```

Append to `AdminInternalTestControllerSourceTest.java`:

```java
    @Test
    public void wapControllerExposesLazyWorkbenchResourceRoutes() throws Exception {
        String controller = read(
                "src/main/java/com/ypat/controller/AdminInternalTestController.java",
                "backend/system-wap/src/main/java/com/ypat/controller/AdminInternalTestController.java");
        String client = read(
                "src/main/java/com/ypat/service/InternalTestServiceClient.java",
                "backend/system-wap/src/main/java/com/ypat/service/InternalTestServiceClient.java");
        String restapi = read(
                "../system-restapi/src/main/java/com/ypat/controller/InternalTestController.java",
                "backend/system-restapi/src/main/java/com/ypat/controller/InternalTestController.java");

        assertTrue(controller.contains("@PostMapping(\"/resources/batch\")"));
        assertTrue(controller.contains("@GetMapping(\"/resource-groups\")"));
        assertTrue(client.contains("/service/internal-test/resources/batch"));
        assertTrue(client.contains("/service/internal-test/resource-groups"));
        assertTrue(restapi.contains("@PostMapping(\"/resources/batch\")"));
        assertTrue(restapi.contains("@GetMapping(\"/resource-groups\")"));
    }
```

- [ ] **Step 2: Run the failing tests(运行失败测试)**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
cd ../system-wap
mvn test -Dtest=AdminInternalTestControllerSourceTest
```

Expected(预期): FAIL(失败)，因为服务方法和路由尚不存在。

- [ ] **Step 3: Extend repository(扩展资源仓库)**

Add methods to `InternalTestResourceRepository.java`:

```java
InternalTestResource findByUrl(String url);

List<InternalTestResource> findByGroupNoInAndStatus(List<String> groupNos, String status);

List<InternalTestResource> findByUsedBatchNo(String usedBatchNo);
```

- [ ] **Step 4: Implement batch import and groups(实现批量导入和作品组)**

Add these public methods to `InternalTestResourceService.java`:

```java
public Map<String, Object> batchSave(InternalTestResourceQo qo) {
    validateBatchQo(qo);
    List<String> urls = normalizeUrls(qo.getUrls());
    List<List<String>> groups = splitWorkGroups(urls, qo);
    List<InternalTestResourceQo> saved = new ArrayList<InternalTestResourceQo>();
    int duplicateCount = 0;
    Date now = new Date();
    int groupIndex = 0;
    for (List<String> group : groups) {
        String groupNo = InternalTestResourceUsageType.work.value.equals(qo.getUsageType())
                ? buildGroupNo(groupIndex++)
                : null;
        int sort = 0;
        for (String url : group) {
            if (existsByUrl(url)) {
                duplicateCount++;
                continue;
            }
            InternalTestResource resource = CopyUtil.copy(qo, InternalTestResource.class);
            resource.setId(null);
            resource.setUrl(url);
            resource.setGroupNo(groupNo);
            resource.setGroupSortNo(sort++);
            resource.setUsedFlag(0);
            resource.setStatus(defaultStatus(qo.getStatus()));
            resource.setCreatedAt(now);
            resource.setUpdatedAt(now);
            saved.add(CopyUtil.copy(internalTestResourceRepository.save(resource), InternalTestResourceQo.class));
        }
    }
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("content", saved);
    result.put("createdCount", saved.size());
    result.put("duplicateCount", duplicateCount);
    return result;
}

public Map<String, Object> listAvailableGroups(InternalTestResourceQo qo) {
    if (qo == null) qo = new InternalTestResourceQo();
    qo.setUsageType(InternalTestResourceUsageType.work.value);
    qo.setStatus(InternalTestResourceStatus.enabled.value);
    qo.setUsedFlag(0);
    Map<String, Object> page = page(qo);
    Map<String, List<InternalTestResourceQo>> groups = new LinkedHashMap<String, List<InternalTestResourceQo>>();
    for (InternalTestResourceQo item : (List<InternalTestResourceQo>) page.get("content")) {
        String groupNo = CommonUtils.isNotNull(item.getGroupNo()) ? item.getGroupNo() : "single-" + item.getId();
        if (!groups.containsKey(groupNo)) {
            groups.put(groupNo, new ArrayList<InternalTestResourceQo>());
        }
        groups.get(groupNo).add(item);
    }
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    result.put("content", new ArrayList<List<InternalTestResourceQo>>(groups.values()));
    result.put("totalElements", groups.size());
    result.put("totalPages", 1);
    return result;
}

public void markResourcesUsed(List<InternalTestResource> resources, String batchNo, String targetType, Long targetId) {
    Date now = new Date();
    for (InternalTestResource resource : resources) {
        if (resource.getUsedFlag() != null && resource.getUsedFlag() == 1) {
            throw new SysException(ResponseCode.FAIL_PARA, "资源已被占用");
        }
        resource.setUsedFlag(1);
        resource.setUsedBatchNo(batchNo);
        resource.setUsedTargetType(targetType);
        resource.setUsedTargetId(targetId);
        resource.setUsedAt(now);
        resource.setUpdatedAt(now);
        internalTestResourceRepository.save(resource);
    }
}

public int releaseResourcesByBatch(String batchNo) {
    if (CommonUtils.isNull(batchNo)) return 0;
    List<InternalTestResource> resources = internalTestResourceRepository.findByUsedBatchNo(batchNo);
    for (InternalTestResource resource : resources) {
        resource.setUsedFlag(0);
        resource.setUsedBatchNo(null);
        resource.setUsedTargetType(null);
        resource.setUsedTargetId(null);
        resource.setUsedAt(null);
        resource.setUpdatedAt(new Date());
        internalTestResourceRepository.save(resource);
    }
    return resources.size();
}
```

Add helper methods in the same service:

```java
private void validateBatchQo(InternalTestResourceQo qo) {
    if (qo == null || qo.getUrls() == null || qo.getUrls().isEmpty()) {
        throw new SysException(ResponseCode.FAIL_PARA, "请输入资源URL");
    }
    validateSaveQo(qo);
}

private List<String> normalizeUrls(List<String> urls) {
    List<String> result = new ArrayList<String>();
    Set<String> seen = new HashSet<String>();
    for (String raw : urls) {
        if (CommonUtils.isNull(raw)) continue;
        String url = raw.trim();
        if (url.length() == 0 || seen.contains(url)) continue;
        seen.add(url);
        result.add(url);
    }
    if (result.isEmpty()) throw new SysException(ResponseCode.FAIL_PARA, "请输入资源URL");
    return result;
}

List<List<String>> splitWorkGroups(List<String> urls, InternalTestResourceQo qo) {
    List<List<String>> groups = new ArrayList<List<String>>();
    if (!InternalTestResourceUsageType.work.value.equals(qo.getUsageType())) {
        for (String url : urls) {
            List<String> single = new ArrayList<String>();
            single.add(url);
            groups.add(single);
        }
        return groups;
    }
    int groupSize = qo.getGroupSize() == null || qo.getGroupSize() < 1 ? 6 : qo.getGroupSize();
    for (int start = 0; start < urls.size(); start += groupSize) {
        groups.add(urls.subList(start, Math.min(urls.size(), start + groupSize)));
    }
    return groups;
}

private String buildGroupNo(int index) {
    return "ITG" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + String.format("%03d", index);
}

private boolean existsByUrl(String url) {
    return internalTestResourceRepository.findByUrl(url) != null;
}

private String defaultStatus(String status) {
    return CommonUtils.isNotNull(status) ? status : InternalTestResourceStatus.enabled.value;
}
```

- [ ] **Step 5: Add query filtering for used flag and region(增加占用和城市筛选)**

In `InternalTestResourceService.buildSpecification`, add predicates:

```java
if (CommonUtils.isNotNull(qo.getProvince())) {
    predicates.add(cb.equal(root.get("province"), qo.getProvince()));
}
if (CommonUtils.isNotNull(qo.getArea())) {
    predicates.add(cb.equal(root.get("area"), qo.getArea()));
}
if (qo.getUsedFlag() != null) {
    predicates.add(cb.equal(root.get("usedFlag"), qo.getUsedFlag()));
}
if (CommonUtils.isNotNull(qo.getGroupNo())) {
    predicates.add(cb.equal(root.get("groupNo"), qo.getGroupNo()));
}
```

- [ ] **Step 6: Expose routes(暴露路由)**

Add to `InternalTestController.java`:

```java
@PostMapping("/resources/batch")
public Map<String, Object> batchResources(@RequestBody InternalTestResourceQo qo) {
    return internalTestResourceService.batchSave(qo);
}

@GetMapping("/resource-groups")
public Map<String, Object> resourceGroups(InternalTestResourceQo qo) {
    return internalTestResourceService.listAvailableGroups(qo);
}
```

Add to `InternalTestServiceClient.java`:

```java
@PostMapping("/service/internal-test/resources/batch")
String batchResources(@RequestBody InternalTestResourceQo qo);

@GetMapping("/service/internal-test/resource-groups")
String resourceGroups(@SpringQueryMap InternalTestResourceQo qo);
```

Add to `AdminInternalTestController.java`:

```java
@PostMapping("/resources/batch")
public ResponseApiBody batchResources(@RequestBody InternalTestResourceQo qo) {
    return ResponseApiBody.success(parseResponseRes(internalTestServiceClient.batchResources(qo)));
}

@GetMapping("/resource-groups")
public ResponseApiBody resourceGroups(InternalTestResourceQo qo) {
    return ResponseApiBody.success(parseResponseRes(internalTestServiceClient.resourceGroups(qo)));
}
```

- [ ] **Step 7: Run tests(运行测试)**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
cd ../system-wap
mvn test -Dtest=AdminInternalTestControllerSourceTest
```

Expected(预期): PASS(通过).

- [ ] **Step 8: Commit(提交)**

Run:

```bash
git add backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java \
  backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java \
  backend/system-restapi/src/main/java/com/ypat/controller/InternalTestController.java \
  backend/system-wap/src/main/java/com/ypat/service/InternalTestServiceClient.java \
  backend/system-wap/src/main/java/com/ypat/controller/AdminInternalTestController.java \
  backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java \
  backend/system-wap/src/test/java/com/ypat/controller/AdminInternalTestControllerSourceTest.java
git commit -m "feat: add internal resource batch import"
```

---

### Task 3: 后端三类生成入口和资源占用

**Files(文件):**

- Modify: `backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java`
- Modify: `backend/system-restapi/src/main/java/com/ypat/controller/InternalTestController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/service/InternalTestServiceClient.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/AdminInternalTestController.java`
- Modify: `backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java`
- Modify: `backend/system-wap/src/test/java/com/ypat/controller/AdminInternalTestControllerSourceTest.java`

- [ ] **Step 1: Write source tests for generation split(编写生成入口拆分源代码测试)**

Append to `InternalTestDataSourceTest.java`:

```java
    @Test
    public void dataServiceSplitsUserWorkYpatGenerationAndMarksResourcesUsed() throws Exception {
        String service = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java");

        assertTrue(service.contains("generateUsers"));
        assertTrue(service.contains("generateWorks"));
        assertTrue(service.contains("generateYpats"));
        assertTrue(service.contains("loadAvailableWorkGroup"));
        assertTrue(service.contains("markResourcesUsed"));
        assertTrue(service.contains("releaseResourcesByBatch"));
        assertTrue(service.contains("setWx(qo.getWx())"));
        assertTrue(service.contains("setMobile(qo.getMobile())"));
        assertTrue(service.contains("String.join(\",\", qo.getStyleCodes())"));
    }
```

Append to `AdminInternalTestControllerSourceTest.java`:

```java
    @Test
    public void wapControllerExposesSplitGenerationRoutes() throws Exception {
        String controller = read(
                "src/main/java/com/ypat/controller/AdminInternalTestController.java",
                "backend/system-wap/src/main/java/com/ypat/controller/AdminInternalTestController.java");
        String client = read(
                "src/main/java/com/ypat/service/InternalTestServiceClient.java",
                "backend/system-wap/src/main/java/com/ypat/service/InternalTestServiceClient.java");

        assertTrue(controller.contains("@PostMapping(\"/generate/users\")"));
        assertTrue(controller.contains("@PostMapping(\"/generate/works\")"));
        assertTrue(controller.contains("@PostMapping(\"/generate/ypats\")"));
        assertTrue(client.contains("/service/internal-test/generate/users"));
        assertTrue(client.contains("/service/internal-test/generate/works"));
        assertTrue(client.contains("/service/internal-test/generate/ypats"));
    }
```

- [ ] **Step 2: Run failing tests(运行失败测试)**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
cd ../system-wap
mvn test -Dtest=AdminInternalTestControllerSourceTest
```

Expected(预期): FAIL(失败).

- [ ] **Step 3: Inject resource service(注入资源服务)**

In `InternalTestDataService.java`, add:

```java
@Autowired
private InternalTestResourceService internalTestResourceService;
```

- [ ] **Step 4: Add split generation methods(增加拆分生成方法)**

In `InternalTestDataService.java`, add public methods:

```java
public InternalTestBatchQo generateUsers(InternalTestGenerateQo qo) {
    CreateUsersResult usersResult = createInternalUsers(qo, buildBatchNo());
    return buildBatch(usersResult.batchNo, usersResult.users.size(), 0, 0);
}

public InternalTestBatchQo generateWorks(InternalTestGenerateQo qo) {
    if (qo == null || qo.getUserId() == null || CollectionUtils.isEmpty(qo.getGroupNos())) {
        throw new SysException(ResponseCode.FAIL_PARA);
    }
    User user = loadInternalUser(qo.getUserId());
    String batchNo = CommonUtils.isNotNull(qo.getBatchNo()) ? qo.getBatchNo() : buildBatchNo();
    int workCount = 0;
    for (String groupNo : qo.getGroupNos()) {
        List<InternalTestResource> group = loadAvailableWorkGroup(groupNo);
        Work work = createWorkFromGroup(user, group, qo, batchNo);
        internalTestResourceService.markResourcesUsed(group, batchNo, "work", work.getId());
        workCount++;
    }
    return buildBatch(batchNo, 0, 0, workCount);
}

public InternalTestBatchQo generateYpats(InternalTestGenerateQo qo) {
    if (qo == null || qo.getUserId() == null) {
        throw new SysException(ResponseCode.FAIL_PARA);
    }
    User user = loadInternalUser(qo.getUserId());
    updateInternalUserContact(user, qo);
    String batchNo = CommonUtils.isNotNull(qo.getBatchNo()) ? qo.getBatchNo() : buildBatchNo();
    List<InternalTestResource> resources = ensureResources(
            qo.getYpatResourceIds(),
            InternalTestResourceUsageType.ypat.value,
            InternalTestResourceMediaType.image.value,
            qo.getStyleCode(),
            1);
    YpatInfo ypat = createYpat(user, pick(resources, 0), qo, batchNo);
    internalTestResourceService.markResourcesUsed(resources, batchNo, "ypat", ypat.getId());
    return buildBatch(batchNo, 0, 1, 0);
}
```

Change `createYpat` to return `YpatInfo` instead of `void` and return saved `ypat`. Change `createWork` to return `Work` instead of `void` where useful.

- [ ] **Step 5: Add helpers for user contact and work group(增加用户联系方式和作品组辅助方法)**

Add to `InternalTestDataService.java`:

```java
private User loadInternalUser(Long userId) {
    User user = userRepository.findOne(userId);
    if (user == null) throw new SysException(ResponseCode.FAIL_NOT);
    if (!InternalTestDataFlag.internalTest.value.equals(user.getDataFlag())) {
        throw new SysException(ResponseCode.FAIL_PARA, "只能操作内测用户");
    }
    return user;
}

private void updateInternalUserContact(User user, InternalTestGenerateQo qo) {
    if (CommonUtils.isNull(qo.getWx()) || CommonUtils.isNull(qo.getMobile())) {
        throw new SysException(ResponseCode.FAIL_PARA, "微信号和联系电话不能为空");
    }
    user.setWx(qo.getWx());
    user.setMobile(qo.getMobile());
    userRepository.save(user);
}

private List<InternalTestResource> loadAvailableWorkGroup(String groupNo) {
    if (CommonUtils.isNull(groupNo)) throw new SysException(ResponseCode.FAIL_PARA);
    List<String> groupNos = new ArrayList<String>();
    groupNos.add(groupNo);
    List<InternalTestResource> resources = internalTestResourceRepository.findByGroupNoInAndStatus(groupNos, InternalTestResourceStatus.enabled.value);
    if (CollectionUtils.isEmpty(resources)) throw new SysException(ResponseCode.FAIL_PARA, "作品组资源不足");
    String mediaType = resources.get(0).getMediaType();
    for (InternalTestResource resource : resources) {
        if (!InternalTestResourceUsageType.work.value.equals(resource.getUsageType())) {
            throw new SysException(ResponseCode.FAIL_PARA, "作品组用途错误");
        }
        if (resource.getUsedFlag() != null && resource.getUsedFlag() == 1) {
            throw new SysException(ResponseCode.FAIL_PARA, "作品组资源已占用");
        }
        if (!mediaType.equals(resource.getMediaType())) {
            throw new SysException(ResponseCode.FAIL_PARA, "同一作品组不能同时包含图片和视频");
        }
    }
    return resources;
}
```

- [ ] **Step 6: Add createWorkFromGroup(按作品组创建作品)**

Add to `InternalTestDataService.java`:

```java
private Work createWorkFromGroup(User user, List<InternalTestResource> group, InternalTestGenerateQo qo, String batchNo) {
    Date now = new Date();
    InternalTestResource first = group.get(0);
    Work work = new Work();
    work.setUserid(user.getId());
    work.setDescription(defaultString(qo.getDescrib(), defaultString(first.getDescription(), "内测作品内容")));
    work.setDevice("internal-test");
    work.setShootLocation(defaultString(qo.getCity(), user.getCity()));
    work.setReturnPhotoFlag(1);
    work.setMediaType(toWorkMediaType(first.getMediaType()));
    work.setIsNationwide(0);
    work.setStatus(resolveWorkStatus(qo.getPublishStatus()));
    work.setReadCount(0);
    work.setLikeCount(0);
    work.setFavoriteCount(0);
    work.setPublishTime(now);
    work.setCreatedAt(now);
    work.setUpdatedAt(now);
    work.setDeletedFlag(0);
    work.setCity(defaultString(qo.getCity(), user.getCity()));
    work.setArea(defaultString(qo.getArea(), user.getArea()));
    work.setDataFlag(InternalTestDataFlag.internalTest.value);
    work.setInternalBatchNo(batchNo);
    work = workRepository.save(work);

    int sort = 0;
    for (InternalTestResource resource : group) {
        WorkMedia media = new WorkMedia();
        media.setWorkId(work.getId());
        media.setUserId(user.getId());
        media.setType(toWorkMediaType(resource.getMediaType()));
        media.setUrl(resource.getUrl());
        media.setFileSize(0L);
        media.setMime(InternalTestResourceMediaType.video.value.equals(resource.getMediaType()) ? "video/mp4" : "image/jpeg");
        media.setSortNo(sort++);
        media.setUploadStatus("1");
        media.setCreatedAt(now);
        workMediaRepository.save(media);
    }
    return work;
}
```

- [ ] **Step 7: Release resources during cleanup(清理时释放资源)**

In `cleanup(InternalTestGenerateQo qo)`, after building the batch result, call:

```java
int releasedResources = batchNo == null ? 0 : internalTestResourceService.releaseResourcesByBatch(batchNo);
batch.setReleasedResourceCount(releasedResources);
```

Add `releasedResourceCount` field to `InternalTestBatchQo` with getter/setter.

- [ ] **Step 8: Expose split generation routes(暴露拆分生成路由)**

Add `generateUsers`, `generateWorks`, `generateYpats` to `InternalTestController`, `InternalTestServiceClient`, and `AdminInternalTestController` following the existing `generate` route pattern. Use paths:

```java
@PostMapping("/generate/users")
@PostMapping("/generate/works")
@PostMapping("/generate/ypats")
```

- [ ] **Step 9: Run tests(运行测试)**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
cd ../system-wap
mvn test -Dtest=AdminInternalTestControllerSourceTest
```

Expected(预期): PASS(通过).

- [ ] **Step 10: Commit(提交)**

Run:

```bash
git add backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java \
  backend/system-object/src/main/java/com/ypat/InternalTestBatchQo.java \
  backend/system-restapi/src/main/java/com/ypat/controller/InternalTestController.java \
  backend/system-wap/src/main/java/com/ypat/service/InternalTestServiceClient.java \
  backend/system-wap/src/main/java/com/ypat/controller/AdminInternalTestController.java \
  backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java \
  backend/system-wap/src/test/java/com/ypat/controller/AdminInternalTestControllerSourceTest.java
git commit -m "feat: split internal test generation flows"
```

---

### Task 4: 内测用户一键会员、认证、保证金

**Files(文件):**

- Create: `backend/system-domain/src/main/java/com/ypat/service/InternalTestUserActionService.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/DepositService.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/DepositOrderRepository.java`
- Modify: `backend/system-restapi/src/main/java/com/ypat/controller/InternalTestController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/service/InternalTestServiceClient.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/AdminInternalTestController.java`
- Modify: `backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java`

- [ ] **Step 1: Write source tests for safe internal user actions(编写安全动作源代码测试)**

Append to `InternalTestDataSourceTest.java`:

```java
    @Test
    public void internalUserActionsRequireInternalFlagAndUseRealBusinessServices() throws Exception {
        String service = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestUserActionService.java");
        String deposit = read("backend/system-domain/src/main/java/com/ypat/service/DepositService.java");

        assertTrue(service.contains("grantMember"));
        assertTrue(service.contains("verifyUser"));
        assertTrue(service.contains("markDepositPaid"));
        assertTrue(service.contains("InternalTestDataFlag.internalTest.value.equals"));
        assertTrue(service.contains("memberService.adminGrant"));
        assertTrue(service.contains("depositService.createInternalTestPaidOrder"));
        assertTrue(service.contains("user.setRealnameflag"));
        assertTrue(service.contains("user.setCreditflag"));
        assertTrue(deposit.contains("createInternalTestPaidOrder"));
        assertTrue(deposit.contains("INTERNAL_TEST"));
    }
```

- [ ] **Step 2: Run failing test(运行失败测试)**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
```

Expected(预期): FAIL(失败).

- [ ] **Step 3: Add internal deposit support(增加内测保证金支持)**

Add to `DepositOrderRepository.java`:

```java
DepositOrder findByUserIdAndChannelAndStatus(Long userId, String channel, String status);
```

Add to `DepositService.java`:

```java
public DepositOrderQo createInternalTestPaidOrder(Long userId) {
    if (userId == null) throw new SysException(ResponseCode.FAIL_PARA);
    DepositOrder existing = depositOrderRepository.findByUserIdAndChannelAndStatus(userId, "INTERNAL_TEST", PaymentStatus.PAID.value);
    if (existing != null) return CopyUtil.copy(existing, DepositOrderQo.class);
    DepositConfig config = loadConfig();
    Date now = new Date();
    DepositOrder order = new DepositOrder();
    order.setOutTradeNo("ITD" + new SimpleDateFormat("yyyyMMddHHmmss").format(now) + userId);
    order.setUserId(userId);
    order.setAmountFen(effectiveAmountFen(config));
    order.setChannel("INTERNAL_TEST");
    order.setStatus(PaymentStatus.PAID.value);
    order.setTransactionId("INTERNAL_TEST");
    order.setPaidAt(now);
    order.setCreatedAt(now);
    order.setUpdatedAt(now);
    order.setVersion(0);
    order = depositOrderRepository.save(order);
    User user = userRepository.findById(userId);
    if (user == null) throw new SysException(ResponseCode.FAIL_NOT);
    user.setCreditflag("1");
    userRepository.save(user);
    return CopyUtil.copy(order, DepositOrderQo.class);
}
```

- [ ] **Step 4: Create InternalTestUserActionService(创建内测用户动作服务)**

Create `backend/system-domain/src/main/java/com/ypat/service/InternalTestUserActionService.java`:

```java
package com.ypat.service;

import com.ypat.InternalTestUserActionQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.User;
import com.ypat.enums.InternalTestDataFlag;
import com.ypat.enums.YesNo;
import com.ypat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class InternalTestUserActionService {
    @Autowired private UserRepository userRepository;
    @Autowired private MemberService memberService;
    @Autowired private DepositService depositService;

    public boolean grantMember(InternalTestUserActionQo qo) {
        User user = requireInternalUser(qo);
        int days = qo.getDays() == null || qo.getDays() <= 0 ? 365 : qo.getDays();
        return memberService.adminGrant(user.getId(), days, null, reason(qo, "内测数据一键会员"));
    }

    public boolean verifyUser(InternalTestUserActionQo qo) {
        User user = requireInternalUser(qo);
        user.setRealnameflag(YesNo.yes.value);
        user.setCreditflag(YesNo.yes.value);
        userRepository.save(user);
        return true;
    }

    public boolean markDepositPaid(InternalTestUserActionQo qo) {
        User user = requireInternalUser(qo);
        depositService.createInternalTestPaidOrder(user.getId());
        return true;
    }

    private User requireInternalUser(InternalTestUserActionQo qo) {
        if (qo == null || qo.getUserId() == null) throw new SysException(ResponseCode.FAIL_PARA);
        User user = userRepository.findOne(qo.getUserId());
        if (user == null) throw new SysException(ResponseCode.FAIL_NOT);
        if (!InternalTestDataFlag.internalTest.value.equals(user.getDataFlag())) {
            throw new SysException(ResponseCode.FAIL_PARA, "只能操作内测用户");
        }
        return user;
    }

    private String reason(InternalTestUserActionQo qo, String fallback) {
        return qo != null && qo.getReason() != null && qo.getReason().trim().length() > 0 ? qo.getReason().trim() : fallback;
    }
}
```

- [ ] **Step 5: Expose action routes(暴露动作路由)**

Inject `InternalTestUserActionService` into `InternalTestController` and add:

```java
@PostMapping("/users/{userId}/grant-member")
public Boolean grantMember(@PathVariable("userId") Long userId, @RequestBody InternalTestUserActionQo qo) {
    if (qo == null) qo = new InternalTestUserActionQo();
    qo.setUserId(userId);
    return internalTestUserActionService.grantMember(qo);
}

@PostMapping("/users/{userId}/verify")
public Boolean verifyUser(@PathVariable("userId") Long userId, @RequestBody InternalTestUserActionQo qo) {
    if (qo == null) qo = new InternalTestUserActionQo();
    qo.setUserId(userId);
    return internalTestUserActionService.verifyUser(qo);
}

@PostMapping("/users/{userId}/deposit-paid")
public Boolean depositPaid(@PathVariable("userId") Long userId, @RequestBody InternalTestUserActionQo qo) {
    if (qo == null) qo = new InternalTestUserActionQo();
    qo.setUserId(userId);
    return internalTestUserActionService.markDepositPaid(qo);
}
```

Mirror the same paths in `InternalTestServiceClient` and `AdminInternalTestController`.

- [ ] **Step 6: Run tests(运行测试)**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
cd ../system-wap
mvn test -Dtest=AdminInternalTestControllerSourceTest
```

Expected(预期): PASS(通过).

- [ ] **Step 7: Commit(提交)**

Run:

```bash
git add backend/system-domain/src/main/java/com/ypat/service/InternalTestUserActionService.java \
  backend/system-domain/src/main/java/com/ypat/service/DepositService.java \
  backend/system-domain/src/main/java/com/ypat/repository/DepositOrderRepository.java \
  backend/system-restapi/src/main/java/com/ypat/controller/InternalTestController.java \
  backend/system-wap/src/main/java/com/ypat/service/InternalTestServiceClient.java \
  backend/system-wap/src/main/java/com/ypat/controller/AdminInternalTestController.java \
  backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java
git commit -m "feat: add internal test user actions"
```

---

### Task 5: 用户、作品、约拍列表 `dataFlag`(数据标识) 筛选

**Files(文件):**

- Modify: `backend/system-domain/src/main/java/com/ypat/service/UserService.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/AdminUserController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/AdminWorkController.java`
- Modify: `backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java`

- [ ] **Step 1: Write source test for list filters(编写列表筛选源代码测试)**

Append to `InternalTestDataSourceTest.java`:

```java
    @Test
    public void businessListsSupportInternalTestDataFlagFilter() throws Exception {
        String userService = read("backend/system-domain/src/main/java/com/ypat/service/UserService.java");
        String ypatService = read("backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java");
        String workService = read("backend/system-domain/src/main/java/com/ypat/service/WorkService.java");
        String adminUser = read("backend/system-wap/src/main/java/com/ypat/controller/AdminUserController.java");
        String adminYpat = read("backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java");
        String adminWork = read("backend/system-wap/src/main/java/com/ypat/controller/AdminWorkController.java");

        assertTrue(userService.contains("applyDataFlagPredicate"));
        assertTrue(ypatService.contains("applyDataFlagPredicate"));
        assertTrue(workService.contains("applyDataFlagPredicate"));
        assertTrue(adminUser.contains("@RequestParam(value = \"dataFlag\""));
        assertTrue(adminYpat.contains("@RequestParam(value = \"dataFlag\""));
        assertTrue(adminWork.contains("@RequestParam(value = \"dataFlag\""));
    }
```

- [ ] **Step 2: Run failing test(运行失败测试)**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
```

Expected(预期): FAIL(失败).

- [ ] **Step 3: Add data flag predicates(增加数据标识谓词)**

In `UserService`, `YpatInfoService`, and `WorkService`, add a helper with the local root type:

```java
private void applyDataFlagPredicate(List<Predicate> predicatesList, CriteriaBuilder criteriaBuilder, Root<User> root, String dataFlag) {
    if (StringUtils.isBlank(dataFlag)) return;
    if (InternalTestDataFlag.internalTest.value.equals(dataFlag)) {
        predicatesList.add(criteriaBuilder.equal(root.get("dataFlag"), InternalTestDataFlag.internalTest.value));
    } else if (InternalTestDataFlag.real.value.equals(dataFlag)) {
        predicatesList.add(criteriaBuilder.or(
                criteriaBuilder.isNull(root.get("dataFlag")),
                criteriaBuilder.notEqual(root.get("dataFlag"), InternalTestDataFlag.internalTest.value)
        ));
    }
}
```

Use `Root<YpatInfo>` in `YpatInfoService` and `Root<Work>` in `WorkService`. Import `InternalTestDataFlag`.

Call the helper inside each existing predicate builder:

```java
applyDataFlagPredicate(predicatesList, criteriaBuilder, root, queryQo.getDataFlag());
```

- [ ] **Step 4: Pass dataFlag through admin controllers(管理端控制器透传 dataFlag)**

In each admin controller list endpoint, add:

```java
@RequestParam(value = "dataFlag", required = false) String dataFlag
```

Then set it on the query object:

```java
if (StringUtils.isNotBlank(dataFlag)) {
    qo.setDataFlag(dataFlag);
}
```

- [ ] **Step 5: Run tests(运行测试)**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
```

Expected(预期): PASS(通过).

- [ ] **Step 6: Commit(提交)**

Run:

```bash
git add backend/system-domain/src/main/java/com/ypat/service/UserService.java \
  backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java \
  backend/system-domain/src/main/java/com/ypat/service/WorkService.java \
  backend/system-wap/src/main/java/com/ypat/controller/AdminUserController.java \
  backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java \
  backend/system-wap/src/main/java/com/ypat/controller/AdminWorkController.java \
  backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java
git commit -m "feat: filter internal test business data"
```

---

### Task 6: 前端 API(接口) 与枚举契约

**Files(文件):**

- Modify: `frontend-admin/src/api/modules/internal-test.ts`
- Modify: `frontend-admin/src/api/modules/user.ts`
- Modify: `frontend-admin/src/api/modules/ypat.ts`
- Modify: `frontend-admin/src/api/modules/work.ts`
- Modify: `frontend-admin/src/constants/enums.ts`
- Modify: `frontend-admin/tests/unit/internal-test-api.test.ts`
- Modify: `frontend-admin/tests/unit/admin-enums.test.ts`

- [ ] **Step 1: Write failing frontend API tests(编写失败的前端接口测试)**

Extend `frontend-admin/tests/unit/internal-test-api.test.ts` inside the existing `describe('内测数据 API', ...)` block:

```ts
it('应使用内测工作台懒人版路由', async () => {
  const api = await import('@/api/modules/internal-test')

  await api.batchCreateInternalResources({ mediaType: 'image', usageType: 'work', urls: ['https://example.com/1.jpg'] })
  expect(postMock).toHaveBeenCalledWith('/admin/internal-test/resources/batch', {
    mediaType: 'image',
    usageType: 'work',
    urls: ['https://example.com/1.jpg'],
  })

  await api.getInternalResourceGroups({ page: 0, size: 10, usageType: 'work', usedFlag: 0 })
  expect(getMock).toHaveBeenCalledWith('/admin/internal-test/resource-groups', {
    page: 0,
    size: 10,
    usageType: 'work',
    usedFlag: 0,
  })

  await api.generateInternalUsers({ actionType: 'create_users', userCount: 2 })
  expect(postMock).toHaveBeenCalledWith('/admin/internal-test/generate/users', { actionType: 'create_users', userCount: 2 })

  await api.generateInternalWorks({ actionType: 'create_works', userId: 1, groupNos: ['ITG1'] })
  expect(postMock).toHaveBeenCalledWith('/admin/internal-test/generate/works', { actionType: 'create_works', userId: 1, groupNos: ['ITG1'] })

  await api.generateInternalYpats({ actionType: 'create_ypats', userId: 1, wx: 'wx-test', mobile: '13800138000' })
  expect(postMock).toHaveBeenCalledWith('/admin/internal-test/generate/ypats', {
    actionType: 'create_ypats',
    userId: 1,
    wx: 'wx-test',
    mobile: '13800138000',
  })

  await api.searchInternalUsers({ keyword: '内测', page: 0, size: 20 })
  expect(getMock).toHaveBeenCalledWith('/admin/internal-test/users/search', { keyword: '内测', page: 0, size: 20 })

  await api.grantInternalUserMember(1, { days: 365, reason: '内测数据一键会员' })
  expect(postMock).toHaveBeenCalledWith('/admin/internal-test/users/1/grant-member', { days: 365, reason: '内测数据一键会员' })

  await api.verifyInternalUser(1, { reason: '内测数据一键认证' })
  expect(postMock).toHaveBeenCalledWith('/admin/internal-test/users/1/verify', { reason: '内测数据一键认证' })

  await api.markInternalUserDepositPaid(1, { reason: '内测数据一键保证金' })
  expect(postMock).toHaveBeenCalledWith('/admin/internal-test/users/1/deposit-paid', { reason: '内测数据一键保证金' })
})
```

Extend `frontend-admin/tests/unit/admin-enums.test.ts`:

```ts
it('内测工作台枚举应包含生成动作和资源占用状态', () => {
  expect(InternalTestGenerateAction.CREATE_USERS.value).toBe('create_users')
  expect(InternalTestGenerateAction.CREATE_WORKS.value).toBe('create_works')
  expect(InternalTestGenerateAction.CREATE_YPATS.value).toBe('create_ypats')
  expect(InternalTestResourceUsedFlag.UNUSED.value).toBe(0)
  expect(InternalTestResourceUsedFlag.USED.value).toBe(1)
})
```

- [ ] **Step 2: Run failing frontend tests(运行失败的前端测试)**

Run:

```bash
cd frontend-admin
pnpm vitest run tests/unit/internal-test-api.test.ts tests/unit/admin-enums.test.ts
```

Expected(预期): FAIL(失败).

- [ ] **Step 3: Extend internal-test API module(扩展内测接口模块)**

Add types and functions to `frontend-admin/src/api/modules/internal-test.ts`:

```ts
export interface InternalTestResourceGroup {
  groupNo: string
  groupTitle?: string
  mediaType?: string
  resources: InternalTestResource[]
}

export interface InternalTestUserActionPayload {
  days?: number
  reason?: string
}

export function batchCreateInternalResources(data: InternalTestResource): Promise<ApiResult<unknown>> {
  return post('/admin/internal-test/resources/batch', data)
}

export function getInternalResourceGroups(
  params: InternalTestResourceQuery,
): Promise<ApiResult<PageResult<InternalTestResourceGroup>>> {
  return get<PageResult<InternalTestResourceGroup>>('/admin/internal-test/resource-groups', params as Record<string, unknown>)
}

export function generateInternalUsers(data: InternalTestGeneratePayload): Promise<ApiResult<InternalTestBatch>> {
  return post<InternalTestBatch>('/admin/internal-test/generate/users', data)
}

export function generateInternalWorks(data: InternalTestGeneratePayload): Promise<ApiResult<InternalTestBatch>> {
  return post<InternalTestBatch>('/admin/internal-test/generate/works', data)
}

export function generateInternalYpats(data: InternalTestGeneratePayload): Promise<ApiResult<InternalTestBatch>> {
  return post<InternalTestBatch>('/admin/internal-test/generate/ypats', data)
}

export function searchInternalUsers(params: InternalTestUserQuery): Promise<ApiResult<PageResult<InternalTestUser>>> {
  return get<PageResult<InternalTestUser>>('/admin/internal-test/users/search', params as Record<string, unknown>)
}

export function grantInternalUserMember(userId: number, data: InternalTestUserActionPayload): Promise<ApiResult<boolean>> {
  return post<boolean>(`/admin/internal-test/users/${userId}/grant-member`, data)
}

export function verifyInternalUser(userId: number, data: InternalTestUserActionPayload): Promise<ApiResult<boolean>> {
  return post<boolean>(`/admin/internal-test/users/${userId}/verify`, data)
}

export function markInternalUserDepositPaid(userId: number, data: InternalTestUserActionPayload): Promise<ApiResult<boolean>> {
  return post<boolean>(`/admin/internal-test/users/${userId}/deposit-paid`, data)
}
```

Extend `InternalTestResource`:

```ts
province?: string
area?: string
groupNo?: string
groupTitle?: string
groupSortNo?: number
usedFlag?: number
usedBatchNo?: string
usedTargetType?: string
usedTargetId?: number
usedAt?: string
urls?: string[]
groupSize?: number
```

Extend `InternalTestGeneratePayload`:

```ts
actionType?: string
userId?: number
groupNos?: string[]
wx?: string
mobile?: string
styleCodes?: string[]
patdate?: string
patslice?: string
describ?: string
target?: string
```

- [ ] **Step 4: Extend query API types(扩展查询类型)**

Add `dataFlag?: string` to:

```ts
frontend-admin/src/api/modules/user.ts UserListQuery
frontend-admin/src/api/modules/ypat.ts YpatListQuery
frontend-admin/src/api/modules/work.ts WorkListQuery
frontend-admin/src/api/modules/work.ts WorkAdminInfo
```

Add `internalBatchNo?: string` to `WorkAdminInfo`.

- [ ] **Step 5: Add frontend enums(增加前端枚举)**

Add to `frontend-admin/src/constants/enums.ts`:

```ts
export const InternalTestGenerateAction = {
  CREATE_USERS: { value: 'create_users', name: '新增用户' },
  CREATE_WORKS: { value: 'create_works', name: '新增作品' },
  CREATE_YPATS: { value: 'create_ypats', name: '新增约拍' },
} as const
export const getInternalTestGenerateActionOptions = () =>
  Object.values(InternalTestGenerateAction).map((o) => ({ label: o.name, value: o.value }))

export const InternalTestResourceUsedFlag = {
  UNUSED: { value: 0, name: '未占用', type: 'success' as const },
  USED: { value: 1, name: '已占用', type: 'warning' as const },
} as const
export const getInternalTestResourceUsedFlagOptions = () =>
  Object.values(InternalTestResourceUsedFlag).map((o) => ({ label: o.name, value: o.value }))
```

- [ ] **Step 6: Run frontend tests(运行前端测试)**

Run:

```bash
cd frontend-admin
pnpm vitest run tests/unit/internal-test-api.test.ts tests/unit/admin-enums.test.ts
```

Expected(预期): PASS(通过).

- [ ] **Step 7: Commit(提交)**

Run:

```bash
git add frontend-admin/src/api/modules/internal-test.ts \
  frontend-admin/src/api/modules/user.ts \
  frontend-admin/src/api/modules/ypat.ts \
  frontend-admin/src/api/modules/work.ts \
  frontend-admin/src/constants/enums.ts \
  frontend-admin/tests/unit/internal-test-api.test.ts \
  frontend-admin/tests/unit/admin-enums.test.ts
git commit -m "feat: add internal test workbench api"
```

---

### Task 7: 前端资源管理懒人导入

**Files(文件):**

- Modify: `frontend-admin/src/views/internal-test/resource/index.vue`
- Create: `frontend-admin/tests/unit/internal-test-workbench-source.test.ts`

- [ ] **Step 1: Write source test for resource page(编写资源页源代码测试)**

Create `frontend-admin/tests/unit/internal-test-workbench-source.test.ts`:

```ts
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

function source(path: string): string {
  return readFileSync(resolve(process.cwd(), path), 'utf-8')
}

describe('internal test workbench source contracts', () => {
  it('资源管理页支持批量 URL、作品组、占用筛选和三级城市', () => {
    const page = source('src/views/internal-test/resource/index.vue')

    expect(page).toContain('batchCreateInternalResources')
    expect(page).toContain('urlsText')
    expect(page).toContain('groupSize')
    expect(page).toContain('usedFlag')
    expect(page).toContain('regionCascaderOptions')
    expect(page).toContain('一行一个 URL')
    expect(page).toContain('作品组')
    expect(page).toContain('占用状态')
  })
})
```

- [ ] **Step 2: Run failing test(运行失败测试)**

Run:

```bash
cd frontend-admin
pnpm vitest run tests/unit/internal-test-workbench-source.test.ts
```

Expected(预期): FAIL(失败).

- [ ] **Step 3: Import new API and region helpers(导入新接口和城市工具)**

In `resource/index.vue`, import:

```ts
batchCreateInternalResources,
getInternalTestResourceUsedFlagOptions,
regionCascaderOptions,
toRegionFields,
type RegionPath,
```

Use the existing import style and actual modules:

```ts
import { regionCascaderOptions, toRegionFields, type RegionPath } from '@/utils/region'
```

- [ ] **Step 4: Extend form state(扩展表单状态)**

Add to the resource form state:

```ts
urlsText: ''
province: ''
area: ''
groupSize: 6
usedFlag: undefined
```

Add a computed Cascader(级联选择) path:

```ts
const formRegionPath = computed<RegionPath>({
  get: () => [form.province, form.city, form.area].filter(Boolean),
  set: (path) => {
    const fields = toRegionFields(path)
    form.province = fields.province
    form.city = fields.city
    form.area = fields.area
  },
})
```

- [ ] **Step 5: Add query used flag and region fields(增加查询占用和城市字段)**

Extend `query`:

```ts
province: '',
area: '',
usedFlag: undefined,
```

Add Cascader(级联选择) for search:

```ts
const queryRegionPath = computed<RegionPath>({
  get: () => [query.province, query.city, query.area].filter(Boolean),
  set: (path) => {
    const fields = toRegionFields(path)
    query.province = fields.province
    query.city = fields.city
    query.area = fields.area
  },
})
```

- [ ] **Step 6: Implement batch save(实现批量保存)**

Replace create branch in `saveResource()` with:

```ts
const urls = form.urlsText
  .split('\n')
  .map((item) => item.trim())
  .filter(Boolean)

if (!payload.id) {
  await batchCreateInternalResources({
    ...payload,
    urls,
    groupSize: form.usageType === InternalTestUsageType.WORK.value ? form.groupSize : undefined,
  })
  ElMessage.success('资源批量创建成功')
} else {
  await updateInternalResource(payload)
  ElMessage.success('资源更新成功')
}
```

Keep edit mode using single `url`.

- [ ] **Step 7: Update template(更新模板)**

Add search controls:

```vue
<el-form-item label="城市">
  <el-cascader
    v-model="queryRegionPath"
    :options="regionCascaderOptions"
    :props="regionCascaderProps"
    clearable
    filterable
    placeholder="请选择省 / 市 / 区"
  />
</el-form-item>
<el-form-item label="占用状态">
  <el-select v-model="query.usedFlag" clearable placeholder="全部" style="width: 140px">
    <el-option
      v-for="option in getInternalTestResourceUsedFlagOptions()"
      :key="option.value"
      :label="option.label"
      :value="option.value"
    />
  </el-select>
</el-form-item>
```

Add form controls:

```vue
<el-form-item v-if="!form.id" label="资源 URL" prop="urlsText">
  <el-input v-model="form.urlsText" type="textarea" :rows="8" placeholder="一行一个 URL" />
</el-form-item>
<el-form-item v-else label="资源 URL" prop="url">
  <el-input v-model="form.url" :disabled="form.usedFlag === 1" />
</el-form-item>
<el-form-item v-if="form.usageType === InternalTestUsageType.WORK.value && !form.id" label="每组资源数">
  <el-input-number v-model="form.groupSize" :min="1" :max="20" controls-position="right" />
</el-form-item>
<el-form-item label="城市">
  <el-cascader
    v-model="formRegionPath"
    :options="regionCascaderOptions"
    :props="regionCascaderProps"
    clearable
    filterable
    placeholder="请选择省 / 市 / 区"
  />
</el-form-item>
```

Add table columns:

```vue
<el-table-column prop="groupNo" label="作品组" min-width="130" show-overflow-tooltip />
<el-table-column label="占用状态" width="100" align="center">
  <template #default="{ row }">
    <el-tag :type="row.usedFlag === 1 ? 'warning' : 'success'" size="small">
      {{ row.usedFlag === 1 ? '已占用' : '未占用' }}
    </el-tag>
  </template>
</el-table-column>
```

- [ ] **Step 8: Run frontend test(运行前端测试)**

Run:

```bash
cd frontend-admin
pnpm vitest run tests/unit/internal-test-workbench-source.test.ts
```

Expected(预期): PASS(通过).

- [ ] **Step 9: Commit(提交)**

Run:

```bash
git add frontend-admin/src/views/internal-test/resource/index.vue \
  frontend-admin/tests/unit/internal-test-workbench-source.test.ts
git commit -m "feat: simplify internal resource import"
```

---

### Task 8: 前端内测数据生成工作台

**Files(文件):**

- Modify: `frontend-admin/src/views/internal-test/generator/index.vue`
- Modify: `frontend-admin/tests/unit/internal-test-workbench-source.test.ts`

- [ ] **Step 1: Extend source test for generator page(扩展生成页源代码测试)**

Add this test to `internal-test-workbench-source.test.ts`:

```ts
it('生成页提供新增用户、新增作品、新增约拍三类动作和内测用户搜索', () => {
  const page = source('src/views/internal-test/generator/index.vue')

  expect(page).toContain('InternalTestGenerateAction')
  expect(page).toContain('generateInternalUsers')
  expect(page).toContain('generateInternalWorks')
  expect(page).toContain('generateInternalYpats')
  expect(page).toContain('searchInternalUsers')
  expect(page).toContain('getInternalResourceGroups')
  expect(page).toContain('微信号')
  expect(page).toContain('联系电话')
  expect(page).not.toContain('模板类型')
})
```

- [ ] **Step 2: Run failing test(运行失败测试)**

Run:

```bash
cd frontend-admin
pnpm vitest run tests/unit/internal-test-workbench-source.test.ts
```

Expected(预期): FAIL(失败).

- [ ] **Step 3: Replace mode/content/template state with action state(用动作状态替代模式/内容/模板状态)**

In `generator/index.vue`, replace `mode/contentType/templateType` usage with:

```ts
const form = reactive({
  actionType: InternalTestGenerateAction.CREATE_USERS.value,
  userCount: 5,
  userId: undefined as number | undefined,
  nicknamePrefix: '',
  gender: Gender.FEMALE.value,
  profess: '',
  province: '',
  city: '',
  area: '',
  styleCodes: [] as string[],
  publishStatus: '1',
  groupNos: [] as string[],
  patdate: '',
  patslice: '',
  describ: '',
  target: YpatTarget.PHOTOGRAPHER.value,
  wx: '',
  mobile: '',
})
```

Add computed helpers:

```ts
const isCreateUsers = computed(() => form.actionType === InternalTestGenerateAction.CREATE_USERS.value)
const isCreateWorks = computed(() => form.actionType === InternalTestGenerateAction.CREATE_WORKS.value)
const isCreateYpats = computed(() => form.actionType === InternalTestGenerateAction.CREATE_YPATS.value)
```

- [ ] **Step 4: Add remote user search(增加远程用户搜索)**

Add state:

```ts
const userOptions = ref<InternalTestUser[]>([])
const userSearching = ref(false)
```

Add method:

```ts
async function remoteSearchUsers(keyword: string): Promise<void> {
  userSearching.value = true
  try {
    const res = await searchInternalUsers({ keyword, page: 0, size: 20 })
    userOptions.value = res.data.content || []
  } finally {
    userSearching.value = false
  }
}
```

Add template select:

```vue
<el-form-item v-if="!isCreateUsers" label="内测用户">
  <el-select
    v-model="form.userId"
    filterable
    remote
    :remote-method="remoteSearchUsers"
    :loading="userSearching"
    placeholder="搜索用户ID、昵称或手机号"
    style="width: 260px"
  >
    <el-option
      v-for="user in userOptions"
      :key="user.id"
      :label="`${user.nickname || '-'} / ${user.mobile || '-'} / ${user.id}`"
      :value="user.id"
    />
  </el-select>
</el-form-item>
```

- [ ] **Step 5: Add work group loading(增加作品组加载)**

Add:

```ts
const workGroups = ref<InternalTestResourceGroup[]>([])

async function loadWorkGroups(): Promise<void> {
  const res = await getInternalResourceGroups({
    mediaType: activeWorkMediaType.value,
    usageType: InternalTestUsageType.WORK.value,
    styleCode: form.styleCodes[0],
    usedFlag: InternalTestResourceUsedFlag.UNUSED.value,
    status: InternalTestResourceStatus.ENABLED.value,
    page: 0,
    size: 50,
  })
  workGroups.value = res.data.content || []
}
```

Add template multi-select:

```vue
<el-form-item v-if="isCreateWorks" label="作品组">
  <el-select v-model="form.groupNos" multiple filterable placeholder="请选择未占用作品组" style="width: 320px">
    <el-option
      v-for="group in workGroups"
      :key="group.groupNo"
      :label="`${group.groupTitle || group.groupNo}（${group.resources?.length || 0}个资源）`"
      :value="group.groupNo"
    />
  </el-select>
</el-form-item>
```

- [ ] **Step 6: Add ypat real fields(增加约拍真实字段)**

Add template controls shown only for `isCreateYpats`:

```vue
<el-form-item v-if="isCreateYpats" label="时间">
  <el-date-picker v-model="form.patdate" type="date" value-format="YYYY-MM-DD" placeholder="请选择约拍日期" />
</el-form-item>
<el-form-item v-if="isCreateYpats" label="要求">
  <el-input v-model="form.describ" type="textarea" :rows="3" maxlength="200" show-word-limit />
</el-form-item>
<el-form-item v-if="isCreateYpats" label="微信号">
  <el-input v-model="form.wx" maxlength="40" />
</el-form-item>
<el-form-item v-if="isCreateYpats" label="联系电话">
  <el-input v-model="form.mobile" maxlength="11" />
</el-form-item>
<el-form-item v-if="isCreateYpats" label="约拍对象">
  <el-select v-model="form.target" style="width: 240px">
    <el-option v-for="option in getYpatTargetOptions()" :key="option.value" :label="option.label" :value="option.value" />
  </el-select>
</el-form-item>
```

- [ ] **Step 7: Route submit to split APIs(按动作提交到拆分接口)**

Replace submit call:

```ts
async function submitGenerate(): Promise<void> {
  const message = validateBeforeSubmit()
  if (message) {
    ElMessage.warning(message)
    return
  }
  await ElMessageBox.confirm('确认提交后仅生成内测数据，不会标记为真实用户数据。', '生成确认', {
    type: 'warning',
    confirmButtonText: '确认生成',
    cancelButtonText: '取消',
  })
  submitting.value = true
  try {
    const payload = buildPayload()
    const res = isCreateUsers.value
      ? await generateInternalUsers(payload)
      : isCreateWorks.value
        ? await generateInternalWorks(payload)
        : await generateInternalYpats(payload)
    ElMessage.success(`生成成功，批次号：${res.data.batchNo}`)
    batchQuery.batchNo = res.data.batchNo
    await loadBatches()
  } finally {
    submitting.value = false
  }
}
```

Validation must require:

```ts
if (isCreateWorks.value && !form.userId) return '请选择内测用户'
if (isCreateWorks.value && form.groupNos.length === 0) return '请选择作品组'
if (isCreateYpats.value && !form.userId) return '请选择内测用户'
if (isCreateYpats.value && !form.patdate) return '请选择约拍时间'
if (isCreateYpats.value && !form.city) return '请选择约拍地点'
if (isCreateYpats.value && !form.describ.trim()) return '请输入约拍要求'
if (isCreateYpats.value && !form.wx.trim()) return '请输入微信号'
if (isCreateYpats.value && !form.mobile.trim()) return '请输入联系电话'
if (isCreateYpats.value && form.styleCodes.length === 0) return '请选择风格'
```

- [ ] **Step 8: Run source test and type check(运行源代码测试和类型检查)**

Run:

```bash
cd frontend-admin
pnpm vitest run tests/unit/internal-test-workbench-source.test.ts
pnpm vue-tsc --noEmit
```

Expected(预期): PASS(通过). Failure blocks this task and must be fixed before commit(提交).

- [ ] **Step 9: Commit(提交)**

Run:

```bash
git add frontend-admin/src/views/internal-test/generator/index.vue \
  frontend-admin/tests/unit/internal-test-workbench-source.test.ts
git commit -m "feat: redesign internal data generator"
```

---

### Task 9: 前端业务列表内测列、筛选和专属按钮

**Files(文件):**

- Modify: `frontend-admin/src/views/query/user-list/index.vue`
- Modify: `frontend-admin/src/views/query/ypat-list/index.vue`
- Modify: `frontend-admin/src/views/manage/work-list/index.vue`
- Modify: `frontend-admin/tests/unit/internal-test-workbench-source.test.ts`

- [ ] **Step 1: Extend source tests for business lists(扩展业务列表源代码测试)**

Add:

```ts
it('用户作品约拍列表展示内测数据列和筛选，用户列表展示内测专属按钮', () => {
  const user = source('src/views/query/user-list/index.vue')
  const ypat = source('src/views/query/ypat-list/index.vue')
  const work = source('src/views/manage/work-list/index.vue')

  for (const page of [user, ypat, work]) {
    expect(page).toContain('dataFlag')
    expect(page).toContain('内测数据')
    expect(page).toContain('InternalTestDataFlag')
  }
  expect(user).toContain('grantInternalUserMember')
  expect(user).toContain('verifyInternalUser')
  expect(user).toContain('markInternalUserDepositPaid')
  expect(user).toContain('row.dataFlag === InternalTestDataFlag.INTERNAL_TEST.value')
})
```

- [ ] **Step 2: Run failing test(运行失败测试)**

Run:

```bash
cd frontend-admin
pnpm vitest run tests/unit/internal-test-workbench-source.test.ts
```

Expected(预期): FAIL(失败).

- [ ] **Step 3: Add dataFlag filter to user list(用户列表增加筛选和列)**

In `query/user-list/index.vue`:

Import:

```ts
import {
  InternalTestDataFlag,
  getInternalTestDataFlagOptions,
} from '@/constants/enums'
import {
  grantInternalUserMember,
  markInternalUserDepositPaid,
  verifyInternalUser,
} from '@/api/modules/internal-test'
import { ElMessage, ElMessageBox } from 'element-plus'
```

Extend query:

```ts
dataFlag: '',
```

Add filter:

```vue
<el-form-item label="内测数据">
  <el-select v-model="query.dataFlag" clearable placeholder="全部" style="width: 140px">
    <el-option v-for="option in getInternalTestDataFlagOptions()" :key="option.value" :label="option.label" :value="option.value" />
  </el-select>
</el-form-item>
```

Add table column:

```vue
<el-table-column label="内测数据" width="100" align="center">
  <template #default="{ row }">
    <el-tag v-if="row.dataFlag === InternalTestDataFlag.INTERNAL_TEST.value" type="warning" size="small">是</el-tag>
    <span v-else>否</span>
  </template>
</el-table-column>
```

Add action helpers:

```ts
function isInternalUser(row: OauthQo): boolean {
  return row.dataFlag === InternalTestDataFlag.INTERNAL_TEST.value
}

async function runInternalUserAction(row: OauthQo, action: 'member' | 'verify' | 'deposit'): Promise<void> {
  const userId = getUserId(row)
  if (!userId || !isInternalUser(row)) return
  const titleMap = { member: '一键设置会员', verify: '一键认证', deposit: '一键设置保证金' }
  await ElMessageBox.confirm(`确认对内测用户 #${userId} 执行${titleMap[action]}吗？本操作仅影响内测数据。`, '内测操作确认', {
    type: 'warning',
    confirmButtonText: '确认执行',
    cancelButtonText: '取消',
  })
  if (action === 'member') await grantInternalUserMember(userId, { days: 365, reason: '内测数据一键会员' })
  if (action === 'verify') await verifyInternalUser(userId, { reason: '内测数据一键认证' })
  if (action === 'deposit') await markInternalUserDepositPaid(userId, { reason: '内测数据一键保证金' })
  ElMessage.success('操作成功')
  await fetchList()
}
```

Add buttons in operation column:

```vue
<el-button v-if="isInternalUser(row as OauthQo)" type="success" link size="small" @click="runInternalUserAction(row as OauthQo, 'member')">会员</el-button>
<el-button v-if="isInternalUser(row as OauthQo)" type="warning" link size="small" @click="runInternalUserAction(row as OauthQo, 'verify')">认证</el-button>
<el-button v-if="isInternalUser(row as OauthQo)" type="danger" link size="small" @click="runInternalUserAction(row as OauthQo, 'deposit')">保证金</el-button>
```

- [ ] **Step 4: Add dataFlag column/filter to ypat list(约拍列表增加列和筛选)**

In `query/ypat-list/index.vue`, import `InternalTestDataFlag/getInternalTestDataFlagOptions`, extend query with `dataFlag`, add the same filter and table column.

- [ ] **Step 5: Add dataFlag column/filter to work list(作品列表增加列和筛选)**

In `manage/work-list/index.vue`, import `InternalTestDataFlag/getInternalTestDataFlagOptions`, extend `queryParams` with `dataFlag`, add the same filter and table column.

- [ ] **Step 6: Run tests and type check(运行测试和类型检查)**

Run:

```bash
cd frontend-admin
pnpm vitest run tests/unit/internal-test-workbench-source.test.ts
pnpm vue-tsc --noEmit
```

Expected(预期): PASS(通过). Failure blocks this task and must be fixed before commit(提交).

- [ ] **Step 7: Commit(提交)**

Run:

```bash
git add frontend-admin/src/views/query/user-list/index.vue \
  frontend-admin/src/views/query/ypat-list/index.vue \
  frontend-admin/src/views/manage/work-list/index.vue \
  frontend-admin/tests/unit/internal-test-workbench-source.test.ts
git commit -m "feat: mark internal test business lists"
```

---

### Task 10: Final verification(最终验证)

**Files(文件):**

- Review only: all files changed in Tasks 1-9.

- [ ] **Step 1: Run backend focused tests(运行后端聚焦测试)**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
cd ../system-wap
mvn test -Dtest=AdminInternalTestControllerSourceTest
```

Expected(预期): PASS(通过).

- [ ] **Step 2: Run frontend focused tests(运行前端聚焦测试)**

Run:

```bash
cd frontend-admin
pnpm vitest run tests/unit/internal-test-api.test.ts tests/unit/admin-enums.test.ts tests/unit/internal-test-workbench-source.test.ts tests/unit/permission.test.ts
```

Expected(预期): PASS(通过).

- [ ] **Step 3: Run frontend type check(运行前端类型检查)**

Run:

```bash
cd frontend-admin
pnpm vue-tsc --noEmit
```

Expected(预期): PASS(通过). Failure blocks completion and must be fixed before final summary(最终总结).

- [ ] **Step 4: Confirm old admin untouched(确认旧后台未改动)**

Run:

```bash
git diff --name-only HEAD~9..HEAD | rg '^backend/system-web/' || true
```

Expected(预期): no output(无输出).

- [ ] **Step 5: Confirm working tree only has intentional changes(确认工作区只剩有意改动)**

Run:

```bash
git status --short
```

Expected(预期): no changed files from this implementation. 当前已有的 `.omx/state/session.json` 修改可以保留，但不得提交。

- [ ] **Step 6: Final summary(最终总结)**

Prepare a concise Chinese summary including:

```text
已完成：
- 内测资源批量导入、作品组、占用释放。
- 新增用户/作品/约拍三类生成入口。
- 用户/作品/约拍列表内测列和筛选。
- 内测用户一键会员/认证/保证金，后端强校验真实数据不可操作。

验证：
- backend/system-domain: mvn test -Dtest=InternalTestDataSourceTest
- backend/system-wap: mvn test -Dtest=AdminInternalTestControllerSourceTest
- frontend-admin: pnpm vitest run ...
- frontend-admin: pnpm vue-tsc --noEmit
```
