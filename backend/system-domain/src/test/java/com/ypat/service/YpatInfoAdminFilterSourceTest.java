package com.ypat.service;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class YpatInfoAdminFilterSourceTest {
    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    @Test
    public void ypatAdminFiltersIncludeNewPublishFields() throws Exception {
        String source = read("src/main/java/com/ypat/service/YpatInfoService.java");

        assertTrue(source.contains("queryQo.getTarget()"));
        assertTrue(source.contains("root.get(\"target\")"));
        assertTrue(source.contains("queryQo.getPatstyle()"));
        assertTrue(source.contains("root.get(\"patstyle\")"));
        assertTrue(source.contains("queryQo.getChargeway()"));
        assertTrue(source.contains("root.get(\"chargeway\")"));
        assertTrue(source.contains("queryQo.getWorkId()"));
        assertTrue(source.contains("root.get(\"workId\")"));
    }

    @Test
    public void ypatAdminWorkIdFilterRejectsInvalidInputWithBusinessError() throws Exception {
        String source = read("src/main/java/com/ypat/service/YpatInfoService.java");

        assertTrue(source.contains("catch (NumberFormatException"));
        assertTrue(source.contains("workId <= 0"));
        assertTrue(source.contains("new SysException(ResponseCode.FAIL_PARA"));
        assertTrue(source.contains("workId参数错误"));
    }

    @Test
    public void ypatAdminPatstyleFilterMatchesCommaSeparatedBoundaries() throws Exception {
        String source = read("src/main/java/com/ypat/service/YpatInfoService.java");

        assertFalse(source.contains("criteriaBuilder.like(root.get(\"patstyle\"), \"%\" + queryQo.getPatstyle() + \"%\")"));
        assertTrue(source.contains("queryQo.getPatstyle().split(\",\")"));
        assertTrue(source.contains(".trim()"));
        assertTrue(source.contains("Set<String> patstyles"));
        assertTrue(source.contains("patstyles.add(patstyle)"));
        assertTrue(source.contains("new SysException(ResponseCode.FAIL_PARA, \"patstyle参数错误\")"));
        assertTrue(source.contains("criteriaBuilder.or("));
        assertTrue(source.contains("criteriaBuilder.equal(root.get(\"patstyle\"), patstyle)"));
        assertTrue(source.contains("criteriaBuilder.like(root.get(\"patstyle\"), patstyle + \",%\")"));
        assertTrue(source.contains("criteriaBuilder.like(root.get(\"patstyle\"), \"%,\" + patstyle)"));
        assertTrue(source.contains("criteriaBuilder.like(root.get(\"patstyle\"), \"%,\" + patstyle + \",%\")"));
    }

    @Test
    public void ypatAdminAuditOnlyUpdatesReviewFields() throws Exception {
        String source = read("src/main/java/com/ypat/service/YpatInfoService.java");
        String auditMethod = methodBody(source,
                "public void audit(Long id, String flag, String recomflag, String reason)",
                "public void delete(Long id)");

        assertTrue(auditMethod.contains("info.setStatus(flag)"));
        assertTrue(auditMethod.contains("info.setRecomflag(recomflag)"));
        assertTrue(auditMethod.contains("info.setReason(reason)"));
        assertFalse(auditMethod.contains("setTarget"));
        assertFalse(auditMethod.contains("setPatstyle"));
        assertFalse(auditMethod.contains("setChargeway"));
        assertFalse(auditMethod.contains("setWorkId"));
    }

    private String methodBody(String source, String startToken, String endToken) {
        int start = source.indexOf(startToken);
        assertTrue(start >= 0);
        int end = source.indexOf(endToken, start);
        assertTrue(end > start);
        return source.substring(start, end);
    }
}
