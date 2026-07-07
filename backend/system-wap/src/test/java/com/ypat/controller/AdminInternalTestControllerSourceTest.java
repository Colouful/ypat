package com.ypat.controller;

import com.google.gson.JsonElement;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
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
        String wap = readSource(
                "src/main/java/com/ypat/controller/AdminInternalTestController.java",
                "backend/system-wap/src/main/java/com/ypat/controller/AdminInternalTestController.java");
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
        assertThreeLayerGetRoute(wap, client, restapi, "resources");
        assertThreeLayerGetRoute(wap, client, restapi, "users");
        assertThreeLayerGetRoute(wap, client, restapi, "batches");
        assertThreeLayerPostRoute(wap, client, restapi, "resources");
        assertThreeLayerPostRoute(wap, client, restapi, "resources/update");
        assertThreeLayerPostRoute(wap, client, restapi, "users/create");
        assertThreeLayerPostRoute(wap, client, restapi, "generate");
        assertThreeLayerPostRoute(wap, client, restapi, "cleanup");
        assertRequestBodyRoute(client, "InternalTestResourceQo qo");
        assertRequestBodyRoute(restapi, "InternalTestResourceQo qo");
        assertRequestBodyRoute(client, "InternalTestGenerateQo qo");
        assertRequestBodyRoute(restapi, "InternalTestGenerateQo qo");
        assertStatusRouteUsesIdAndStatus(client);
        assertStatusRouteUsesIdAndStatus(restapi);
    }

    @Test
    public void restapiStatusRouteReturnsObjectPayloadForWapParserContract() throws Exception {
        String restapi = readSource(
                "../system-restapi/src/main/java/com/ypat/controller/InternalTestController.java",
                "backend/system-restapi/src/main/java/com/ypat/controller/InternalTestController.java");

        assertFalse(restapi.contains("ResponseApiBody.success(\"更新成功\")"));
        assertTrue(restapi.contains("Map<String, Object>"));
        assertTrue(restapi.contains("result.put(\"success\", true)"));
        assertTrue(restapi.contains("return ResponseApiBody.success(result)"));
    }

    @Test
    public void parseResponseResReturnsExpectedDataAndPropagatesBusinessErrors() throws Exception {
        AdminInternalTestController controller = new AdminInternalTestController();

        JsonElement data = invokeParseResponseRes(controller, "{\"code\":200,\"res\":{\"ok\":true}}");
        assertTrue(data.isJsonObject());
        assertTrue(data.getAsJsonObject().get("ok").getAsBoolean());

        assertParseResponseFail(controller, "{\"code\":1002,\"msg\":\"参数错误\"}", 1002, "参数错误");
    }

    @Test
    public void parseResponseResRejectsMalformedResponses() throws Exception {
        AdminInternalTestController controller = new AdminInternalTestController();

        assertParseResponseFail(controller, "{\"content\":[],\"totalElements\":0}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"res\":{\"ok\":true}}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":500,\"msg\":{}}", ResponseCode.FAIL_SER.getMsg());
        assertParseResponseFail(controller, "{\"code\":500,\"msg\":[]}", ResponseCode.FAIL_SER.getMsg());
        assertParseResponseFail(controller, "{\"code\":500,\"msg\":null}", ResponseCode.FAIL_SER.getMsg());
        assertParseResponseFail(controller, "{\"code\":500,\"msg\":123}", ResponseCode.FAIL_SER.getMsg());
        assertParseResponseFail(controller, "{\"code\":500,\"msg\":true}", ResponseCode.FAIL_SER.getMsg());
        assertParseResponseFail(controller, "{\"code\":null,\"msg\":\"bad\"}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":\"x\",\"msg\":\"bad\"}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200,\"msg\":\"ok\"}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200,\"res\":null}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200,\"res\":[]}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200,\"res\":123}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200,\"res\":true}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200,\"res\":\"x\"}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200,\"res\":\"更新成功\"}", "服务响应格式错误");
        assertParseResponseFail(controller, "[]", "服务响应格式错误");
        assertParseResponseFail(controller, "123", "服务响应格式错误");
        assertParseResponseFail(controller, "null", "服务响应格式错误");
        assertParseResponseFail(controller, "{not-json", "服务响应格式错误");
        assertParseResponseFail(controller, "", "服务响应格式错误");
    }

    @Test
    public void normalizePageAndSizeClampToSupportedRange() throws Exception {
        AdminInternalTestController controller = new AdminInternalTestController();

        assertEquals(0, invokeNormalizePage(controller, -1));
        assertEquals(50, invokeNormalizeSize(controller, 99));
    }

    @Test
    public void legacyAdminInternalTestControllerIsNotIntroduced() throws Exception {
        assertFalse(sourceExists(
                "../system-web/src/main/java/com/ypat/controller/AdminInternalTestController.java",
                "backend/system-web/src/main/java/com/ypat/controller/AdminInternalTestController.java"));
    }

    private boolean sourceExists(String modulePath, String repoPath) {
        return Files.exists(Paths.get(modulePath)) || Files.exists(Paths.get(repoPath));
    }

    private void assertThreeLayerGetRoute(String wap, String client, String restapi, String route) {
        assertTrue(wap.contains("@GetMapping(\"/" + route + "\")"));
        assertTrue(client.contains("@GetMapping(\"/service/internal-test/" + route + "\")"));
        assertTrue(restapi.contains("@GetMapping(\"/" + route + "\")"));
    }

    private void assertThreeLayerPostRoute(String wap, String client, String restapi, String route) {
        assertTrue(wap.contains("@PostMapping(\"/" + route + "\")"));
        assertTrue(client.contains("@PostMapping(\"/service/internal-test/" + route + "\")"));
        assertTrue(restapi.contains("@PostMapping(\"/" + route + "\")"));
    }

    private void assertRequestBodyRoute(String source, String argument) {
        assertTrue(source.contains("@RequestBody " + argument));
    }

    private void assertStatusRouteUsesIdAndStatus(String source) {
        assertTrue(source.contains("@PostMapping(\"/service/internal-test/resources/status\")")
                || source.contains("@PostMapping(\"/resources/status\")"));
        assertTrue(source.contains("@RequestParam(\"id\") Long id"));
        assertTrue(source.contains("@RequestParam(\"status\") String status"));
    }

    private JsonElement invokeParseResponseRes(AdminInternalTestController controller, String json) throws Exception {
        Method method = AdminInternalTestController.class.getDeclaredMethod("parseResponseRes", String.class);
        method.setAccessible(true);
        return (JsonElement) method.invoke(controller, json);
    }

    private int invokeNormalizePage(AdminInternalTestController controller, Integer page) throws Exception {
        Method method = AdminInternalTestController.class.getDeclaredMethod("normalizePage", Integer.class);
        method.setAccessible(true);
        return (Integer) method.invoke(controller, page);
    }

    private int invokeNormalizeSize(AdminInternalTestController controller, Integer size) throws Exception {
        Method method = AdminInternalTestController.class.getDeclaredMethod("normalizeSize", Integer.class);
        method.setAccessible(true);
        return (Integer) method.invoke(controller, size);
    }

    private void assertParseResponseFail(AdminInternalTestController controller, String json, String expectedMessage) throws Exception {
        assertParseResponseFail(controller, json, ResponseCode.FAIL_SER.getCode(), expectedMessage);
    }

    private void assertParseResponseFail(AdminInternalTestController controller, String json, int expectedCode, String expectedMessage) throws Exception {
        try {
            invokeParseResponseRes(controller, json);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof SysException);
            SysException sysException = (SysException) cause;
            assertEquals(expectedCode, sysException.getCode());
            assertEquals(expectedMessage, sysException.getMsg());
            return;
        }
        throw new AssertionError("Expected SysException for json=" + json);
    }
}
