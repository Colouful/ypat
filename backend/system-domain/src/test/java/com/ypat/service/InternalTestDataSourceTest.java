package com.ypat.service;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class InternalTestDataSourceTest {
    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
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
