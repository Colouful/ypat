package com.ypat.service;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class WorkServiceAdminSourceTest {
    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    @Test
    public void adminWorkMethodsUseAdminScopeAndDoNotReusePublicApprovalFilter() throws Exception {
        String source = read("src/main/java/com/ypat/service/WorkService.java");

        assertTrue(source.contains("public Map<String, Object> adminPageList(WorkListQo qo)"));
        assertTrue(source.contains("public Map<String, Object> adminDetail(Long workId)"));
        assertTrue(source.contains("public void adminAudit(Long workId, String flag, String reason)"));
        assertTrue(source.contains("public void adminOffline(Long workId, String reason)"));
        assertTrue(source.contains("WorkStatus.isValid(flag)"));
        assertTrue(source.contains("workRepository.updateStatusAndAuditReason(workId, flag, reason)"));
        assertTrue(source.contains("workRepository.updateStatusAndAuditReason(workId, WorkStatus.xj.value, reason)"));
    }

    @Test
    public void repositorySupportsAuditReasonStatusUpdate() throws Exception {
        String source = read("src/main/java/com/ypat/repository/WorkRepository.java");

        assertTrue(source.contains("int updateStatusAndAuditReason"));
        assertTrue(source.contains("auditReason = :auditReason"));
        assertTrue(source.contains("deletedFlag = 0"));
    }
}
