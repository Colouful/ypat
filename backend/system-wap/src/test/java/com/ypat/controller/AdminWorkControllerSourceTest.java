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
        String source = readSource();

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

    private String readSource() throws IOException {
        Path path = Paths.get("src/main/java/com/ypat/controller/AdminWorkController.java");
        if (!Files.exists(path)) {
            path = Paths.get("backend/system-wap/src/main/java/com/ypat/controller/AdminWorkController.java");
        }
        assertTrue("AdminWorkController.java should exist", Files.exists(path));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
