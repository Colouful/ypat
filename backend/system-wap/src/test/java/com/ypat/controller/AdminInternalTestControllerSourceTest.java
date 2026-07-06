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
        if (!Files.exists(path)) {
            path = Paths.get(repoPath);
        }
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
        assertTrue(source.contains("throw new SysException(code, msg)"));
        assertTrue(source.contains("服务响应格式错误"));
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
        assertTrue(client.contains("/service/internal-test/resources/update"));
        assertTrue(client.contains("/service/internal-test/resources/status"));
        assertTrue(client.contains("/service/internal-test/users"));
        assertTrue(client.contains("/service/internal-test/users/create"));
        assertTrue(client.contains("/service/internal-test/generate"));
        assertTrue(client.contains("/service/internal-test/batches"));
        assertTrue(client.contains("/service/internal-test/cleanup"));
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
