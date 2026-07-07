package com.ypat.controller;

import com.ypat.WorkComplainQo;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AdminWorkComplainControllerSourceTest {

    @Test
    public void adminWorkComplainControllerExposesRoutesAndDelegatesToFeignClient() throws IOException {
        String source = readSource(
                "src/main/java/com/ypat/controller/AdminWorkComplainController.java",
                "backend/system-wap/src/main/java/com/ypat/controller/AdminWorkComplainController.java",
                "AdminWorkComplainController.java should exist");

        assertTrue(source.contains("@RestController"));
        assertTrue(source.contains("@RequestMapping(\"/admin/work/complain\")"));
        assertTrue(source.contains("@GetMapping(\"/list\")"));
        assertTrue(source.contains("@GetMapping(\"/detail\")"));
        assertTrue(source.contains("@PostMapping(\"/handle\")"));
        assertTrue(source.contains("workComplainAdminServiceClient.list("));
        assertTrue(source.contains("workComplainAdminServiceClient.detail(id)"));
        assertTrue(source.contains("workComplainAdminServiceClient.handle(qo)"));
        assertTrue(source.contains("return ResponseApiBody.success(parseResponseRes(json))"));
        assertFalse(source.contains("ResponseApiBody.success(JsonParser.parseString(json))"));
        assertFalse(source.contains("ResponseApiBody.success(JsonParser.parseString(res))"));
    }

    @Test
    public void adminWorkComplainControllerNormalizesPageSizeAndValidatesHandleInputs() throws IOException {
        String source = readSource(
                "src/main/java/com/ypat/controller/AdminWorkComplainController.java",
                "backend/system-wap/src/main/java/com/ypat/controller/AdminWorkComplainController.java",
                "AdminWorkComplainController.java should exist");

        assertTrue(source.contains("private static final int DEFAULT_PAGE = 0;"));
        assertTrue(source.contains("private static final int DEFAULT_SIZE = 10;"));
        assertTrue(source.contains("private static final int MAX_SIZE = 50;"));
        assertTrue(source.contains("return page == null || page < 0 ? DEFAULT_PAGE : page;"));
        assertTrue(source.contains("return Math.min(size, MAX_SIZE);"));
        assertTrue(source.contains("if (id == null || id <= 0)"));
        assertTrue(source.contains("if (StringUtils.isBlank(status))"));
        assertTrue(source.contains("qo.setOfflineWork(offlineWork)"));
        assertTrue(source.contains("qo.setStatus(status)"));
        assertTrue(source.contains("qo.setReason(reason)"));
    }

    @Test
    public void adminWorkComplainFeignClientUsesSystemApiAdminPaths() throws IOException {
        String source = readSource(
                "src/main/java/com/ypat/service/WorkComplainAdminServiceClient.java",
                "backend/system-wap/src/main/java/com/ypat/service/WorkComplainAdminServiceClient.java",
                "WorkComplainAdminServiceClient.java should exist");

        assertTrue(source.contains("@FeignClient(\"SYSTEM-API\")"));
        assertTrue(source.contains("@GetMapping(\"/service/work/complain/admin/list\")"));
        assertTrue(source.contains("@GetMapping(\"/service/work/complain/admin/detail\")"));
        assertTrue(source.contains("@PostMapping(\"/service/work/complain/admin/handle\")"));
        assertTrue(source.contains("String list("));
        assertTrue(source.contains("String detail(@RequestParam(\"id\") Long id);"));
        assertTrue(source.contains("String handle(@RequestBody WorkComplainQo qo);"));
    }

    @Test
    public void webSecurityConfigDoesNotOpenAnonymousAdminComplainEndpoints() throws IOException {
        String source = readSource(
                "src/main/java/com/ypat/config/WebSecurityConfig.java",
                "backend/system-wap/src/main/java/com/ypat/config/WebSecurityConfig.java",
                "WebSecurityConfig.java should exist");

        assertTrue(source.contains(".antMatchers(\"/admin/**\").hasRole(\"ADMIN\")"));
        assertFalse(source.contains("\"/admin/work/complain"));
    }

    @Test
    public void internalRestapiExposesAdminComplaintEndpoints() throws IOException {
        String source = readSource(
                "../system-restapi/src/main/java/com/ypat/controller/WorkComplainController.java",
                "backend/system-restapi/src/main/java/com/ypat/controller/WorkComplainController.java",
                "WorkComplainController.java should exist");

        assertTrue(source.contains("@GetMapping(\"/complain/admin/list\")"));
        assertTrue(source.contains("@GetMapping(\"/complain/admin/detail\")"));
        assertTrue(source.contains("@PostMapping(\"/complain/admin/handle\")"));
        assertTrue(source.contains("workComplainService.adminList("));
        assertTrue(source.contains("workComplainService.adminDetail(id)"));
        assertTrue(source.contains("workComplainService.adminHandle(qo)"));
        assertTrue(source.contains("throw new SysException(ResponseCode.FAIL_PARA)"));
    }

    @Test
    public void domainServiceAddsAdminComplaintFlowAndOfflineWorkHandling() throws IOException {
        String source = readSource(
                "../system-domain/src/main/java/com/ypat/service/WorkComplainService.java",
                "backend/system-domain/src/main/java/com/ypat/service/WorkComplainService.java",
                "WorkComplainService.java should exist");

        assertTrue(source.contains("public Map<String, Object> adminList("));
        assertTrue(source.contains("public Map<String, Object> adminDetail(Long id)"));
        assertTrue(source.contains("public void adminHandle(WorkComplainQo qo)"));
        assertTrue(source.contains("qo.getOfflineWork()"));
        assertTrue(source.contains("workService.adminOffline("));
        assertTrue(source.contains("int updated = workComplainRepository.updatePendingStatus(qo.getId(), targetStatus);"));
        assertTrue(source.contains("if (updated <= 0)"));
        assertTrue(source.contains("投诉已处理"));
        assertTrue(source.contains("handled"));
        assertTrue(source.contains("rejected"));
        assertFalse(source.contains("workRepository.updateStatusAndAuditReason("));
    }

    @Test
    public void repositorySupportsAdminComplaintPaginationAndStatusUpdate() throws IOException {
        String source = readSource(
                "../system-domain/src/main/java/com/ypat/repository/WorkComplainRepository.java",
                "backend/system-domain/src/main/java/com/ypat/repository/WorkComplainRepository.java",
                "WorkComplainRepository.java should exist");

        assertTrue(source.contains("JpaSpecificationExecutor<WorkComplain>"));
        assertTrue(source.contains("updatePendingStatus("));
        assertTrue(source.contains("c.status = '0'"));
    }

    @Test
    public void miniProgramComplaintPageCombinesReasonAndContentBeforeSubmit() throws IOException {
        String source = readSource(
                "../../91pai-master/pages/work/complain/index.js",
                "91pai-master/pages/work/complain/index.js",
                "91pai complain page should exist");

        assertTrue(source.contains("const submitReason ="));
        assertTrue(source.contains("投诉原因：${this.reason}"));
        assertTrue(source.contains("投诉内容：${complainContent}"));
        assertTrue(source.contains("用户已上传证据截图"));
        assertTrue(source.contains("reason: submitReason"));
    }

    @Test
    public void workComplainQoExposesAdminFieldsAndAccessors() throws Exception {
        Field idField = WorkComplainQo.class.getDeclaredField("id");
        Field statusField = WorkComplainQo.class.getDeclaredField("status");
        Field offlineWorkField = WorkComplainQo.class.getDeclaredField("offlineWork");

        assertNotNull(idField);
        assertNotNull(statusField);
        assertNotNull(offlineWorkField);

        Method getId = WorkComplainQo.class.getDeclaredMethod("getId");
        Method setId = WorkComplainQo.class.getDeclaredMethod("setId", Long.class);
        Method getStatus = WorkComplainQo.class.getDeclaredMethod("getStatus");
        Method setStatus = WorkComplainQo.class.getDeclaredMethod("setStatus", String.class);
        Method getOfflineWork = WorkComplainQo.class.getDeclaredMethod("getOfflineWork");
        Method setOfflineWork = WorkComplainQo.class.getDeclaredMethod("setOfflineWork", Boolean.class);

        assertNotNull(getId);
        assertNotNull(setId);
        assertNotNull(getStatus);
        assertNotNull(setStatus);
        assertNotNull(getOfflineWork);
        assertNotNull(setOfflineWork);
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
