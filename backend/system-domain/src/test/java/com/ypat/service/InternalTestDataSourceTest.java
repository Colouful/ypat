package com.ypat.service;

import com.ypat.InternalTestGenerateQo;
import com.ypat.InternalTestBatchQo;
import com.ypat.InternalTestResourceQo;
import com.ypat.InternalTestUserActionQo;
import com.ypat.SysException;
import com.ypat.entity.InternalTestResource;
import com.ypat.entity.User;
import com.ypat.entity.Work;
import com.ypat.entity.WorkMedia;
import com.ypat.entity.WorkTag;
import com.ypat.entity.YpatImg;
import com.ypat.entity.YpatInfo;
import com.ypat.repository.InternalTestResourceRepository;
import com.ypat.repository.UserImgRepository;
import com.ypat.repository.UserRepository;
import com.ypat.repository.WorkMediaRepository;
import com.ypat.repository.WorkRepository;
import com.ypat.repository.WorkTagRelRepository;
import com.ypat.repository.WorkTagRepository;
import com.ypat.repository.YpatImgRepository;
import com.ypat.repository.YpatInfoRepository;
import org.junit.Test;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class InternalTestDataSourceTest {
    private static String read(String path) throws IOException {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            p = Paths.get("..").resolve(path).normalize();
        }
        if (!Files.exists(p)) {
            p = Paths.get("../..").resolve(path).normalize();
        }
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    @Test
    public void internalTestSqlCreatesResourceTableAndMarkers() throws Exception {
        String sql = read("../../docs/sql/pending/V_admin_internal_test_data.sql");

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
    public void mainEntitiesContainInternalTestMarkers() throws Exception {
        assertEntityMarkers("src/main/java/com/ypat/entity/User.java");
        assertEntityMarkers("src/main/java/com/ypat/entity/YpatInfo.java");
        assertEntityMarkers("src/main/java/com/ypat/entity/Work.java");
    }

    @Test
    public void ypatImagesAreReadInGeneratedIdOrder() throws Exception {
        Field ypatImages = YpatInfo.class.getDeclaredField("ypatImgs");
        OrderBy orderBy = ypatImages.getAnnotation(OrderBy.class);

        assertNotNull(orderBy);
        assertEquals("id ASC", orderBy.value());

        String source = read("src/main/java/com/ypat/entity/YpatInfo.java");
        String association = methodBody(source,
                "    @OneToMany",
                "    private String recomflag;");
        assertTrue(association.contains("@OrderBy(\"id ASC\")"));
    }

    @Test
    public void internalTestResourceEntityMapsResourceTable() throws Exception {
        String source = read("src/main/java/com/ypat/entity/InternalTestResource.java");

        assertTrue(source.contains("@Table(name = \"t_internal_test_resource\")"));
        assertTrue(source.contains("mediaType"));
        assertTrue(source.contains("usageType"));
        assertTrue(source.contains("styleCode"));
        assertTrue(source.contains("url"));
    }

    @Test
    public void internalTestDtosExposeRequiredFields() throws Exception {
        String resourceQo = read("../system-object/src/main/java/com/ypat/InternalTestResourceQo.java");
        assertTrue(resourceQo.contains("mediaType"));
        assertTrue(resourceQo.contains("usageType"));
        assertTrue(resourceQo.contains("styleCode"));
        assertTrue(resourceQo.contains("status"));

        String generateQo = read("../system-object/src/main/java/com/ypat/InternalTestGenerateQo.java");
        assertTrue(generateQo.contains("mode"));
        assertTrue(generateQo.contains("userCount"));
        assertTrue(generateQo.contains("publishStatus"));
        assertTrue(generateQo.contains("java.util.List<Long> userIds"));

        String batchQo = read("../system-object/src/main/java/com/ypat/InternalTestBatchQo.java");
        assertTrue(batchQo.contains("batchNo"));
        assertTrue(batchQo.contains("userCount"));
        assertTrue(batchQo.contains("ypatCount"));
        assertTrue(batchQo.contains("workCount"));
        assertTrue(batchQo.contains("ignoredRealCount"));
    }

    @Test
    public void publicListDtosExposeDataFlagFilter() throws Exception {
        assertDtoMarkers("../system-object/src/main/java/com/ypat/UserQo.java");
        assertDtoMarkers("../system-object/src/main/java/com/ypat/YpatInfoQo.java");
        assertDtoMarkers("../system-object/src/main/java/com/ypat/WorkListQo.java");
    }

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

    @Test
    public void internalTestEnumsExposeContractValues() throws Exception {
        assertEnumValues("../system-object/src/main/java/com/ypat/enums/InternalTestDataFlag.java",
                "real", "internal_test");
        assertEnumValues("../system-object/src/main/java/com/ypat/enums/InternalTestResourceMediaType.java",
                "image", "video");
        assertEnumValues("../system-object/src/main/java/com/ypat/enums/InternalTestResourceUsageType.java",
                "avatar", "ypat", "work");
        assertEnumValues("../system-object/src/main/java/com/ypat/enums/InternalTestResourceStatus.java",
                "enabled", "disabled");
    }

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

    @Test
    public void cleanupRequiresExplicitConditionAndSupportsUserIds() throws Exception {
        String dataService = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java");
        String userRepo = read("backend/system-domain/src/main/java/com/ypat/repository/UserRepository.java");
        String ypatRepo = read("backend/system-domain/src/main/java/com/ypat/repository/YpatInfoRepository.java");
        String workRepo = read("backend/system-domain/src/main/java/com/ypat/repository/WorkRepository.java");

        assertTrue(dataService.contains("清理条件不能为空"));
        assertTrue(dataService.contains("findInternalTestUserIdsForCleanup"));
        assertTrue(dataService.contains("getCity()"));
        assertTrue(dataService.contains("getArea()"));
        assertTrue(dataService.contains("getProfess()"));
        assertTrue(dataService.contains("getGender()"));
        assertTrue(dataService.contains("setIgnoredRealCount"));
        assertTrue(dataService.contains("updateInternalTestUsersStatusByIds"));
        assertTrue(dataService.contains("updateInternalTestYpatStatusByUserIds"));
        assertTrue(dataService.contains("updateInternalTestWorkStatusByUserIds"));
        assertTrue(userRepo.contains("updateInternalTestUsersStatusByIds"));
        assertTrue(userRepo.contains("u.id in :userIds"));
        assertTrue(userRepo.contains("u.dataFlag = 'internal_test'"));
        assertTrue(userRepo.contains("(:batchNo is null or u.internalBatchNo = :batchNo)"));
        assertTrue(ypatRepo.contains("updateInternalTestYpatStatusByUserIds"));
        assertTrue(ypatRepo.contains("y.user.id in :userIds"));
        assertTrue(ypatRepo.contains("y.dataFlag = 'internal_test'"));
        assertTrue(ypatRepo.contains("(:batchNo is null or y.internalBatchNo = :batchNo)"));
        assertTrue(workRepo.contains("updateInternalTestWorkStatusByUserIds"));
        assertTrue(workRepo.contains("w.userid in :userIds"));
        assertTrue(workRepo.contains("w.dataFlag = 'internal_test'"));
        assertTrue(workRepo.contains("(:batchNo is null or w.internalBatchNo = :batchNo)"));
    }

    @Test
    public void cleanupAndBatchListAvoidUnboundedScans() throws Exception {
        String dataService = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java");
        String userRepo = read("backend/system-domain/src/main/java/com/ypat/repository/UserRepository.java");
        String ypatRepo = read("backend/system-domain/src/main/java/com/ypat/repository/YpatInfoRepository.java");
        String workRepo = read("backend/system-domain/src/main/java/com/ypat/repository/WorkRepository.java");

        assertFalse(dataService.contains("userRepository.findAll(cleanupUserSpec"));
        assertFalse(dataService.contains("List<User> matchedUsers"));
        assertTrue(dataService.contains("CHUNK_SIZE"));
        assertTrue(dataService.contains("countRealUsersForCleanup"));
        assertTrue(dataService.contains("findInternalTestUserIdsForCleanup"));
        assertTrue(dataService.contains("aggregateInternalTestBatches"));
        assertFalse(dataService.contains("List<User> users = userRepository.findAll(userSpec(qo.getBatchNo()))"));
        assertFalse(dataService.contains("List<YpatInfo> ypats = ypatInfoRepository.findAll(ypatSpec(qo.getBatchNo()))"));
        assertFalse(dataService.contains("List<Work> works = workRepository.findAll(workSpec(qo.getBatchNo()))"));
        assertTrue(userRepo.contains("countRealUsersForCleanup"));
        assertTrue(userRepo.contains("findInternalTestUserIdsForCleanup"));
        assertTrue(userRepo.contains("aggregateInternalTestBatches"));
        assertTrue(userRepo.contains("u.dataFlag = 'internal_test'"));
        assertTrue(ypatRepo.contains("aggregateInternalTestBatches"));
        assertTrue(ypatRepo.contains("y.dataFlag = 'internal_test'"));
        assertTrue(workRepo.contains("aggregateInternalTestBatches"));
        assertTrue(workRepo.contains("w.dataFlag = 'internal_test'"));
    }

    @Test
    public void cleanupAllIsExplicitCompleteAndKeysetPaginated() throws Exception {
        String dataService = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java");
        String generateQo = read("backend/system-object/src/main/java/com/ypat/InternalTestGenerateQo.java");
        String userRepo = read("backend/system-domain/src/main/java/com/ypat/repository/UserRepository.java");
        String resourceService = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java");
        String resourceRepo = read("backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java");

        assertTrue(generateQo.contains("private Boolean cleanupAll;"));
        assertTrue(dataService.contains("Boolean.TRUE.equals(qo.getCleanupAll())"));
        assertTrue(dataService.contains("releaseAllUsedResources"));
        assertTrue(resourceService.contains("public int releaseAllUsedResources()"));
        assertTrue(resourceRepo.contains("int releaseAllUsed("));

        assertTrue(userRepo.contains("u.id > :afterId"));
        assertTrue(userRepo.contains("@Param(\"afterId\") Long afterId"));
        assertTrue(dataService.contains("Long afterId = 0L;"));
        assertTrue(dataService.contains("afterId = internalUserIds.get(internalUserIds.size() - 1)"));
        assertFalse(dataService.contains("int page = 0;"));

        assertTrue(dataService.contains("releaseResourcesByTargets(batchNo, \"user\", userIds)"));
        assertTrue(dataService.contains("target[3] += source[3];"));
    }

    @Test
    public void lazyWorkbenchMigrationAddsResourceGroupUsageAndRegionFields() throws Exception {
        String sql = read("docs/sql/pending/V_admin_internal_test_data.sql");

        assertResourceColumnMigration(sql, "province",
                "ALTER TABLE `t_internal_test_resource` ADD COLUMN `province` VARCHAR(64) DEFAULT NULL");
        assertResourceColumnMigration(sql, "area",
                "ALTER TABLE `t_internal_test_resource` ADD COLUMN `area` VARCHAR(64) DEFAULT NULL");
        assertResourceColumnMigration(sql, "group_no",
                "ALTER TABLE `t_internal_test_resource` ADD COLUMN `group_no` VARCHAR(64) DEFAULT NULL");
        assertResourceColumnMigration(sql, "group_title",
                "ALTER TABLE `t_internal_test_resource` ADD COLUMN `group_title` VARCHAR(128) DEFAULT NULL");
        assertResourceColumnMigration(sql, "group_sort_no",
                "ALTER TABLE `t_internal_test_resource` ADD COLUMN `group_sort_no` INT NOT NULL DEFAULT 0");
        assertResourceColumnMigration(sql, "used_flag",
                "ALTER TABLE `t_internal_test_resource` ADD COLUMN `used_flag` TINYINT NOT NULL DEFAULT 0");
        assertResourceColumnMigration(sql, "used_batch_no",
                "ALTER TABLE `t_internal_test_resource` ADD COLUMN `used_batch_no` VARCHAR(64) DEFAULT NULL");
        assertResourceColumnMigration(sql, "used_target_type",
                "ALTER TABLE `t_internal_test_resource` ADD COLUMN `used_target_type` VARCHAR(16) DEFAULT NULL");
        assertResourceColumnMigration(sql, "used_target_id",
                "ALTER TABLE `t_internal_test_resource` ADD COLUMN `used_target_id` BIGINT DEFAULT NULL");
        assertResourceColumnMigration(sql, "used_at",
                "ALTER TABLE `t_internal_test_resource` ADD COLUMN `used_at` DATETIME DEFAULT NULL");
        assertResourceIndexMigration(sql, "idx_internal_resource_available_group",
                "ALTER TABLE `t_internal_test_resource` ADD INDEX `idx_internal_resource_available_group` (`usage_type`, `status`, `used_flag`, `group_no`(254), `sort_no`, `id`)");
        assertResourceIndexMigration(sql, "idx_internal_resource_group_no",
                "ALTER TABLE `t_internal_test_resource` ADD INDEX `idx_internal_resource_group_no` (`group_no`, `id`)");
    }

    @Test
    public void lazyWorkbenchDtosExposeBatchImportGenerationAndUserActionFields() throws Exception {
        assertReadableWritable(InternalTestResourceQo.class, "urls", List.class);
        assertReadableWritable(InternalTestResourceQo.class, "province", String.class);
        assertReadableWritable(InternalTestResourceQo.class, "area", String.class);
        assertReadableWritable(InternalTestResourceQo.class, "groupNo", String.class);
        assertReadableWritable(InternalTestResourceQo.class, "groupTitle", String.class);
        assertReadableWritable(InternalTestResourceQo.class, "groupSize", Integer.class);
        assertReadableWritable(InternalTestResourceQo.class, "groupSortNo", Integer.class);
        assertReadableWritable(InternalTestResourceQo.class, "usedFlag", Integer.class);
        assertReadableWritable(InternalTestResourceQo.class, "usedBatchNo", String.class);
        assertReadableWritable(InternalTestResourceQo.class, "usedTargetType", String.class);
        assertReadableWritable(InternalTestResourceQo.class, "usedTargetId", Long.class);
        assertReadableWritable(InternalTestResourceQo.class, "usedAt", Date.class);

        assertReadableWritable(InternalTestGenerateQo.class, "actionType", String.class);
        assertReadableWritable(InternalTestGenerateQo.class, "userId", Long.class);
        assertReadableWritable(InternalTestGenerateQo.class, "groupNos", List.class);
        assertReadableWritable(InternalTestGenerateQo.class, "wx", String.class);
        assertReadableWritable(InternalTestGenerateQo.class, "mobile", String.class);
        assertReadableWritable(InternalTestGenerateQo.class, "styleCodes", List.class);
        assertReadableWritable(InternalTestGenerateQo.class, "patdate", String.class);
        assertReadableWritable(InternalTestGenerateQo.class, "patslice", String.class);
        assertReadableWritable(InternalTestGenerateQo.class, "describ", String.class);
        assertReadableWritable(InternalTestGenerateQo.class, "target", String.class);

        assertReadableWritable(InternalTestUserActionQo.class, "userId", Long.class);
        assertReadableWritable(InternalTestUserActionQo.class, "days", Integer.class);
        assertReadableWritable(InternalTestUserActionQo.class, "reason", String.class);

        assertReadableWritable(InternalTestResource.class, "urls", List.class);
        assertReadableWritable(InternalTestResource.class, "province", String.class);
        assertReadableWritable(InternalTestResource.class, "area", String.class);
        assertReadableWritable(InternalTestResource.class, "groupNo", String.class);
        assertReadableWritable(InternalTestResource.class, "groupTitle", String.class);
        assertReadableWritable(InternalTestResource.class, "groupSize", Integer.class);
        assertReadableWritable(InternalTestResource.class, "groupSortNo", Integer.class);
        assertReadableWritable(InternalTestResource.class, "usedFlag", Integer.class);
        assertReadableWritable(InternalTestResource.class, "usedBatchNo", String.class);
        assertReadableWritable(InternalTestResource.class, "usedTargetType", String.class);
        assertReadableWritable(InternalTestResource.class, "usedTargetId", Long.class);
        assertReadableWritable(InternalTestResource.class, "usedAt", Date.class);
        assertTransientField(InternalTestResource.class, "urls");
        assertTransientField(InternalTestResource.class, "groupSize");
        assertColumnName(InternalTestResource.class, "groupNo", "group_no");
        assertColumnName(InternalTestResource.class, "groupTitle", "group_title");
        assertColumnName(InternalTestResource.class, "groupSortNo", "group_sort_no");
        assertColumnName(InternalTestResource.class, "usedFlag", "used_flag");
        assertColumnName(InternalTestResource.class, "usedBatchNo", "used_batch_no");
        assertColumnName(InternalTestResource.class, "usedTargetType", "used_target_type");
        assertColumnName(InternalTestResource.class, "usedTargetId", "used_target_id");
        assertColumnName(InternalTestResource.class, "usedAt", "used_at");
    }

    @Test
    public void resourceServiceSupportsBatchImportGroupsUsageReleaseAndFilters() throws Exception {
        String service = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java");
        String repo = read("backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java");
        String frontend = read("frontend-admin/src/api/modules/internal-test.ts");

        assertTrue(service.contains("public Map<String, Object> batchSave(InternalTestResourceQo qo)"));
        assertTrue(service.contains("public Map<String, Object> listAvailableGroups(InternalTestResourceQo qo)"));
        assertTrue(service.contains("public void markResourcesUsed(List<InternalTestResource> resources"));
        assertTrue(service.contains("public int releaseResourcesByBatch(String batchNo)"));
        assertTrue(service.contains("splitWorkGroups"));
        assertTrue(service.contains("validateBatchQo"));
        assertTrue(service.contains("normalizeUrls"));
        assertTrue(service.contains("buildGroupNo"));
        assertTrue(service.contains("existsByUrl"));
        assertTrue(service.contains("defaultStatus"));
        assertTrue(service.contains("usedFlag"));
        assertTrue(service.contains("root.get(\"province\")"));
        assertTrue(service.contains("root.get(\"area\")"));
        assertTrue(service.contains("root.get(\"usedFlag\")"));
        assertTrue(service.contains("root.get(\"groupNo\")"));
        assertTrue(repo.contains("InternalTestResource findByUrl(String url);"));
        assertTrue(repo.contains("List<InternalTestResource> findByGroupNoIn(List<String> groupNos);"));
        assertTrue(repo.contains("List<InternalTestResource> findByGroupNoInAndStatus(List<String> groupNos, String status);"));
        assertTrue(repo.contains("List<InternalTestResource> findByUsedBatchNo(String usedBatchNo);"));
        assertTrue(repo.contains("int markResourcesUsedIfAvailable("));
        assertTrue(repo.contains("int releaseByUsedBatchNo("));
        assertAvailableResourceRepositoryMethod(repo, "findAvailableGroupNos");
        assertAvailableResourceRepositoryMethod(repo, "countAvailableGroups");
        assertAvailableResourceRepositoryMethod(repo, "findAvailableSingleResources");
        assertAvailableResourceRepositoryMethod(repo, "countAvailableSingleResources");
        assertFalse(repo.contains("coalesce(r.used_flag"));
        assertFalse(repo.contains("select count(*) from t_internal_test_resource all_r"));
        assertTrue(repo.contains("join (select group_no, count(*) total_count"));

        String resourceGroupType = methodBody(frontend,
                "export interface InternalTestResourceGroup {",
                "export interface InternalTestUserActionPayload {");
        assertTrue(resourceGroupType.contains("usedFlag?: number"));

        String listAvailableGroups = methodBody(service,
                "public Map<String, Object> listAvailableGroups(InternalTestResourceQo qo)",
                "public void markResourcesUsed(List<InternalTestResource> resources");
        assertFalse(listAvailableGroups.contains("page(qo)"));
        assertFalse(listAvailableGroups.contains("result.put(\"totalPages\", 1)"));
        assertFalse(listAvailableGroups.contains("findAll(buildSpecification(qo)"));
        assertTrue(listAvailableGroups.contains("findAvailableGroupNos"));
        assertTrue(listAvailableGroups.contains("countAvailableGroups"));
        assertTrue(listAvailableGroups.contains("findAvailableSingleResources"));
        assertTrue(listAvailableGroups.contains("countAvailableSingleResources"));
        assertTrue(listAvailableGroups.contains("findByGroupNoIn"));
        assertTrue(listAvailableGroups.contains("isCompleteAvailableGroup"));
        assertTrue(listAvailableGroups.contains("calculateTotalPages"));
        assertFalse(listAvailableGroups.contains("qo.setUsedFlag(0)"));
        assertTrue(listAvailableGroups.contains("Integer usedFlag = qo.getUsedFlag()"));
        assertTrue(listAvailableGroups.contains("group.put(\"usedFlag\""));

        String markResourcesUsed = methodBody(service,
                "public void markResourcesUsed(List<InternalTestResource> resources",
                "public int releaseResourcesByBatch(String batchNo)");
        assertTrue(markResourcesUsed.contains("validateUsageContext(batchNo, targetType, targetId)"));
        assertTrue(markResourcesUsed.contains("collectResourceIds(resources)"));
        assertTrue(markResourcesUsed.contains("markResourcesUsedIfAvailable"));
        assertTrue(markResourcesUsed.contains("if (updated != ids.size())"));
        assertFalse(markResourcesUsed.contains("internalTestResourceRepository.save(resource)"));
        assertTrue(service.contains("private void validateUsageContext(String batchNo, String targetType, Long targetId)"));
        assertTrue(service.contains("private List<Long> collectResourceIds(List<InternalTestResource> resources)"));

        assertTrue(service.contains("private String buildGroupNoPrefix()"));
        assertTrue(service.contains("UUID.randomUUID()"));
        assertTrue(service.contains("yyyyMMddHHmmssSSS"));
        assertTrue(service.contains("private String buildGroupNo(String prefix, int index)"));
    }

    @Test
    public void markResourcesUsedRejectsMissingUsageContextBeforeSave() {
        InternalTestResourceService service = new InternalTestResourceService();
        final RepositoryState state = new RepositoryState();
        ReflectionTestUtils.setField(service, "internalTestResourceRepository",
                repositoryProxy(Collections.<InternalTestResource>emptyList(),
                        Collections.<InternalTestResource>emptyList(),
                        state));

        List<InternalTestResource> resources = Collections.singletonList(resource(1L, "G1", "enabled", 0));

        assertMarkResourcesUsedFails(service, resources, null, "work", 1L);
        assertMarkResourcesUsedFails(service, resources, "B1", null, 1L);
        assertMarkResourcesUsedFails(service, resources, "B1", "work", null);
        assertEquals(0, state.saveCount);
        assertEquals(0, state.markUsedCallCount);
    }

    @Test
    public void markResourcesUsedUsesConditionalUpdateAndRejectsPartialUpdates() {
        InternalTestResourceService service = new InternalTestResourceService();
        final RepositoryState state = new RepositoryState();
        state.markUsedAffectedRows = 1;
        ReflectionTestUtils.setField(service, "internalTestResourceRepository",
                repositoryProxy(Collections.<InternalTestResource>emptyList(),
                        Collections.<InternalTestResource>emptyList(),
                        state));

        List<InternalTestResource> resources = Arrays.asList(
                resource(1L, "G1", "enabled", 0),
                resource(2L, "G1", "enabled", 0)
        );

        assertMarkResourcesUsedFails(service, resources, "B1", "work", 9L);
        assertEquals(1, state.markUsedCallCount);
        assertEquals(0, state.saveCount);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void listAvailableGroupsExcludesPartiallyAvailableWorkGroups() throws Exception {
        InternalTestResourceService service = new InternalTestResourceService();
        List<InternalTestResource> candidates = Arrays.asList(
                resource(1L, "G1", "enabled", 0),
                resource(3L, "G2", "enabled", 0),
                resource(4L, "G2", "enabled", 0),
                resource(5L, null, "enabled", 0)
        );
        List<InternalTestResource> allGrouped = Arrays.asList(
                resource(1L, "G1", "enabled", 0),
                resource(2L, "G1", "enabled", 1),
                resource(3L, "G2", "enabled", 0),
                resource(4L, "G2", "enabled", 0)
        );
        RepositoryState state = new RepositoryState();
        state.groupNosPage = Arrays.asList("G1", "G2");
        state.singleResourcesPage = Collections.singletonList(resource(5L, null, "enabled", 0));
        ReflectionTestUtils.setField(service, "internalTestResourceRepository",
                repositoryProxy(candidates, allGrouped, state));

        InternalTestResourceQo qo = new InternalTestResourceQo();
        qo.setPage(0);
        qo.setSize(10);
        qo.setUsedFlag(0);

        Map<String, Object> result = service.listAvailableGroups(qo);
        List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");

        assertEquals(2, content.size());
        assertFalse(containsGroupView(content, "G1"));
        assertTrue(containsGroupView(content, "G2"));
        assertEquals(2, groupViewSize(content, "G2"));
        assertEquals(Integer.valueOf(0), groupViewUsedFlag(content, "G2"));
        assertTrue(containsGroupView(content, "single-5"));
        assertEquals(2, ((Number) result.get("totalElements")).intValue());
        assertEquals(1, ((Number) result.get("totalPages")).intValue());
        assertEquals(1, state.groupNoPageCallCount);
        assertEquals(Integer.valueOf(0), state.findGroupNosUsedFlag);
        assertEquals(Integer.valueOf(0), state.countGroupsUsedFlag);
        assertEquals(Integer.valueOf(0), state.findSinglesUsedFlag);
        assertEquals(Integer.valueOf(0), state.countSinglesUsedFlag);

        String source = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java");
        assertTrue(source.contains("groupNo.startsWith(\"single-\")"));
        assertTrue(source.contains("internalTestResourceRepository.findOne(resourceId)"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void listAvailableGroupsReturnsCompleteUsedGroupsWhenRequested() {
        InternalTestResourceService service = new InternalTestResourceService();
        List<InternalTestResource> allGrouped = Arrays.asList(
                resource(6L, "G3", "enabled", 1),
                resource(7L, "G3", "enabled", 1),
                resource(8L, "G4", "enabled", 0)
        );
        RepositoryState state = new RepositoryState();
        state.groupNosPage = Arrays.asList("G3", "G4");
        state.singleResourcesPage = Arrays.asList(
                resource(9L, null, "enabled", 1),
                resource(10L, null, "enabled", 0)
        );
        ReflectionTestUtils.setField(service, "internalTestResourceRepository",
                repositoryProxy(Collections.<InternalTestResource>emptyList(), allGrouped, state));

        InternalTestResourceQo qo = new InternalTestResourceQo();
        qo.setPage(0);
        qo.setSize(10);
        qo.setUsedFlag(1);

        Map<String, Object> result = service.listAvailableGroups(qo);
        List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");

        assertEquals(2, content.size());
        assertTrue(containsGroupView(content, "G3"));
        assertEquals(2, groupViewSize(content, "G3"));
        assertEquals(Integer.valueOf(1), groupViewUsedFlag(content, "G3"));
        assertTrue(containsGroupView(content, "single-9"));
        assertFalse(containsGroupView(content, "G4"));
        assertFalse(containsGroupView(content, "single-10"));
        assertEquals(Integer.valueOf(1), state.findGroupNosUsedFlag);
        assertEquals(Integer.valueOf(1), state.countGroupsUsedFlag);
        assertEquals(Integer.valueOf(1), state.findSinglesUsedFlag);
        assertEquals(Integer.valueOf(1), state.countSinglesUsedFlag);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void listAvailableGroupsDoesNotFilterUsageWhenUsedFlagIsNull() {
        InternalTestResourceService service = new InternalTestResourceService();
        List<InternalTestResource> allGrouped = Arrays.asList(
                resource(11L, "G5", "enabled", 0),
                resource(12L, "G5", "enabled", 0),
                resource(13L, "G6", "enabled", 1),
                resource(14L, "G6", "enabled", 1)
        );
        RepositoryState state = new RepositoryState();
        state.groupNosPage = Arrays.asList("G5", "G6");
        state.singleResourcesPage = Arrays.asList(
                resource(15L, null, "enabled", 0),
                resource(16L, null, "enabled", 1)
        );
        ReflectionTestUtils.setField(service, "internalTestResourceRepository",
                repositoryProxy(Collections.<InternalTestResource>emptyList(), allGrouped, state));

        InternalTestResourceQo qo = new InternalTestResourceQo();
        qo.setPage(0);
        qo.setSize(10);

        Map<String, Object> result = service.listAvailableGroups(qo);
        List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");

        assertEquals(4, content.size());
        assertEquals(Integer.valueOf(0), groupViewUsedFlag(content, "G5"));
        assertEquals(Integer.valueOf(1), groupViewUsedFlag(content, "G6"));
        assertTrue(containsGroupView(content, "single-15"));
        assertTrue(containsGroupView(content, "single-16"));
        assertEquals(1, state.groupNoPageCallCount);
        assertEquals(1, state.countGroupsCallCount);
        assertEquals(1, state.singlePageCallCount);
        assertEquals(1, state.countSinglesCallCount);
        assertNull(state.findGroupNosUsedFlag);
        assertNull(state.countGroupsUsedFlag);
        assertNull(state.findSinglesUsedFlag);
        assertNull(state.countSinglesUsedFlag);
    }

    @Test
    public void saveRejectsIdentityChangesForUsedResources() {
        InternalTestResourceService service = new InternalTestResourceService();
        RepositoryState state = new RepositoryState();
        state.findOneResource = resource(11L, "G1", "enabled", 1);
        state.findOneResource.setUrl("https://example.com/old.jpg");
        state.findOneResource.setUsageType("work");
        state.findOneResource.setMediaType("image");
        ReflectionTestUtils.setField(service, "internalTestResourceRepository",
                repositoryProxy(Collections.<InternalTestResource>emptyList(),
                        Collections.<InternalTestResource>emptyList(),
                        state));

        InternalTestResourceQo qo = new InternalTestResourceQo();
        qo.setId(11L);
        qo.setUrl("https://example.com/new.jpg");
        qo.setUsageType("work");
        qo.setMediaType("image");
        qo.setGroupNo("G1");

        try {
            service.save(qo);
        } catch (SysException e) {
            assertEquals(0, state.saveCount);
            return;
        }
        fail("Expected SysException");
    }

    @Test
    public void splitWorkGroupsUsesBlankLinesBeforeGroupSizeFallback() {
        InternalTestResourceService service = new InternalTestResourceService();
        InternalTestResourceQo qo = new InternalTestResourceQo();
        qo.setUsageType("work");
        qo.setGroupSize(2);

        List<List<String>> groups = service.splitWorkGroups(Arrays.asList(
                "https://example.com/a.jpg",
                "https://example.com/b.jpg",
                "",
                "https://example.com/c.jpg",
                "https://example.com/d.jpg",
                "https://example.com/e.jpg"
        ), qo);

        assertEquals(2, groups.size());
        assertEquals(Arrays.asList("https://example.com/a.jpg", "https://example.com/b.jpg"), groups.get(0));
        assertEquals(Arrays.asList("https://example.com/c.jpg", "https://example.com/d.jpg", "https://example.com/e.jpg"), groups.get(1));
    }

    @Test
    public void dataServiceSplitsUserWorkYpatGenerationAndMarksResourcesUsed() throws Exception {
        String service = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java");
        String batchQo = read("backend/system-object/src/main/java/com/ypat/InternalTestBatchQo.java");
        String resourceService = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java");
        String resourceRepo = read("backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java");
        String ypatRepo = read("backend/system-domain/src/main/java/com/ypat/repository/YpatInfoRepository.java");
        String workRepo = read("backend/system-domain/src/main/java/com/ypat/repository/WorkRepository.java");

        assertTrue(service.contains("InternalTestResourceService"));
        assertTrue(service.contains("public InternalTestBatchQo generateUsers(InternalTestGenerateQo qo)"));
        assertTrue(service.contains("public InternalTestBatchQo generateWorks(InternalTestGenerateQo qo)"));
        assertTrue(service.contains("public InternalTestBatchQo generateYpats(InternalTestGenerateQo qo)"));
        assertTrue(service.contains("loadInternalUser(qo.getUserId())"));
        assertTrue(service.contains("loadAvailableWorkGroup"));
        assertTrue(service.contains("internalTestResourceRepository.findByGroupNoIn(groupNos)"));
        assertTrue(service.contains("createWorkFromGroup"));
        assertTrue(service.contains("WorkTagRepository"));
        assertTrue(service.contains("WorkTagRelRepository"));
        assertTrue(service.contains("bindWorkTags(work.getId(), qo.getStyleCodes())"));
        assertTrue(service.contains("workTagRepository.findByCode(styleCode)"));
        assertTrue(service.contains("workTagRelRepository.save(rel)"));
        assertTrue(service.contains("作品风格不能为空"));
        assertTrue(service.contains("markResourcesUsed"));
        assertTrue(service.contains("markSingleResourceUsed(avatar, batchNo, \"user\", user.getId())"));
        assertTrue(service.contains("releaseResourcesByBatch"));
        assertTrue(service.contains("setReleasedResourceCount"));
        assertTrue(service.contains("setWx(qo.getWx())"));
        assertTrue(service.contains("setMobile(qo.getMobile())"));
        assertTrue(service.contains("String.join(\",\", qo.getStyleCodes())"));
        assertTrue(service.contains("validateYpatGeneration(qo)"));
        assertTrue(service.contains("约拍日期和时间段不能为空"));
        assertTrue(service.contains("约拍地点必须选择到区县"));
        assertTrue(service.contains("约拍要求不能为空"));
        assertTrue(service.contains("约拍风格不能为空"));
        assertTrue(service.contains("联系电话格式错误"));
        assertTrue(service.contains("同一作品组不能同时包含图片和视频"));
        assertTrue(service.contains("只能操作内测用户"));
        assertTrue(service.contains("internalTestResourceService.releaseResourcesByTargets"));
        assertTrue(service.contains("ypatInfoRepository.findInternalTestYpatIdsByUserIds"));
        assertTrue(service.contains("workRepository.findInternalTestWorkIdsByUserIds"));
        assertTrue(resourceService.contains("public int releaseResourcesByTargets(String batchNo, String targetType, List<Long> targetIds)"));
        assertTrue(resourceRepo.contains("int releaseByUsedTargets("));
        assertTrue(ypatRepo.contains("List<Long> findInternalTestYpatIdsByUserIds("));
        assertTrue(workRepo.contains("List<Long> findInternalTestWorkIdsByUserIds("));
        assertTrue(batchQo.contains("releasedResourceCount"));
        assertTrue(batchQo.contains("getReleasedResourceCount"));
        assertTrue(batchQo.contains("setReleasedResourceCount"));

        String generate = methodBody(service,
                "public InternalTestBatchQo generate(InternalTestGenerateQo qo)",
                "public Map<String, Object> listUsers(InternalTestGenerateQo qo)");
        assertTrue(generate.contains("markSingleResourceUsed"));
        assertTrue(generate.contains("saveYpatImages(ypat, Collections.singletonList(resource))"));
        assertTrue(generate.contains("users.size()"));
    }

    @Test
    public void workGenerationRequiresExactlyOneGroupAndDoesNotLoopGroups() throws Exception {
        InternalTestDataService dataService = new InternalTestDataService();
        InternalTestGenerateQo qo = validYpatQo();
        qo.setGroupNos(Arrays.asList("G1", "G2"));

        assertPrivateSysException(dataService, "validateWorkGeneration", qo, "一次只能选择一个作品组");

        String service = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java");
        String generateWorks = methodBody(service,
                "public InternalTestBatchQo generateWorks(InternalTestGenerateQo qo)",
                "public InternalTestBatchQo generateYpats(InternalTestGenerateQo qo)");
        assertTrue(generateWorks.contains("String groupNo = qo.getGroupNos().get(0);"));
        assertFalse(generateWorks.contains("for (String groupNo : qo.getGroupNos())"));

        String validateWorks = methodBody(service,
                "private void validateWorkGeneration(InternalTestGenerateQo qo)",
                "private void bindWorkTags(Long workId, List<String> styleCodes)");
        assertTrue(validateWorks.contains("qo.getGroupNos().size() != 1"));
        assertTrue(validateWorks.contains("\"一次只能选择一个作品组\""));
    }

    @Test
    public void workGenerationCreatesOneWorkWithAllGroupMediaAndMarksTheWholeGroupOnce() {
        InternalTestDataService dataService = new InternalTestDataService();
        final List<InternalTestResource> group = Arrays.asList(
                resource(11L, "G-WORK", "enabled", 0),
                resource(12L, "G-WORK", "enabled", 0));
        ReflectionTestUtils.setField(dataService, "internalTestResourceRepository",
                repositoryProxy(Collections.<InternalTestResource>emptyList(), group, new RepositoryState()));

        User user = internalUser(7L);
        ReflectionTestUtils.setField(dataService, "userRepository", internalUserRepository(user));

        final List<Work> savedWorks = new ArrayList<Work>();
        ReflectionTestUtils.setField(dataService, "workRepository", repositoryProxy(WorkRepository.class,
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if (method.getDeclaringClass().equals(Object.class)) {
                            return objectMethod(proxy, method, args);
                        }
                        if ("save".equals(method.getName())) {
                            Work work = (Work) args[0];
                            work.setId(88L);
                            savedWorks.add(work);
                            return work;
                        }
                        return defaultValue(method.getReturnType());
                    }
                }));

        final List<WorkMedia> savedMedia = new ArrayList<WorkMedia>();
        ReflectionTestUtils.setField(dataService, "workMediaRepository", repositoryProxy(WorkMediaRepository.class,
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if (method.getDeclaringClass().equals(Object.class)) {
                            return objectMethod(proxy, method, args);
                        }
                        if ("save".equals(method.getName())) {
                            savedMedia.add((WorkMedia) args[0]);
                            return args[0];
                        }
                        return defaultValue(method.getReturnType());
                    }
                }));

        final WorkTag workTag = new WorkTag();
        workTag.setId(9L);
        workTag.setStatus(1);
        ReflectionTestUtils.setField(dataService, "workTagRepository", repositoryProxy(WorkTagRepository.class,
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if (method.getDeclaringClass().equals(Object.class)) {
                            return objectMethod(proxy, method, args);
                        }
                        if ("findByCode".equals(method.getName())) {
                            return workTag;
                        }
                        return defaultValue(method.getReturnType());
                    }
                }));
        ReflectionTestUtils.setField(dataService, "workTagRelRepository",
                passThroughSaveRepository(WorkTagRelRepository.class));

        RecordingResourceService resourceService = new RecordingResourceService();
        ReflectionTestUtils.setField(dataService, "internalTestResourceService", resourceService);
        InternalTestGenerateQo qo = validYpatQo();
        qo.setBatchNo("BATCH-WORK-1");
        qo.setGroupNos(Collections.singletonList("G-WORK"));
        qo.setStyleCodes(Collections.singletonList("work-style"));

        InternalTestBatchQo batch = dataService.generateWorks(qo);

        assertEquals(1, batch.getWorkCount().intValue());
        assertEquals(1, savedWorks.size());
        assertEquals(2, savedMedia.size());
        assertEquals(Arrays.asList("https://example.com/11.jpg", "https://example.com/12.jpg"),
                workMediaUrls(savedMedia));
        assertEquals(1, resourceService.markResourcesUsedCallCount);
        assertEquals(Arrays.asList(11L, 12L), resourceIds(resourceService.resources));
        assertEquals("BATCH-WORK-1", resourceService.batchNo);
        assertEquals("work", resourceService.targetType);
        assertEquals(Long.valueOf(88L), resourceService.targetId);
    }

    @Test
    public void ypatGenerationRequiresOneToNineUniqueResourceIds() {
        InternalTestDataService dataService = new InternalTestDataService();
        InternalTestGenerateQo qo = validYpatQo();

        qo.setYpatResourceIds(Collections.<Long>emptyList());
        assertPrivateSysException(dataService, "validateYpatGeneration", qo, "约拍图片不能为空");

        qo.setYpatResourceIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
        assertPrivateSysException(dataService, "validateYpatGeneration", qo, "约拍图片最多选择9张");

        qo.setYpatResourceIds(Arrays.asList(1L, 2L, 1L));
        assertPrivateSysException(dataService, "validateYpatGeneration", qo, "约拍图片不能重复");
    }

    @Test
    public void ypatMultiImageSourceContractIsScopedToGenerationMethods() throws Exception {
        String service = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java");
        assertTrue(service.contains("private static final int YPAT_RESOURCE_LIMIT = 9;"));

        String generateYpats = methodBody(service,
                "public InternalTestBatchQo generateYpats(InternalTestGenerateQo qo)",
                "public InternalTestBatchQo generate(InternalTestGenerateQo qo)");
        assertTrue(generateYpats.contains("loadSelectedYpatResources(qo.getYpatResourceIds())"));
        assertTrue(generateYpats.contains("saveYpatImages(ypat, resources)"));
        assertTrue(generateYpats.contains("markResourcesUsed(resources, batchNo, \"ypat\", ypat.getId())"));
        assertFalse(generateYpats.contains("ensureResources("));
        assertFalse(generateYpats.contains("qo.getStyleCode()"));
        assertFalse(generateYpats.contains("pick("));
        assertFalse(generateYpats.contains("usedResources"));

        String createYpat = methodBody(service,
                "private YpatInfo createYpat(User user, InternalTestResource resource, InternalTestGenerateQo qo, String batchNo)",
                "private void saveYpatImages(YpatInfo ypat, List<InternalTestResource> resources)");
        assertTrue(createYpat.contains("resource.getDescription()"));
        assertFalse(createYpat.contains("ypatImgRepository.save"));

        String loadResources = methodBody(service,
                "private List<InternalTestResource> loadSelectedYpatResources(List<Long> resourceIds)",
                "private boolean isValidYpatStyle(String styleCode)");
        assertTrue(loadResources.contains("internalTestResourceRepository.findAll(resourceIds)"));
        assertTrue(loadResources.contains("new HashMap<Long, InternalTestResource>()"));
        assertFalse(loadResources.contains("styleCode"));
    }

    @Test
    public void selectedYpatResourcesAreLoadedInRequestOrderWithoutStyleFiltering() {
        InternalTestDataService dataService = new InternalTestDataService();
        RepositoryState state = new RepositoryState();
        state.selectedResources = Arrays.asList(
                ypatResource(3L, "enabled", 0, "ypat", "image"),
                ypatResource(1L, "enabled", 0, "ypat", "image"),
                ypatResource(2L, "enabled", 0, "ypat", "image"));
        ReflectionTestUtils.setField(dataService, "internalTestResourceRepository",
                repositoryProxy(Collections.<InternalTestResource>emptyList(),
                        Collections.<InternalTestResource>emptyList(), state));

        List<InternalTestResource> loaded = ReflectionTestUtils.invokeMethod(
                dataService, "loadSelectedYpatResources", Arrays.asList(2L, 1L, 3L));

        assertEquals(Arrays.asList(2L, 1L, 3L), state.findAllResourceIds);
        assertEquals(Arrays.asList(2L, 1L, 3L), resourceIds(loaded));
    }

    @Test
    public void selectedYpatResourcesReportPreciseValidationErrors() {
        assertSelectedYpatResourceError(Collections.<Long>singletonList(null),
                Collections.<InternalTestResource>emptyList(),
                "约拍资源不存在，请重新选择");
        assertSelectedYpatResourceError(Arrays.asList(1L, 2L),
                Collections.singletonList(ypatResource(1L, "enabled", 0, "ypat", "image")),
                "约拍资源不存在，请重新选择");
        assertSelectedYpatResourceError(Collections.singletonList(1L),
                Collections.singletonList(ypatResource(1L, "disabled", 0, "ypat", "image")),
                "约拍资源未启用，请重新选择");
        assertSelectedYpatResourceError(Collections.singletonList(1L),
                Collections.singletonList(ypatResource(1L, "enabled", 1, "ypat", "image")),
                "约拍资源已被占用，请重新选择");
        assertSelectedYpatResourceError(Collections.singletonList(1L),
                Collections.singletonList(ypatResource(1L, "enabled", 0, "work", "image")),
                "约拍资源用途错误");
        assertSelectedYpatResourceError(Collections.singletonList(1L),
                Collections.singletonList(ypatResource(1L, "enabled", 0, "ypat", "video")),
                "约拍资源包含视频");
    }

    @Test
    public void ypatGenerationCreatesOneYpatAndOrderedImagesThenMarksAllResourcesUsed() {
        InternalTestDataService dataService = new InternalTestDataService();
        RepositoryState resourceState = new RepositoryState();
        resourceState.selectedResources = Arrays.asList(
                ypatResource(3L, "enabled", 0, "ypat", "image"),
                ypatResource(1L, "enabled", 0, "ypat", "image"),
                ypatResource(2L, "enabled", 0, "ypat", "image"));
        ReflectionTestUtils.setField(dataService, "internalTestResourceRepository",
                repositoryProxy(Collections.<InternalTestResource>emptyList(),
                        Collections.<InternalTestResource>emptyList(), resourceState));

        final User user = new User();
        user.setId(7L);
        user.setDataFlag("internal_test");
        user.setProvince("浙江省");
        user.setCity("杭州市");
        user.setArea("西湖区");
        ReflectionTestUtils.setField(dataService, "userRepository", repositoryProxy(UserRepository.class,
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if (method.getDeclaringClass().equals(Object.class)) {
                            return objectMethod(proxy, method, args);
                        }
                        if ("findOne".equals(method.getName())) {
                            return user;
                        }
                        if ("save".equals(method.getName())) {
                            return args[0];
                        }
                        return defaultValue(method.getReturnType());
                    }
                }));

        final List<YpatInfo> savedYpats = new ArrayList<YpatInfo>();
        ReflectionTestUtils.setField(dataService, "ypatInfoRepository", repositoryProxy(YpatInfoRepository.class,
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if (method.getDeclaringClass().equals(Object.class)) {
                            return objectMethod(proxy, method, args);
                        }
                        if ("save".equals(method.getName())) {
                            YpatInfo ypat = (YpatInfo) args[0];
                            ypat.setId(99L);
                            savedYpats.add(ypat);
                            return ypat;
                        }
                        return defaultValue(method.getReturnType());
                    }
                }));

        final List<YpatImg> savedImages = new ArrayList<YpatImg>();
        ReflectionTestUtils.setField(dataService, "ypatImgRepository", repositoryProxy(YpatImgRepository.class,
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if (method.getDeclaringClass().equals(Object.class)) {
                            return objectMethod(proxy, method, args);
                        }
                        if ("save".equals(method.getName())) {
                            savedImages.add((YpatImg) args[0]);
                            return args[0];
                        }
                        return defaultValue(method.getReturnType());
                    }
                }));

        RecordingResourceService resourceService = new RecordingResourceService();
        ReflectionTestUtils.setField(dataService, "internalTestResourceService", resourceService);
        InternalTestGenerateQo qo = validYpatQo();
        qo.setBatchNo("BATCH-YPAT-3");
        qo.setYpatResourceIds(Arrays.asList(2L, 1L, 3L));
        qo.setStyleCode("must-not-filter-selected-resources");

        InternalTestBatchQo batch = dataService.generateYpats(qo);

        assertEquals(1, batch.getYpatCount().intValue());
        assertEquals(1, savedYpats.size());
        assertEquals(3, savedImages.size());
        assertEquals(Arrays.asList("https://example.com/2.jpg", "https://example.com/1.jpg", "https://example.com/3.jpg"),
                imagePaths(savedImages));
        assertEquals(1, resourceService.markResourcesUsedCallCount);
        assertEquals(Arrays.asList(2L, 1L, 3L), resourceIds(resourceService.resources));
        assertEquals("BATCH-YPAT-3", resourceService.batchNo);
        assertEquals("ypat", resourceService.targetType);
        assertEquals(Long.valueOf(99L), resourceService.targetId);
    }

    @Test
    public void dataGenerationDeclaresRollbackForExceptions() {
        Transactional transactional = InternalTestDataService.class.getAnnotation(Transactional.class);

        assertNotNull(transactional);
        assertTrue(Arrays.asList(transactional.rollbackFor()).contains(Exception.class));
    }

    @Test
    public void ypatGenerationPropagatesResourceMarkingFailure() {
        InternalTestDataService dataService = new InternalTestDataService();
        RepositoryState resourceState = new RepositoryState();
        resourceState.selectedResources = Collections.singletonList(
                ypatResource(1L, "enabled", 0, "ypat", "image"));
        ReflectionTestUtils.setField(dataService, "internalTestResourceRepository",
                repositoryProxy(Collections.<InternalTestResource>emptyList(),
                        Collections.<InternalTestResource>emptyList(), resourceState));
        ReflectionTestUtils.setField(dataService, "userRepository", internalUserRepository(internalUser(7L)));
        ReflectionTestUtils.setField(dataService, "ypatInfoRepository", ypatInfoRepositoryWithId(99L));
        ReflectionTestUtils.setField(dataService, "ypatImgRepository",
                passThroughSaveRepository(YpatImgRepository.class));

        RecordingResourceService resourceService = new RecordingResourceService();
        resourceService.markResourcesUsedException = new SysException(400, "mark failed");
        ReflectionTestUtils.setField(dataService, "internalTestResourceService", resourceService);

        try {
            dataService.generateYpats(validYpatQo());
        } catch (SysException e) {
            assertEquals("mark failed", e.getMsg());
            assertEquals(1, resourceService.markResourcesUsedCallCount);
            return;
        }
        fail("Expected resource marking failure to propagate");
    }

    @Test
    public void createUsersAllowsEmptyAvatarResourcePool() {
        InternalTestDataService service = new InternalTestDataService();
        final List<User> savedUsers = new ArrayList<User>();
        final int[] savedAvatarCount = {0};

        ReflectionTestUtils.setField(service, "internalTestResourceRepository",
                repositoryProxy(Collections.<InternalTestResource>emptyList(),
                        Collections.<InternalTestResource>emptyList(),
                        new RepositoryState()));
        ReflectionTestUtils.setField(service, "userRepository", repositoryProxy(UserRepository.class,
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if (method.getDeclaringClass().equals(Object.class)) {
                            return objectMethod(proxy, method, args);
                        }
                        if ("save".equals(method.getName()) && args[0] instanceof User) {
                            User user = (User) args[0];
                            user.setId((long) savedUsers.size() + 1L);
                            savedUsers.add(user);
                            return user;
                        }
                        return defaultValue(method.getReturnType());
                    }
                }));
        ReflectionTestUtils.setField(service, "userImgRepository", repositoryProxy(UserImgRepository.class,
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if (method.getDeclaringClass().equals(Object.class)) {
                            return objectMethod(proxy, method, args);
                        }
                        if ("save".equals(method.getName())) {
                            savedAvatarCount[0]++;
                            return args[0];
                        }
                        return defaultValue(method.getReturnType());
                    }
                }));

        InternalTestGenerateQo qo = new InternalTestGenerateQo();
        qo.setUserCount(3);
        qo.setNicknamePrefix("F-");
        qo.setGender("1");
        qo.setProfess("0");
        qo.setProvince("北京市");
        qo.setCity("北京市");
        qo.setArea("东城区");

        InternalTestBatchQo batch = service.createUsers(qo);

        assertEquals(3, batch.getUserCount().intValue());
        assertEquals(3, savedUsers.size());
        assertEquals(0, savedAvatarCount[0]);
        for (User user : savedUsers) {
            assertEquals("internal_test", user.getDataFlag());
            assertTrue(user.getAvatarurl() == null);
        }
    }

    @Test
    public void internalUserSearchIsPagedAndRestrictedToInternalUsers() throws Exception {
        String service = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java");
        String generateQo = read("backend/system-object/src/main/java/com/ypat/InternalTestGenerateQo.java");

        assertTrue(generateQo.contains("private String keyword;"));
        assertTrue(generateQo.contains("private Integer page;"));
        assertTrue(generateQo.contains("private Integer size;"));
        assertTrue(service.contains("public Map<String, Object> searchUsers(InternalTestGenerateQo qo)"));
        assertTrue(service.contains("Math.min(qo.getSize(), 50)"));
        assertTrue(service.contains("root.get(\"dataFlag\")"));
        assertTrue(service.contains("root.get(\"nickname\")"));
        assertTrue(service.contains("root.get(\"mobile\")"));
        assertTrue(service.contains("root.get(\"id\")"));
    }

    @Test
    public void internalUserActionsRequireInternalFlagAndUseRealBusinessServices() throws Exception {
        String service = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestUserActionService.java");
        String actionQo = read("backend/system-object/src/main/java/com/ypat/InternalTestUserActionQo.java");
        String member = read("backend/system-domain/src/main/java/com/ypat/service/MemberService.java");
        String deposit = read("backend/system-domain/src/main/java/com/ypat/service/DepositService.java");
        String depositRepo = read("backend/system-domain/src/main/java/com/ypat/repository/DepositOrderRepository.java");

        assertTrue(service.contains("grantMember"));
        assertTrue(service.contains("verifyUser"));
        assertTrue(service.contains("markDepositPaid"));
        assertTrue(service.contains("InternalTestDataFlag.internalTest.value.equals"));
        assertTrue(service.contains("memberService.adminGrant"));
        assertTrue(service.contains("depositService.createInternalTestPaidOrder"));
        assertTrue(service.contains("user.setRealnameflag"));
        assertTrue(service.contains("user.setCreditflag"));
        assertTrue(actionQo.contains("private Long operatorId;"));
        assertTrue(service.contains("qo.getOperatorId()"));
        assertTrue(service.contains("recordAdminOperation"));
        assertTrue(member.contains("public void recordAdminOperation("));
        assertTrue(service.contains("只能操作内测用户"));
        assertTrue(deposit.contains("createInternalTestPaidOrder"));
        assertTrue(deposit.contains("INTERNAL_TEST"));
        assertTrue(deposit.contains("findByUserIdAndChannelAndStatus"));
        assertTrue(deposit.contains("user.setCreditflag(\"1\")"));
        assertTrue(depositRepo.contains("DepositOrder findByUserIdAndChannelAndStatus"));
    }

    private void assertResourceColumnMigration(String sql, String columnName, String ddlFragment) {
        String block = migrationBlock(sql, "t_internal_test_resource", columnName);

        assertTrue(block.contains("SELECT IF(COUNT(*) = 0,"));
        assertTrue(block.contains("FROM information_schema.COLUMNS"));
        assertTrue(block.contains("TABLE_SCHEMA = DATABASE()"));
        assertTrue(block.contains("TABLE_NAME = 't_internal_test_resource'"));
        assertTrue(block.contains("COLUMN_NAME = '" + columnName + "'"));
        assertTrue(block.contains(ddlFragment));
        assertTrue(block.contains("SELECT ''skip t_internal_test_resource." + columnName + "''"));
        assertTrue(block.contains("PREPARE stmt FROM @ddl;"));
        assertTrue(block.contains("EXECUTE stmt;"));
        assertTrue(block.contains("DEALLOCATE PREPARE stmt;"));
    }

    private void assertResourceIndexMigration(String sql, String indexName, String ddlFragment) {
        String block = indexMigrationBlock(sql, "t_internal_test_resource", indexName);

        assertTrue(block.contains("FROM information_schema.STATISTICS"));
        assertTrue(block.contains("TABLE_SCHEMA = DATABASE()"));
        assertTrue(block.contains("TABLE_NAME = 't_internal_test_resource'"));
        assertTrue(block.contains("INDEX_NAME = '" + indexName + "'"));
        assertTrue(block.contains(ddlFragment));
        assertTrue(block.contains("PREPARE stmt FROM @ddl;"));
        assertTrue(block.contains("EXECUTE stmt;"));
        assertTrue(block.contains("DEALLOCATE PREPARE stmt;"));
    }

    private String migrationBlock(String sql, String tableName, String columnName) {
        int columnIndex = sql.indexOf("COLUMN_NAME = '" + columnName + "'");
        assertTrue("missing migration column guard for " + columnName, columnIndex >= 0);

        int start = sql.lastIndexOf("SET @ddl := (", columnIndex);
        int end = sql.indexOf("DEALLOCATE PREPARE stmt;", columnIndex);
        assertTrue("missing migration start for " + columnName, start >= 0);
        assertTrue("missing migration end for " + columnName, end >= 0);

        String block = sql.substring(start, end + "DEALLOCATE PREPARE stmt;".length());
        assertTrue(block.contains("TABLE_NAME = '" + tableName + "'"));
        return block;
    }

    private String indexMigrationBlock(String sql, String tableName, String indexName) {
        int index = sql.indexOf("INDEX_NAME = '" + indexName + "'");
        assertTrue("missing migration index guard for " + indexName, index >= 0);

        int start = sql.lastIndexOf("SET @ddl := (", index);
        int end = sql.indexOf("DEALLOCATE PREPARE stmt;", index);
        assertTrue("missing migration start for " + indexName, start >= 0);
        assertTrue("missing migration end for " + indexName, end >= 0);

        String block = sql.substring(start, end + "DEALLOCATE PREPARE stmt;".length());
        assertTrue(block.contains("TABLE_NAME = '" + tableName + "'"));
        return block;
    }

    private String methodBody(String source, String startMarker, String endMarker) {
        int start = source.indexOf(startMarker);
        int end = source.indexOf(endMarker, start);
        assertTrue("missing method start marker: " + startMarker, start >= 0);
        assertTrue("missing method end marker: " + endMarker, end > start);
        return source.substring(start, end);
    }

    private void assertAvailableResourceRepositoryMethod(String source, String methodName) {
        int methodIndex = source.indexOf(methodName + "(");
        assertTrue("missing repository method: " + methodName, methodIndex >= 0);
        int queryIndex = source.lastIndexOf("@Query(", methodIndex);
        int methodEnd = source.indexOf(";", methodIndex);
        assertTrue("missing query for repository method: " + methodName, queryIndex >= 0);
        assertTrue("missing repository method end: " + methodName, methodEnd > methodIndex);

        String block = source.substring(queryIndex, methodEnd + 1);
        assertTrue(methodName + " must filter usedFlag",
                block.contains("(:usedFlag is null or r.used_flag = :usedFlag)"));
        assertTrue(methodName + " must declare usedFlag",
                block.contains("@Param(\"usedFlag\") Integer usedFlag"));
        assertTrue(methodName + " must place usedFlag after keyword",
                block.indexOf("@Param(\"usedFlag\")") > block.indexOf("@Param(\"keyword\")"));
    }

    private InternalTestGenerateQo validYpatQo() {
        InternalTestGenerateQo qo = new InternalTestGenerateQo();
        qo.setUserId(7L);
        qo.setYpatResourceIds(Collections.singletonList(1L));
        qo.setPatdate("2099-01-01");
        qo.setPatslice("全天");
        qo.setProvince("浙江省");
        qo.setCity("杭州市");
        qo.setArea("西湖区");
        qo.setDescrib("约拍要求");
        qo.setWx("ypat-test");
        qo.setMobile("13800138000");
        qo.setStyleCodes(Collections.singletonList("0"));
        qo.setTarget("0");
        return qo;
    }

    private User internalUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setDataFlag("internal_test");
        user.setProvince("浙江省");
        user.setCity("杭州市");
        user.setArea("西湖区");
        return user;
    }

    private UserRepository internalUserRepository(final User user) {
        return repositoryProxy(UserRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if (method.getDeclaringClass().equals(Object.class)) {
                    return objectMethod(proxy, method, args);
                }
                if ("findOne".equals(method.getName())) {
                    return user;
                }
                if ("save".equals(method.getName())) {
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        });
    }

    private YpatInfoRepository ypatInfoRepositoryWithId(final Long id) {
        return repositoryProxy(YpatInfoRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if (method.getDeclaringClass().equals(Object.class)) {
                    return objectMethod(proxy, method, args);
                }
                if ("save".equals(method.getName())) {
                    YpatInfo ypat = (YpatInfo) args[0];
                    ypat.setId(id);
                    return ypat;
                }
                return defaultValue(method.getReturnType());
            }
        });
    }

    private <T> T passThroughSaveRepository(Class<T> repositoryType) {
        return repositoryProxy(repositoryType, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if (method.getDeclaringClass().equals(Object.class)) {
                    return objectMethod(proxy, method, args);
                }
                if ("save".equals(method.getName())) {
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        });
    }

    private void assertPrivateSysException(InternalTestDataService service,
                                           String methodName,
                                           InternalTestGenerateQo qo,
                                           String expectedMessage) {
        try {
            ReflectionTestUtils.invokeMethod(service, methodName, qo);
        } catch (SysException e) {
            assertEquals(expectedMessage, e.getMsg());
            return;
        }
        fail("Expected SysException: " + expectedMessage);
    }

    private void assertSelectedYpatResourceError(List<Long> requestedIds,
                                                 List<InternalTestResource> resources,
                                                 String expectedMessage) {
        InternalTestDataService service = new InternalTestDataService();
        RepositoryState state = new RepositoryState();
        state.selectedResources = resources;
        ReflectionTestUtils.setField(service, "internalTestResourceRepository",
                repositoryProxy(Collections.<InternalTestResource>emptyList(),
                        Collections.<InternalTestResource>emptyList(), state));
        try {
            ReflectionTestUtils.invokeMethod(service, "loadSelectedYpatResources", requestedIds);
        } catch (SysException e) {
            assertEquals(expectedMessage, e.getMsg());
            return;
        }
        fail("Expected SysException: " + expectedMessage);
    }

    private InternalTestResource ypatResource(Long id,
                                              String status,
                                              Integer usedFlag,
                                              String usageType,
                                              String mediaType) {
        InternalTestResource resource = new InternalTestResource();
        resource.setId(id);
        resource.setStatus(status);
        resource.setUsedFlag(usedFlag);
        resource.setUsageType(usageType);
        resource.setMediaType(mediaType);
        resource.setStyleCode("resource-style-" + id);
        resource.setDescription("resource-description-" + id);
        resource.setUrl("https://example.com/" + id + ".jpg");
        return resource;
    }

    private List<Long> resourceIds(List<InternalTestResource> resources) {
        List<Long> ids = new ArrayList<Long>();
        for (InternalTestResource resource : resources) {
            ids.add(resource.getId());
        }
        return ids;
    }

    private List<String> imagePaths(List<YpatImg> images) {
        List<String> paths = new ArrayList<String>();
        for (YpatImg image : images) {
            paths.add(image.getImgpath());
        }
        return paths;
    }

    private List<String> workMediaUrls(List<WorkMedia> mediaList) {
        List<String> urls = new ArrayList<String>();
        for (WorkMedia media : mediaList) {
            urls.add(media.getUrl());
        }
        return urls;
    }

    private InternalTestResource resource(Long id, String groupNo, String status, Integer usedFlag) {
        InternalTestResource resource = new InternalTestResource();
        resource.setId(id);
        resource.setGroupNo(groupNo);
        resource.setStatus(status);
        resource.setUsedFlag(usedFlag);
        resource.setUsageType("work");
        resource.setMediaType("image");
        resource.setUrl("https://example.com/" + id + ".jpg");
        resource.setGroupSortNo(id == null ? 0 : id.intValue());
        resource.setSortNo(id == null ? 0 : id.intValue());
        return resource;
    }

    private void assertMarkResourcesUsedFails(InternalTestResourceService service,
                                              List<InternalTestResource> resources,
                                              String batchNo,
                                              String targetType,
                                              Long targetId) {
        try {
            service.markResourcesUsed(resources, batchNo, targetType, targetId);
        } catch (SysException e) {
            return;
        }
        fail("Expected SysException");
    }

    private boolean containsGroupView(List<Map<String, Object>> groups, String groupNo) {
        return groupViewSize(groups, groupNo) > 0;
    }

    @SuppressWarnings("unchecked")
    private int groupViewSize(List<Map<String, Object>> groups, String groupNo) {
        for (Map<String, Object> group : groups) {
            if (groupNo.equals(group.get("groupNo"))) {
                return ((List<InternalTestResourceQo>) group.get("resources")).size();
            }
        }
        return 0;
    }

    private Integer groupViewUsedFlag(List<Map<String, Object>> groups, String groupNo) {
        for (Map<String, Object> group : groups) {
            if (groupNo.equals(group.get("groupNo"))) {
                return (Integer) group.get("usedFlag");
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private InternalTestResourceRepository repositoryProxy(final List<InternalTestResource> candidates,
                                                          final List<InternalTestResource> allGrouped,
                                                          final RepositoryState state) {
        return (InternalTestResourceRepository) Proxy.newProxyInstance(
                InternalTestResourceRepository.class.getClassLoader(),
                new Class[]{InternalTestResourceRepository.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if (method.getDeclaringClass().equals(Object.class)) {
                            return objectMethod(proxy, method, args);
                        }
                        if ("findAll".equals(method.getName())
                                && args != null
                                && args.length == 2
                                && args[1] instanceof Sort) {
                            return candidates;
                        }
                        if ("findAll".equals(method.getName())
                                && args != null
                                && args.length == 1
                                && args[0] instanceof List) {
                            state.findAllResourceIds = new ArrayList<Long>((List<Long>) args[0]);
                            return state.selectedResources;
                        }
                        if ("findByGroupNoIn".equals(method.getName())) {
                            return filterByGroupNos(allGrouped, (List<String>) args[0]);
                        }
                        if ("findOne".equals(method.getName())) {
                            return state.findOneResource;
                        }
                        if ("findAvailableGroupNos".equals(method.getName())) {
                            state.groupNoPageCallCount++;
                            state.findGroupNosUsedFlag = (Integer) args[8];
                            return page(filterGroupNosByUsedFlag(allGrouped, state.groupNosPage,
                                    state.findGroupNosUsedFlag), (Integer) args[9], (Integer) args[10]);
                        }
                        if ("countAvailableGroups".equals(method.getName())) {
                            state.countGroupsCallCount++;
                            state.countGroupsUsedFlag = (Integer) args[8];
                            return Long.valueOf(filterGroupNosByUsedFlag(allGrouped, state.groupNosPage,
                                    state.countGroupsUsedFlag).size());
                        }
                        if ("findAvailableSingleResources".equals(method.getName())) {
                            state.singlePageCallCount++;
                            state.findSinglesUsedFlag = (Integer) args[8];
                            return page(filterResourcesByUsedFlag(state.singleResourcesPage,
                                    state.findSinglesUsedFlag), (Integer) args[9], (Integer) args[10]);
                        }
                        if ("countAvailableSingleResources".equals(method.getName())) {
                            state.countSinglesCallCount++;
                            state.countSinglesUsedFlag = (Integer) args[8];
                            return Long.valueOf(filterResourcesByUsedFlag(state.singleResourcesPage,
                                    state.countSinglesUsedFlag).size());
                        }
                        if ("markResourcesUsedIfAvailable".equals(method.getName())) {
                            state.markUsedCallCount++;
                            return state.markUsedAffectedRows;
                        }
                        if ("save".equals(method.getName())) {
                            state.saveCount++;
                            return args[0];
                        }
                        return defaultValue(method.getReturnType());
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private <T> T repositoryProxy(Class<T> repositoryType, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(repositoryType.getClassLoader(), new Class[]{repositoryType}, handler);
    }

    private List<InternalTestResource> filterByGroupNos(List<InternalTestResource> resources, List<String> groupNos) {
        List<InternalTestResource> result = new ArrayList<InternalTestResource>();
        for (InternalTestResource resource : resources) {
            if (groupNos.contains(resource.getGroupNo())) {
                result.add(resource);
            }
        }
        return result;
    }

    private List<String> filterGroupNosByUsedFlag(List<InternalTestResource> resources,
                                                   List<String> groupNos,
                                                   Integer usedFlag) {
        List<String> result = new ArrayList<String>();
        for (String groupNo : groupNos) {
            boolean found = false;
            boolean matches = true;
            for (InternalTestResource resource : resources) {
                if (groupNo.equals(resource.getGroupNo())) {
                    found = true;
                    if (!matchesUsedFlag(resource, usedFlag)) {
                        matches = false;
                    }
                }
            }
            if (found && matches) {
                result.add(groupNo);
            }
        }
        return result;
    }

    private List<InternalTestResource> filterResourcesByUsedFlag(List<InternalTestResource> resources,
                                                                  Integer usedFlag) {
        List<InternalTestResource> result = new ArrayList<InternalTestResource>();
        for (InternalTestResource resource : resources) {
            if (matchesUsedFlag(resource, usedFlag)) {
                result.add(resource);
            }
        }
        return result;
    }

    private boolean matchesUsedFlag(InternalTestResource resource, Integer usedFlag) {
        return resource != null
                && "work".equals(resource.getUsageType())
                && "enabled".equals(resource.getStatus())
                && (usedFlag == null || usedFlag.equals(resource.getUsedFlag()));
    }

    private <T> List<T> page(List<T> values, int offset, int limit) {
        int fromIndex = Math.min(offset, values.size());
        int toIndex = Math.min(fromIndex + limit, values.size());
        return new ArrayList<T>(values.subList(fromIndex, toIndex));
    }

    private Object objectMethod(Object proxy, Method method, Object[] args) {
        if ("toString".equals(method.getName())) {
            return "InternalTestResourceRepositoryProxy";
        }
        if ("hashCode".equals(method.getName())) {
            return System.identityHashCode(proxy);
        }
        if ("equals".equals(method.getName())) {
            return proxy == args[0];
        }
        return null;
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (Boolean.TYPE.equals(type)) {
            return false;
        }
        if (Character.TYPE.equals(type)) {
            return '\0';
        }
        return 0;
    }

    private static class RepositoryState {
        private int saveCount;
        private int markUsedCallCount;
        private int markUsedAffectedRows;
        private int groupNoPageCallCount;
        private int countGroupsCallCount;
        private int singlePageCallCount;
        private int countSinglesCallCount;
        private List<String> groupNosPage = Collections.emptyList();
        private List<InternalTestResource> singleResourcesPage = Collections.emptyList();
        private Integer findGroupNosUsedFlag;
        private Integer countGroupsUsedFlag;
        private Integer findSinglesUsedFlag;
        private Integer countSinglesUsedFlag;
        private InternalTestResource findOneResource;
        private List<Long> findAllResourceIds = Collections.emptyList();
        private List<InternalTestResource> selectedResources = Collections.emptyList();
    }

    private static class RecordingResourceService extends InternalTestResourceService {
        private List<InternalTestResource> resources = Collections.emptyList();
        private String batchNo;
        private String targetType;
        private Long targetId;
        private int markResourcesUsedCallCount;
        private SysException markResourcesUsedException;

        @Override
        public void markResourcesUsed(List<InternalTestResource> resources,
                                      String batchNo,
                                      String targetType,
                                      Long targetId) {
            markResourcesUsedCallCount++;
            this.resources = new ArrayList<InternalTestResource>(resources);
            this.batchNo = batchNo;
            this.targetType = targetType;
            this.targetId = targetId;
            if (markResourcesUsedException != null) {
                throw markResourcesUsedException;
            }
        }
    }

    private void assertReadableWritable(Class<?> type, String fieldName, Class<?> fieldType) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        assertEquals(fieldType, field.getType());

        String accessorSuffix = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method getter = type.getMethod("get" + accessorSuffix);
        assertEquals(fieldType, getter.getReturnType());

        Method setter = type.getMethod("set" + accessorSuffix, fieldType);
        assertEquals(Void.TYPE, setter.getReturnType());
    }

    private void assertTransientField(Class<?> type, String fieldName) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        assertNotNull(field.getAnnotation(Transient.class));
    }

    private void assertColumnName(Class<?> type, String fieldName, String columnName) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        Column column = field.getAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(columnName, column.name());
    }

    private void assertEntityMarkers(String path) throws Exception {
        String source = read(path);

        assertTrue(source.contains("private String dataFlag;"));
        assertTrue(source.contains("private String internalBatchNo;"));
    }

    private void assertDtoMarkers(String path) throws Exception {
        String source = read(path);

        assertTrue(source.contains("dataFlag"));
        assertTrue(source.contains("internalBatchNo"));
    }

    private void assertEnumValues(String path, String... values) throws Exception {
        String source = read(path);

        for (String value : values) {
            assertTrue(source.contains("\"" + value + "\""));
        }
    }
}
