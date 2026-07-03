package com.ypat.controller;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class AdminWorkControllerSourceTest {

    @Test
    public void adminWorkControllerExposesAdminRoutesAndDelegatesToWorkClient() throws IOException {
        String source = readSource(
                "src/main/java/com/ypat/controller/AdminWorkController.java",
                "backend/system-wap/src/main/java/com/ypat/controller/AdminWorkController.java",
                "AdminWorkController.java should exist");

        assertTrue(source.contains("@RestController"));
        assertTrue(source.contains("@RequestMapping(\"/admin/work\")"));
        assertTrue(source.contains("@GetMapping(\"/list\")"));
        assertTrue(source.contains("@GetMapping(\"/detail\")"));
        assertTrue(source.contains("@PostMapping(\"/audit\")"));
        assertTrue(source.contains("@PostMapping(\"/offline\")"));
        assertTrue(source.contains("workServiceClient.adminList(qo)"));
        assertTrue(source.contains("workServiceClient.adminDetail(id)"));
        assertTrue(source.contains("workServiceClient.adminAudit(id, flag, reason)"));
        assertTrue(source.contains("workServiceClient.adminOffline(id, reason)"));
    }

    @Test
    public void internalRestapiExposesAdminWorkEndpointsAndDelegatesToDomainService() throws IOException {
        String source = readSource(
                "../system-restapi/src/main/java/com/ypat/controller/WorkController.java",
                "backend/system-restapi/src/main/java/com/ypat/controller/WorkController.java",
                "WorkController.java should exist");

        assertTrue(source.contains("@PostMapping(\"/admin/list\")"));
        assertTrue(source.contains("public ResponseApiBody adminList(@RequestBody WorkListQo qo)"));
        assertTrue(source.contains("workService.adminPageList(qo)"));
        assertTrue(source.contains("@GetMapping(\"/admin/detail\")"));
        assertTrue(source.contains("public ResponseApiBody adminDetail(@RequestParam(\"id\") Long id)"));
        assertTrue(source.contains("workService.adminDetail(id)"));
        assertTrue(source.contains("@PostMapping(\"/admin/audit\")"));
        assertTrue(source.contains("public ResponseApiBody adminAudit(@RequestParam(\"id\") Long id,"));
        assertTrue(source.contains("workService.adminAudit(id, flag, reason)"));
        assertTrue(source.contains("@PostMapping(\"/admin/offline\")"));
        assertTrue(source.contains("public ResponseApiBody adminOffline(@RequestParam(\"id\") Long id,"));
        assertTrue(source.contains("workService.adminOffline(id, reason)"));
    }

    @Test
    public void workServiceClientDeclaresAdminWorkFeignMethods() throws IOException {
        String source = readSource(
                "src/main/java/com/ypat/service/WorkServiceClient.java",
                "backend/system-wap/src/main/java/com/ypat/service/WorkServiceClient.java",
                "WorkServiceClient.java should exist");

        assertTrue(source.contains("@PostMapping(\"/service/work/admin/list\")"));
        assertTrue(source.contains("String adminList(@RequestBody WorkListQo qo);"));
        assertTrue(source.contains("@GetMapping(\"/service/work/admin/detail\")"));
        assertTrue(source.contains("String adminDetail(@RequestParam(\"id\") Long id);"));
        assertTrue(source.contains("@PostMapping(\"/service/work/admin/audit\")"));
        assertTrue(source.contains("String adminAudit(@RequestParam(\"id\") Long id,"));
        assertTrue(source.contains("@RequestParam(\"flag\") String flag,"));
        assertTrue(source.contains("@RequestParam(\"reason\") String reason);"));
        assertTrue(source.contains("@PostMapping(\"/service/work/admin/offline\")"));
        assertTrue(source.contains("String adminOffline(@RequestParam(\"id\") Long id,"));
        assertTrue(source.contains("@RequestParam(\"reason\") String reason);"));
    }

    private String readSource(String modulePath, String repoPath, String message) throws IOException {
        Path path = Paths.get(modulePath);
        if (!Files.exists(path)) {
            path = Paths.get(repoPath);
        }
        assertTrue(message, Files.exists(path));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
