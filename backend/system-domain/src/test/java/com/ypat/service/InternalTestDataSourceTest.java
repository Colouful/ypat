package com.ypat.service;

import com.ypat.InternalTestGenerateQo;
import com.ypat.InternalTestResourceQo;
import com.ypat.InternalTestUserActionQo;
import com.ypat.SysException;
import com.ypat.entity.InternalTestResource;
import com.ypat.repository.InternalTestResourceRepository;
import org.junit.Test;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.Column;
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
                "ALTER TABLE `t_internal_test_resource` ADD INDEX `idx_internal_resource_available_group` (`usage_type`, `status`, `used_flag`, `group_no`, `sort_no`, `id`)");
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
        assertTrue(repo.contains("List<String> findAvailableGroupNos("));
        assertTrue(repo.contains("Long countAvailableGroups("));
        assertTrue(repo.contains("List<InternalTestResource> findAvailableSingleResources("));
        assertTrue(repo.contains("Long countAvailableSingleResources("));
        assertFalse(repo.contains("coalesce(r.used_flag"));
        assertFalse(repo.contains("select count(*) from t_internal_test_resource all_r"));
        assertTrue(repo.contains("r.used_flag = 0"));
        assertTrue(repo.contains("join (select group_no, count(*) total_count"));

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
    public void listAvailableGroupsExcludesPartiallyAvailableWorkGroups() {
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
        state.availableGroupCount = 1L;
        state.availableSingleCount = 1L;
        state.singleResourcesPage = Collections.singletonList(resource(5L, null, "enabled", 0));
        ReflectionTestUtils.setField(service, "internalTestResourceRepository",
                repositoryProxy(candidates, allGrouped, state));

        InternalTestResourceQo qo = new InternalTestResourceQo();
        qo.setPage(0);
        qo.setSize(10);

        Map<String, Object> result = service.listAvailableGroups(qo);
        List<List<InternalTestResourceQo>> content = (List<List<InternalTestResourceQo>>) result.get("content");

        assertEquals(2, content.size());
        assertFalse(containsGroup(content, "G1"));
        assertTrue(containsGroup(content, "G2"));
        assertEquals(2, groupSize(content, "G2"));
        assertTrue(containsSingle(content, 5L));
        assertEquals(2, ((Number) result.get("totalElements")).intValue());
        assertEquals(1, ((Number) result.get("totalPages")).intValue());
        assertEquals(1, state.groupNoPageCallCount);
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
        assertTrue(service.contains("createWorkFromGroup"));
        assertTrue(service.contains("markResourcesUsed"));
        assertTrue(service.contains("markSingleResourceUsed(avatar, batchNo, \"user\", user.getId())"));
        assertTrue(service.contains("releaseResourcesByBatch"));
        assertTrue(service.contains("setReleasedResourceCount"));
        assertTrue(service.contains("setWx(qo.getWx())"));
        assertTrue(service.contains("setMobile(qo.getMobile())"));
        assertTrue(service.contains("String.join(\",\", qo.getStyleCodes())"));
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
        assertTrue(generate.contains("users.size()"));
    }

    @Test
    public void internalUserActionsRequireInternalFlagAndUseRealBusinessServices() throws Exception {
        String service = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestUserActionService.java");
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

    private boolean containsGroup(List<List<InternalTestResourceQo>> groups, String groupNo) {
        return groupSize(groups, groupNo) > 0;
    }

    private int groupSize(List<List<InternalTestResourceQo>> groups, String groupNo) {
        for (List<InternalTestResourceQo> group : groups) {
            if (!group.isEmpty() && groupNo.equals(group.get(0).getGroupNo())) {
                return group.size();
            }
        }
        return 0;
    }

    private boolean containsSingle(List<List<InternalTestResourceQo>> groups, Long id) {
        for (List<InternalTestResourceQo> group : groups) {
            if (group.size() == 1 && id.equals(group.get(0).getId())) {
                return true;
            }
        }
        return false;
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
                        if ("findByGroupNoIn".equals(method.getName())) {
                            return filterByGroupNos(allGrouped, (List<String>) args[0]);
                        }
                        if ("findOne".equals(method.getName())) {
                            return state.findOneResource;
                        }
                        if ("findAvailableGroupNos".equals(method.getName())) {
                            state.groupNoPageCallCount++;
                            return state.groupNosPage;
                        }
                        if ("countAvailableGroups".equals(method.getName())) {
                            return state.availableGroupCount;
                        }
                        if ("findAvailableSingleResources".equals(method.getName())) {
                            return state.singleResourcesPage;
                        }
                        if ("countAvailableSingleResources".equals(method.getName())) {
                            return state.availableSingleCount;
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

    private List<InternalTestResource> filterByGroupNos(List<InternalTestResource> resources, List<String> groupNos) {
        List<InternalTestResource> result = new ArrayList<InternalTestResource>();
        for (InternalTestResource resource : resources) {
            if (groupNos.contains(resource.getGroupNo())) {
                result.add(resource);
            }
        }
        return result;
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
        private List<String> groupNosPage = Collections.emptyList();
        private List<InternalTestResource> singleResourcesPage = Collections.emptyList();
        private Long availableGroupCount = 0L;
        private Long availableSingleCount = 0L;
        private InternalTestResource findOneResource;
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
