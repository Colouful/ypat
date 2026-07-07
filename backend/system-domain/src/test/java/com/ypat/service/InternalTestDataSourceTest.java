package com.ypat.service;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
