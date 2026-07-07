# Admin Internal Test Data Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a standalone internal test data factory for the new Vue admin so operators can manage media resources, create internal-test users, generate appointment/work content, and safely soft-clean only internal-test data.

**Architecture:** Add explicit internal-test markers to the user, appointment, and work domains, plus one resource table for image/video URLs. Follow the existing backend layering: `system-wap` admin endpoints call Feign clients, `system-restapi` exposes internal service endpoints, and `system-domain` owns persistence and generation logic. Add two new `frontend-admin` pages under a new `内测数据` menu group without modifying `backend/system-web`.

**Tech Stack:** Java 8, Spring Boot 1.5, Spring Data JPA, Feign, MySQL, Vue 3, TypeScript, Pinia, Vue Router, Element Plus, Vitest, Maven.

---

## Spec Source

- Design: `docs/superpowers/specs/2026-07-06-admin-internal-test-data-design.md`
- Worktree: `/Users/lizhenwei/workspace/vueworkspace/ypat-workspace/.worktrees/codex-admin-internal-seeding`
- Branch: `codex/admin-internal-seeding`

## File Structure

Database and docs:

- Create `docs/sql/pending/V_admin_internal_test_data.sql`: idempotent MySQL migration for `t_internal_test_resource`, `data_flag`, and `internal_batch_no`.

Backend object module:

- Create `backend/system-object/src/main/java/com/ypat/enums/InternalTestDataFlag.java`: constants for `real` and `internal_test`.
- Create `backend/system-object/src/main/java/com/ypat/enums/InternalTestResourceMediaType.java`: `image` and `video`.
- Create `backend/system-object/src/main/java/com/ypat/enums/InternalTestResourceUsageType.java`: `avatar`, `ypat`, and `work`.
- Create `backend/system-object/src/main/java/com/ypat/enums/InternalTestResourceStatus.java`: `enabled` and `disabled`.
- Create `backend/system-object/src/main/java/com/ypat/InternalTestResourceQo.java`: resource list/edit DTO.
- Create `backend/system-object/src/main/java/com/ypat/InternalTestGenerateQo.java`: generation request DTO.
- Create `backend/system-object/src/main/java/com/ypat/InternalTestBatchQo.java`: batch result/list DTO.
- Modify `backend/system-object/src/main/java/com/ypat/UserQo.java`: add `dataFlag` and `internalBatchNo`.
- Modify `backend/system-object/src/main/java/com/ypat/YpatInfoQo.java`: add `dataFlag` and `internalBatchNo`.
- Modify `backend/system-object/src/main/java/com/ypat/WorkListQo.java`: add `dataFlag` and `internalBatchNo`.

Backend domain module:

- Create `backend/system-domain/src/main/java/com/ypat/entity/InternalTestResource.java`.
- Modify `backend/system-domain/src/main/java/com/ypat/entity/User.java`: add `dataFlag` and `internalBatchNo`.
- Modify `backend/system-domain/src/main/java/com/ypat/entity/YpatInfo.java`: add `dataFlag` and `internalBatchNo`.
- Modify `backend/system-domain/src/main/java/com/ypat/entity/Work.java`: add `dataFlag` and `internalBatchNo`.
- Create `backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java`.
- Modify `backend/system-domain/src/main/java/com/ypat/repository/UserRepository.java`: add internal-test soft cleanup update.
- Modify `backend/system-domain/src/main/java/com/ypat/repository/YpatInfoRepository.java`: add internal-test soft cleanup update.
- Modify `backend/system-domain/src/main/java/com/ypat/repository/WorkRepository.java`: add internal-test soft cleanup update.
- Create `backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java`.
- Create `backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java`.
- Create `backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java`.

Backend restapi and wap modules:

- Create `backend/system-restapi/src/main/java/com/ypat/controller/InternalTestController.java`.
- Create `backend/system-wap/src/main/java/com/ypat/service/InternalTestServiceClient.java`.
- Create `backend/system-wap/src/main/java/com/ypat/controller/AdminInternalTestController.java`.
- Create `backend/system-wap/src/test/java/com/ypat/controller/AdminInternalTestControllerSourceTest.java`.

Frontend admin:

- Create `frontend-admin/src/api/modules/internal-test.ts`.
- Modify `frontend-admin/src/constants/enums.ts`: add internal-test media, usage, status, data flag, and generation mode enums.
- Modify `frontend-admin/src/constants/menu.ts`: add `内测数据` menu group.
- Modify `frontend-admin/src/stores/modules/permission.ts`: register internal-test pages.
- Create `frontend-admin/src/views/internal-test/resource/index.vue`.
- Create `frontend-admin/src/views/internal-test/generator/index.vue`.
- Create `frontend-admin/tests/unit/internal-test-api.test.ts`.
- Modify `frontend-admin/tests/unit/admin-enums.test.ts`.
- Modify `frontend-admin/tests/unit/permission.test.ts`.

## Task 1: Database, Object DTOs, and Entity Markers

**Files:**

- Create: `docs/sql/pending/V_admin_internal_test_data.sql`
- Create: `backend/system-object/src/main/java/com/ypat/enums/InternalTestDataFlag.java`
- Create: `backend/system-object/src/main/java/com/ypat/enums/InternalTestResourceMediaType.java`
- Create: `backend/system-object/src/main/java/com/ypat/enums/InternalTestResourceUsageType.java`
- Create: `backend/system-object/src/main/java/com/ypat/enums/InternalTestResourceStatus.java`
- Create: `backend/system-object/src/main/java/com/ypat/InternalTestResourceQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/InternalTestGenerateQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/InternalTestBatchQo.java`
- Modify: `backend/system-object/src/main/java/com/ypat/UserQo.java`
- Modify: `backend/system-object/src/main/java/com/ypat/YpatInfoQo.java`
- Modify: `backend/system-object/src/main/java/com/ypat/WorkListQo.java`
- Create: `backend/system-domain/src/main/java/com/ypat/entity/InternalTestResource.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/entity/User.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/entity/YpatInfo.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/entity/Work.java`
- Test: `backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java`

- [ ] **Step 1: Write the source regression test**

Create `backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java`:

```java
package com.ypat.service;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class InternalTestDataSourceTest {
    private static String read(String path) throws IOException {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            p = Paths.get("../..").resolve(path).normalize();
        }
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    @Test
    public void migrationCreatesResourceTableAndMainTableMarkers() throws Exception {
        String sql = read("docs/sql/pending/V_admin_internal_test_data.sql");

        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS `t_internal_test_resource`"));
        assertTrue(sql.contains("`media_type`"));
        assertTrue(sql.contains("`usage_type`"));
        assertTrue(sql.contains("`style_code`"));
        assertTrue(sql.contains("`url`"));
        assertTrue(sql.contains("ADD COLUMN `data_flag`"));
        assertTrue(sql.contains("ADD COLUMN `internal_batch_no`"));
        assertTrue(sql.contains("ALTER TABLE `t_user`"));
        assertTrue(sql.contains("ALTER TABLE `t_ypat_info`"));
        assertTrue(sql.contains("ALTER TABLE `t_work`"));
    }

    @Test
    public void entitiesExposeInternalTestMarkersAndResourceEntity() throws Exception {
        String user = read("backend/system-domain/src/main/java/com/ypat/entity/User.java");
        String ypat = read("backend/system-domain/src/main/java/com/ypat/entity/YpatInfo.java");
        String work = read("backend/system-domain/src/main/java/com/ypat/entity/Work.java");
        String resource = read("backend/system-domain/src/main/java/com/ypat/entity/InternalTestResource.java");

        assertTrue(user.contains("private String dataFlag;"));
        assertTrue(user.contains("private String internalBatchNo;"));
        assertTrue(ypat.contains("private String dataFlag;"));
        assertTrue(ypat.contains("private String internalBatchNo;"));
        assertTrue(work.contains("private String dataFlag;"));
        assertTrue(work.contains("private String internalBatchNo;"));
        assertTrue(resource.contains("@Table(name = \"t_internal_test_resource\")"));
        assertTrue(resource.contains("private String mediaType;"));
        assertTrue(resource.contains("private String usageType;"));
        assertTrue(resource.contains("private String styleCode;"));
        assertTrue(resource.contains("private String url;"));
    }

    @Test
    public void objectDtosExposeGenerationAndResourceContracts() throws Exception {
        String resourceQo = read("backend/system-object/src/main/java/com/ypat/InternalTestResourceQo.java");
        String generateQo = read("backend/system-object/src/main/java/com/ypat/InternalTestGenerateQo.java");
        String batchQo = read("backend/system-object/src/main/java/com/ypat/InternalTestBatchQo.java");
        String userQo = read("backend/system-object/src/main/java/com/ypat/UserQo.java");
        String ypatQo = read("backend/system-object/src/main/java/com/ypat/YpatInfoQo.java");
        String workQo = read("backend/system-object/src/main/java/com/ypat/WorkListQo.java");

        assertTrue(resourceQo.contains("private String mediaType;"));
        assertTrue(resourceQo.contains("private String usageType;"));
        assertTrue(resourceQo.contains("private String styleCode;"));
        assertTrue(resourceQo.contains("private String status;"));
        assertTrue(generateQo.contains("private String mode;"));
        assertTrue(generateQo.contains("private Integer userCount;"));
        assertTrue(generateQo.contains("private String publishStatus;"));
        assertTrue(generateQo.contains("private java.util.List<Long> userIds;"));
        assertTrue(batchQo.contains("private String batchNo;"));
        assertTrue(batchQo.contains("private Integer userCount;"));
        assertTrue(batchQo.contains("private Integer ypatCount;"));
        assertTrue(batchQo.contains("private Integer workCount;"));
        assertTrue(userQo.contains("private String dataFlag;"));
        assertTrue(ypatQo.contains("private String dataFlag;"));
        assertTrue(workQo.contains("private String dataFlag;"));
    }
}
```

- [ ] **Step 2: Run the failing source test**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
```

Expected: fail because the SQL, DTOs, and entity fields do not exist yet.

- [ ] **Step 3: Add the SQL migration**

Create `docs/sql/pending/V_admin_internal_test_data.sql` with idempotent DDL. Use guarded `information_schema` checks like existing pending SQL files use for repeatability:

```sql
-- 新版管理后台内测数据工厂
-- 重复执行安全：表使用 CREATE TABLE IF NOT EXISTS；列通过 information_schema 判断后再添加。

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `t_internal_test_resource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `media_type` VARCHAR(20) NOT NULL COMMENT '媒体类型：image/video',
  `usage_type` VARCHAR(20) NOT NULL COMMENT '用途：avatar/ypat/work',
  `style_code` VARCHAR(20) DEFAULT NULL COMMENT '风格编码',
  `url` VARCHAR(1024) NOT NULL COMMENT '资源 URL',
  `title` VARCHAR(100) DEFAULT NULL COMMENT '标题',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `profession` VARCHAR(20) DEFAULT NULL COMMENT '职业',
  `city` VARCHAR(100) DEFAULT NULL COMMENT '城市',
  `status` VARCHAR(20) NOT NULL DEFAULT 'enabled' COMMENT '状态：enabled/disabled',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_internal_resource_query` (`media_type`, `usage_type`, `style_code`, `status`, `sort_no`),
  KEY `idx_internal_resource_city` (`city`),
  KEY `idx_internal_resource_profession` (`profession`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内测资源表';

SET @sql := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_user' AND COLUMN_NAME = 'data_flag') = 0,
  'ALTER TABLE `t_user` ADD COLUMN `data_flag` VARCHAR(20) NOT NULL DEFAULT ''real'' COMMENT ''数据标识：real/internal_test''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_user' AND COLUMN_NAME = 'internal_batch_no') = 0,
  'ALTER TABLE `t_user` ADD COLUMN `internal_batch_no` VARCHAR(64) DEFAULT NULL COMMENT ''内测批次号''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_ypat_info' AND COLUMN_NAME = 'data_flag') = 0,
  'ALTER TABLE `t_ypat_info` ADD COLUMN `data_flag` VARCHAR(20) NOT NULL DEFAULT ''real'' COMMENT ''数据标识：real/internal_test''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_ypat_info' AND COLUMN_NAME = 'internal_batch_no') = 0,
  'ALTER TABLE `t_ypat_info` ADD COLUMN `internal_batch_no` VARCHAR(64) DEFAULT NULL COMMENT ''内测批次号''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_work' AND COLUMN_NAME = 'data_flag') = 0,
  'ALTER TABLE `t_work` ADD COLUMN `data_flag` VARCHAR(20) NOT NULL DEFAULT ''real'' COMMENT ''数据标识：real/internal_test''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_work' AND COLUMN_NAME = 'internal_batch_no') = 0,
  'ALTER TABLE `t_work` ADD COLUMN `internal_batch_no` VARCHAR(64) DEFAULT NULL COMMENT ''内测批次号''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
```

- [ ] **Step 4: Add enums and DTOs**

Create the four enum files under `backend/system-object/src/main/java/com/ypat/enums/`. Each enum should expose `value`, `name`, `getNameByCode(String code)`, and simple `isValid(String code)` where validation is needed. Use these values:

```java
// InternalTestDataFlag: real / internal_test
// InternalTestResourceMediaType: image / video
// InternalTestResourceUsageType: avatar / ypat / work
// InternalTestResourceStatus: enabled / disabled
```

Create `InternalTestResourceQo`, `InternalTestGenerateQo`, and `InternalTestBatchQo` as JavaBean DTOs with getters and setters. Required fields:

```java
// InternalTestResourceQo
Long id; String mediaType; String usageType; String styleCode; String url; String title;
String description; String profession; String city; String status; Integer sortNo;
String remark; java.util.Date createdAt; java.util.Date updatedAt; String keyword;
Integer page; Integer size;

// InternalTestGenerateQo
String mode; Integer userCount; java.util.List<Long> userIds; String nicknamePrefix;
String gender; String profess; String province; String city; String area; String styleCode;
String contentType; String templateType; String publishStatus; java.util.List<Long> avatarResourceIds;
java.util.List<Long> ypatResourceIds; java.util.List<Long> workResourceIds; String batchNo;

// InternalTestBatchQo
String batchNo; Integer userCount; Integer ypatCount; Integer workCount; String status;
java.util.List<String> errors; java.util.Date createdAt;
```

- [ ] **Step 5: Add marker fields to existing DTOs and entities**

Add `private String dataFlag;` and `private String internalBatchNo;` with getters and setters to:

```text
backend/system-object/src/main/java/com/ypat/UserQo.java
backend/system-object/src/main/java/com/ypat/YpatInfoQo.java
backend/system-object/src/main/java/com/ypat/WorkListQo.java
backend/system-domain/src/main/java/com/ypat/entity/User.java
backend/system-domain/src/main/java/com/ypat/entity/YpatInfo.java
backend/system-domain/src/main/java/com/ypat/entity/Work.java
```

For entity fields, map snake_case columns explicitly:

```java
@Column(name = "data_flag")
private String dataFlag;

@Column(name = "internal_batch_no")
private String internalBatchNo;
```

- [ ] **Step 6: Create `InternalTestResource` entity**

Create `backend/system-domain/src/main/java/com/ypat/entity/InternalTestResource.java` with fields mirroring the SQL table. Use `@Entity`, `@Table(name = "t_internal_test_resource")`, `@DynamicInsert`, and `@DynamicUpdate`.

- [ ] **Step 7: Run the source test**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
```

Expected: pass.

- [ ] **Step 8: Commit**

Run:

```bash
git add docs/sql/pending/V_admin_internal_test_data.sql \
  backend/system-object/src/main/java/com/ypat/enums/InternalTestDataFlag.java \
  backend/system-object/src/main/java/com/ypat/enums/InternalTestResourceMediaType.java \
  backend/system-object/src/main/java/com/ypat/enums/InternalTestResourceUsageType.java \
  backend/system-object/src/main/java/com/ypat/enums/InternalTestResourceStatus.java \
  backend/system-object/src/main/java/com/ypat/InternalTestResourceQo.java \
  backend/system-object/src/main/java/com/ypat/InternalTestGenerateQo.java \
  backend/system-object/src/main/java/com/ypat/InternalTestBatchQo.java \
  backend/system-object/src/main/java/com/ypat/UserQo.java \
  backend/system-object/src/main/java/com/ypat/YpatInfoQo.java \
  backend/system-object/src/main/java/com/ypat/WorkListQo.java \
  backend/system-domain/src/main/java/com/ypat/entity/InternalTestResource.java \
  backend/system-domain/src/main/java/com/ypat/entity/User.java \
  backend/system-domain/src/main/java/com/ypat/entity/YpatInfo.java \
  backend/system-domain/src/main/java/com/ypat/entity/Work.java \
  backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java
git commit -m "feat: add internal test data schema"
```

## Task 2: Domain Resource and Generation Services

**Files:**

- Create: `backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/UserRepository.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/YpatInfoRepository.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/WorkRepository.java`
- Create: `backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java`
- Create: `backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java`
- Modify: `backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java`

- [ ] **Step 1: Extend the source regression test**

Append tests to `InternalTestDataSourceTest`:

```java
@Test
public void domainServicesProtectRealDataAndGenerateMarkedRecords() throws Exception {
    String dataService = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java");
    String resourceService = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java");
    String userRepo = read("backend/system-domain/src/main/java/com/ypat/repository/UserRepository.java");
    String ypatRepo = read("backend/system-domain/src/main/java/com/ypat/repository/YpatInfoRepository.java");
    String workRepo = read("backend/system-domain/src/main/java/com/ypat/repository/WorkRepository.java");

    assertTrue(resourceService.contains("public Map<String, Object> page(InternalTestResourceQo qo)"));
    assertTrue(resourceService.contains("public InternalTestResourceQo save(InternalTestResourceQo qo)"));
    assertTrue(resourceService.contains("public void updateStatus(Long id, String status)"));
    assertTrue(dataService.contains("public InternalTestBatchQo createUsers(InternalTestGenerateQo qo)"));
    assertTrue(dataService.contains("public InternalTestBatchQo generate(InternalTestGenerateQo qo)"));
    assertTrue(dataService.contains("public InternalTestBatchQo cleanup(InternalTestGenerateQo qo)"));
    assertTrue(dataService.contains("InternalTestDataFlag.internalTest.value"));
    assertTrue(dataService.contains("ensureResources"));
    assertTrue(dataService.contains("buildBatchNo"));
    assertTrue(userRepo.contains("updateInternalTestUsersStatus"));
    assertTrue(ypatRepo.contains("updateInternalTestYpatStatus"));
    assertTrue(workRepo.contains("updateInternalTestWorkStatus"));
    assertTrue(userRepo.contains("dataFlag = 'internal_test'"));
    assertTrue(ypatRepo.contains("dataFlag = 'internal_test'"));
    assertTrue(workRepo.contains("dataFlag = 'internal_test'"));
}
```

Run it and confirm failure.

- [ ] **Step 2: Create the resource repository**

Create `InternalTestResourceRepository`:

```java
package com.ypat.repository;

import com.ypat.entity.InternalTestResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternalTestResourceRepository extends JpaRepository<InternalTestResource, Long>, JpaSpecificationExecutor<InternalTestResource> {
    List<InternalTestResource> findByIdInAndStatus(List<Long> ids, String status);
}
```

- [ ] **Step 3: Add soft cleanup repository methods**

Add `@Modifying` update methods:

```java
// UserRepository
@Modifying
@Query("update User u set u.status = :status where u.dataFlag = 'internal_test' and (:batchNo is null or u.internalBatchNo = :batchNo)")
int updateInternalTestUsersStatus(@Param("batchNo") String batchNo, @Param("status") String status);

// YpatInfoRepository
@Modifying
@Query("update YpatInfo y set y.status = :status, y.reason = :reason where y.dataFlag = 'internal_test' and (:batchNo is null or y.internalBatchNo = :batchNo)")
int updateInternalTestYpatStatus(@Param("batchNo") String batchNo, @Param("status") String status, @Param("reason") String reason);

// WorkRepository
@Modifying
@Query("update Work w set w.status = :status, w.auditReason = :reason, w.updatedAt = CURRENT_TIMESTAMP where w.dataFlag = 'internal_test' and w.deletedFlag = 0 and (:batchNo is null or w.internalBatchNo = :batchNo)")
int updateInternalTestWorkStatus(@Param("batchNo") String batchNo, @Param("status") String status, @Param("reason") String reason);
```

- [ ] **Step 4: Implement `InternalTestResourceService`**

Implement list/save/status methods using `Specification<InternalTestResource>` with filters from `InternalTestResourceQo`: `mediaType`, `usageType`, `styleCode`, `profession`, `city`, `status`, `keyword`. Validate `mediaType`, `usageType`, `status`, and nonblank `url` before saving. Return page maps with `content`, `totalPages`, and `totalElements`.

- [ ] **Step 5: Implement `InternalTestDataService`**

Implement:

```java
public InternalTestBatchQo createUsers(InternalTestGenerateQo qo)
public InternalTestBatchQo generate(InternalTestGenerateQo qo)
public Map<String, Object> listUsers(InternalTestGenerateQo qo)
public Map<String, Object> listBatches(InternalTestGenerateQo qo)
public InternalTestBatchQo cleanup(InternalTestGenerateQo qo)
```

Required behavior:

- `userCount` defaults are rejected; caller must provide `1..50` when creating new users.
- Existing-user generation only accepts users with `dataFlag=internal_test`.
- `buildBatchNo()` returns `IT` + `yyyyMMddHHmmss` + 4 random digits.
- Generated users set `dataFlag=internal_test`, `internalBatchNo=batchNo`, `status=UserStatus.shtg.value`, `realnameflag=yes`, `creditflag=yes`, `ppd=1000`, and an internal-test mobile.
- Generated appointments create `YpatInfo` plus `YpatImg` rows, with `dataFlag=internal_test`, `internalBatchNo=batchNo`, `status=publishStatus`, and `target` from the selected template.
- Generated works create `Work` plus `WorkMedia` rows, with `dataFlag=internal_test`, `internalBatchNo=batchNo`, `status=publishStatus`, `deletedFlag=0`, and media rows copied from resources.
- `ensureResources` rejects missing enabled resources for requested usages.
- `cleanup` calls only the internal-test repository update methods.

- [ ] **Step 6: Run the domain source test**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
```

Expected: pass.

- [ ] **Step 7: Commit**

Run:

```bash
git add backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java \
  backend/system-domain/src/main/java/com/ypat/repository/UserRepository.java \
  backend/system-domain/src/main/java/com/ypat/repository/YpatInfoRepository.java \
  backend/system-domain/src/main/java/com/ypat/repository/WorkRepository.java \
  backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java \
  backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java \
  backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java
git commit -m "feat: add internal test data services"
```

## Task 3: Restapi and New Admin WAP Endpoints

**Files:**

- Create: `backend/system-restapi/src/main/java/com/ypat/controller/InternalTestController.java`
- Create: `backend/system-wap/src/main/java/com/ypat/service/InternalTestServiceClient.java`
- Create: `backend/system-wap/src/main/java/com/ypat/controller/AdminInternalTestController.java`
- Create: `backend/system-wap/src/test/java/com/ypat/controller/AdminInternalTestControllerSourceTest.java`

- [ ] **Step 1: Write the WAP source test**

Create `AdminInternalTestControllerSourceTest`:

```java
package com.ypat.controller;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminInternalTestControllerSourceTest {
    private String readSource(String modulePath, String repoPath) throws IOException {
        Path path = Paths.get(modulePath);
        if (!Files.exists(path)) path = Paths.get(repoPath);
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    @Test
    public void wapControllerExposesNewAdminInternalTestRoutesOnly() throws Exception {
        String source = readSource(
                "src/main/java/com/ypat/controller/AdminInternalTestController.java",
                "backend/system-wap/src/main/java/com/ypat/controller/AdminInternalTestController.java");

        assertTrue(source.contains("@RequestMapping(\"/admin/internal-test\")"));
        assertTrue(source.contains("@GetMapping(\"/resources\")"));
        assertTrue(source.contains("@PostMapping(\"/resources\")"));
        assertTrue(source.contains("@PostMapping(\"/resources/update\")"));
        assertTrue(source.contains("@PostMapping(\"/resources/status\")"));
        assertTrue(source.contains("@GetMapping(\"/users\")"));
        assertTrue(source.contains("@PostMapping(\"/users/create\")"));
        assertTrue(source.contains("@PostMapping(\"/generate\")"));
        assertTrue(source.contains("@GetMapping(\"/batches\")"));
        assertTrue(source.contains("@PostMapping(\"/cleanup\")"));
        assertTrue(source.contains("parseResponseRes"));
    }

    @Test
    public void feignAndRestapiExposeMatchingInternalServiceRoutes() throws Exception {
        String client = readSource(
                "src/main/java/com/ypat/service/InternalTestServiceClient.java",
                "backend/system-wap/src/main/java/com/ypat/service/InternalTestServiceClient.java");
        String restapi = readSource(
                "../system-restapi/src/main/java/com/ypat/controller/InternalTestController.java",
                "backend/system-restapi/src/main/java/com/ypat/controller/InternalTestController.java");

        assertTrue(client.contains("@FeignClient(\"SYSTEM-API\")"));
        assertTrue(client.contains("/service/internal-test/resources"));
        assertTrue(client.contains("/service/internal-test/generate"));
        assertTrue(restapi.contains("@RequestMapping(\"/service/internal-test\")"));
        assertTrue(restapi.contains("InternalTestResourceService"));
        assertTrue(restapi.contains("InternalTestDataService"));
    }

    @Test
    public void oldSystemWebIsNotTouchedByInternalTestFeature() throws Exception {
        String status = readSource("../../.gitignore", ".gitignore");
        assertFalse(status.contains("backend/system-web/src/main/java/com/ypat/controller/AdminInternalTestController"));
    }
}
```

- [ ] **Step 2: Run the failing WAP test**

Run:

```bash
cd backend/system-wap
mvn test -Dtest=AdminInternalTestControllerSourceTest
```

Expected: fail until controllers and client exist.

- [ ] **Step 3: Implement restapi controller**

Create `InternalTestController` under `system-restapi` with `@RequestMapping("/service/internal-test")`. Methods should return `ResponseApiBody.success(...)` and delegate to services:

```text
GET /resources
POST /resources
POST /resources/update
POST /resources/status
GET /users
POST /users/create
POST /generate
GET /batches
POST /cleanup
```

- [ ] **Step 4: Implement Feign client**

Create `InternalTestServiceClient` under `system-wap` with matching `String` methods for the `/service/internal-test/*` endpoints. Use `@RequestBody` for DTO payloads and `@RequestParam` for simple status changes.

- [ ] **Step 5: Implement WAP admin controller**

Create `AdminInternalTestController` with `@RequestMapping("/admin/internal-test")`. Normalize pages to zero-based and size to max 50. Reuse the safer `parseResponseRes` style from `AdminYpatController`: reject malformed `code`/`res`, propagate downstream business errors, and wrap success data in `ResponseApiBody.success(...)`.

- [ ] **Step 6: Run tests**

Run:

```bash
cd backend/system-wap
mvn test -Dtest=AdminInternalTestControllerSourceTest
```

Expected: pass.

- [ ] **Step 7: Commit**

Run:

```bash
git add backend/system-restapi/src/main/java/com/ypat/controller/InternalTestController.java \
  backend/system-wap/src/main/java/com/ypat/service/InternalTestServiceClient.java \
  backend/system-wap/src/main/java/com/ypat/controller/AdminInternalTestController.java \
  backend/system-wap/src/test/java/com/ypat/controller/AdminInternalTestControllerSourceTest.java
git commit -m "feat: expose internal test admin APIs"
```

## Task 4: Frontend API, Enums, Menu, and Routing

**Files:**

- Create: `frontend-admin/src/api/modules/internal-test.ts`
- Modify: `frontend-admin/src/constants/enums.ts`
- Modify: `frontend-admin/src/constants/menu.ts`
- Modify: `frontend-admin/src/stores/modules/permission.ts`
- Create: `frontend-admin/tests/unit/internal-test-api.test.ts`
- Modify: `frontend-admin/tests/unit/admin-enums.test.ts`
- Modify: `frontend-admin/tests/unit/permission.test.ts`

- [ ] **Step 1: Write frontend unit tests**

Create `frontend-admin/tests/unit/internal-test-api.test.ts`:

```ts
import { beforeEach, describe, expect, it, vi } from 'vitest'

const getMock = vi.fn((url: string, params?: Record<string, unknown>) => Promise.resolve({ url, params }))
const postMock = vi.fn((url: string, data?: unknown, config?: unknown) => Promise.resolve({ url, data, config }))

vi.mock('@/api/request', () => ({ get: getMock, post: postMock }))

describe('内测数据 API', () => {
  beforeEach(() => {
    getMock.mockClear()
    postMock.mockClear()
  })

  it('应使用内测资源和生成路由', async () => {
    const api = await import('@/api/modules/internal-test')

    await api.getInternalResources({ page: 0, size: 10, mediaType: 'image', usageType: 'work' })
    expect(getMock).toHaveBeenCalledWith('/admin/internal-test/resources', {
      page: 0,
      size: 10,
      mediaType: 'image',
      usageType: 'work',
    })

    await api.createInternalResource({ mediaType: 'image', usageType: 'avatar', url: 'https://example.com/a.jpg' })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/resources', {
      mediaType: 'image',
      usageType: 'avatar',
      url: 'https://example.com/a.jpg',
    })

    await api.generateInternalData({ mode: 'create_and_generate', userCount: 2, publishStatus: '1' })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/generate', {
      mode: 'create_and_generate',
      userCount: 2,
      publishStatus: '1',
    })

    await api.cleanupInternalData({ batchNo: 'IT202607060001' })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/cleanup', { batchNo: 'IT202607060001' })
  })
})
```

Extend `admin-enums.test.ts` with assertions for `InternalTestMediaType`, `InternalTestUsageType`, `InternalTestResourceStatus`, and `InternalTestGenerateMode`.

Extend `permission.test.ts` or the store test to assert route components include:

```text
internal-test/resource/index
internal-test/generator/index
```

- [ ] **Step 2: Run failing frontend tests**

Run:

```bash
cd frontend-admin
pnpm test -- internal-test-api admin-enums permission
```

Expected: fail until files and route registration exist.

- [ ] **Step 3: Add API module**

Create `internal-test.ts` with typed interfaces:

```ts
export interface InternalTestResource { id?: number; mediaType?: string; usageType?: string; styleCode?: string; url?: string; title?: string; description?: string; profession?: string; city?: string; status?: string; sortNo?: number; remark?: string }
export interface InternalTestResourceQuery extends PageQuery { mediaType?: string; usageType?: string; styleCode?: string; profession?: string; city?: string; status?: string; keyword?: string }
export interface InternalTestGeneratePayload { mode?: string; userCount?: number; userIds?: number[]; nicknamePrefix?: string; gender?: string; profess?: string; province?: string; city?: string; area?: string; styleCode?: string; contentType?: string; templateType?: string; publishStatus?: string; avatarResourceIds?: number[]; ypatResourceIds?: number[]; workResourceIds?: number[]; batchNo?: string }
export interface InternalTestBatch { batchNo: string; userCount: number; ypatCount: number; workCount: number; status?: string; errors?: string[]; createdAt?: string }
```

Expose functions:

```ts
getInternalResources, createInternalResource, updateInternalResource, updateInternalResourceStatus,
getInternalUsers, createInternalUsers, generateInternalData, getInternalBatches, cleanupInternalData
```

- [ ] **Step 4: Add enums**

Add enum exports in `frontend-admin/src/constants/enums.ts`:

```ts
export const InternalTestMediaType = { IMAGE: { value: 'image', name: '图片' }, VIDEO: { value: 'video', name: '视频' } } as const
export const InternalTestUsageType = { AVATAR: { value: 'avatar', name: '头像' }, YPAT: { value: 'ypat', name: '约拍' }, WORK: { value: 'work', name: '作品' } } as const
export const InternalTestResourceStatus = { ENABLED: { value: 'enabled', name: '启用' }, DISABLED: { value: 'disabled', name: '停用' } } as const
export const InternalTestGenerateMode = { CREATE_AND_GENERATE: { value: 'create_and_generate', name: '新建用户并生成' }, APPEND_TO_USERS: { value: 'append_to_users', name: '给已有内测用户追加' } } as const
```

Also add option helpers for each enum.

- [ ] **Step 5: Add menu and routing registration**

Add a menu group:

```ts
{
  title: '内测数据',
  icon: 'DataAnalysis',
  children: [
    { title: '内测资源管理', path: '/internal-test/resource', component: 'internal-test/resource/index' },
    { title: '内测数据生成', path: '/internal-test/generator', component: 'internal-test/generator/index' },
  ],
}
```

In `permission.ts`, import the two pages and add them to `viewModules`.

- [ ] **Step 6: Run frontend tests**

Run:

```bash
cd frontend-admin
pnpm test -- internal-test-api admin-enums permission
```

Expected: pass.

- [ ] **Step 7: Commit**

Run:

```bash
git add frontend-admin/src/api/modules/internal-test.ts \
  frontend-admin/src/constants/enums.ts \
  frontend-admin/src/constants/menu.ts \
  frontend-admin/src/stores/modules/permission.ts \
  frontend-admin/tests/unit/internal-test-api.test.ts \
  frontend-admin/tests/unit/admin-enums.test.ts \
  frontend-admin/tests/unit/permission.test.ts
git commit -m "feat: add internal test admin routes"
```

## Task 5: Frontend Resource Management Page

**Files:**

- Create: `frontend-admin/src/views/internal-test/resource/index.vue`

- [ ] **Step 1: Create the resource page**

Build a Vue single-file component using existing Element Plus patterns from `manage/work-list/index.vue`. Required UI:

- `el-tabs` bound to media type, with tabs `图片` and `视频`.
- Search form: usage type, style, profession, city, status, keyword.
- Table: preview, title, media type, usage type, style, profession, city, status, sort number, created time, operations.
- Dialog: add/edit resource URL and metadata.
- Operations: create, update, enable, disable.

Use API functions from `@/api/modules/internal-test` and enum helpers from `@/constants/enums`.

- [ ] **Step 2: Add validation behavior**

Rules:

- URL is required.
- Usage type is required.
- Media type is inherited from active tab.
- Status defaults to `enabled`.
- Sort number defaults to `0`.

- [ ] **Step 3: Run frontend checks**

Run:

```bash
cd frontend-admin
pnpm test -- internal-test-api admin-enums permission
pnpm type-check
```

Expected: tests and type check pass.

- [ ] **Step 4: Commit**

Run:

```bash
git add frontend-admin/src/views/internal-test/resource/index.vue
git commit -m "feat: add internal resource management page"
```

## Task 6: Frontend Data Generator Page

**Files:**

- Create: `frontend-admin/src/views/internal-test/generator/index.vue`

- [ ] **Step 1: Create the generator page**

Build a Vue component with three un-nested sections:

- Generate config form.
- Resource selection tables.
- Batch result table.

Required controls:

- Mode select: `create_and_generate` or `append_to_users`.
- User count input with min `1`, max `50`, shown for create mode.
- Existing internal user ID input for append mode; accept comma-separated IDs and convert to `number[]`.
- City, profession, gender, nickname prefix, style select.
- Content type: `ypat`, `work`, `both`.
- Template type: `publish_ypat`, `appointment_photographer`, `appointment_model`.
- Publish status: default `1`, options `1` and `2`.
- Resource selectors for avatar, ypat, and work resource IDs.

- [ ] **Step 2: Add pre-submit validation and confirmation**

Validation:

- Create mode requires `userCount` between 1 and 50.
- Append mode requires at least one user ID.
- City, profession, style, content type, template type, and publish status are required.
- Avatar resource is required.
- Ypat resource is required when content type is `ypat` or `both`.
- Work resource is required when content type is `work` or `both`.

Before submit, call `ElMessageBox.confirm` with text containing `仅生成内测数据`.

For cleanup, call `ElMessageBox.confirm` with text containing `仅影响内测数据`.

- [ ] **Step 3: Wire API calls**

Use:

- `getInternalResources` to load enabled resources by tab/usage/style.
- `generateInternalData` to submit generation.
- `getInternalBatches` to refresh batch result list.
- `cleanupInternalData` to soft-clean by batch number.

- [ ] **Step 4: Run frontend checks**

Run:

```bash
cd frontend-admin
pnpm test -- internal-test-api admin-enums permission
pnpm type-check
```

Expected: pass.

- [ ] **Step 5: Commit**

Run:

```bash
git add frontend-admin/src/views/internal-test/generator/index.vue
git commit -m "feat: add internal test data generator page"
```

## Task 7: Final Verification and Guardrails

**Files:**

- Verify: no changes under `backend/system-web`
- Verify: docs, backend, and frontend tests

- [ ] **Step 1: Verify old backend is untouched**

Run:

```bash
git diff --name-only dev6...HEAD | rg '^backend/system-web/' || true
```

Expected: no output.

- [ ] **Step 2: Run targeted backend tests**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=InternalTestDataSourceTest
cd ../system-wap
mvn test -Dtest=AdminInternalTestControllerSourceTest
```

Expected: both pass.

- [ ] **Step 3: Run frontend tests and type check**

Run:

```bash
cd frontend-admin
pnpm test -- internal-test-api admin-enums permission
pnpm type-check
```

Expected: pass.

- [ ] **Step 4: Inspect changed files**

Run:

```bash
git status --short
git diff --stat dev6...HEAD
```

Expected: only planned files are changed, plus commits created per task.

- [ ] **Step 5: Commit verification notes only if this plan is updated**

If verification only runs commands and changes no files, do not create a commit. If the verification section of this plan is updated with concrete evidence, run:

```bash
git add docs/superpowers/plans/2026-07-06-admin-internal-test-data.md
git commit -m "docs: record internal test data verification"
```
