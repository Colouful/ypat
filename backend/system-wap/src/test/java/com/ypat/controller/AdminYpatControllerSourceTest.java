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

public class AdminYpatControllerSourceTest {

    @Test
    public void adminYpatListPropagatesDownstreamBusinessErrors() throws IOException {
        String source = readSource(
                "src/main/java/com/ypat/controller/AdminYpatController.java",
                "backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java",
                "AdminYpatController.java should exist");

        assertTrue(source.contains("private JsonElement parseResponseRes(String json)"));
        assertTrue(source.contains("object.get(\"code\")"));
        assertTrue(source.contains("code != ResponseCode.SUCCESS.getCode()"));
        assertTrue(source.contains("throw new SysException(code, msg)"));
        assertTrue(source.contains("JsonElement codeElement = object.get(\"code\")"));
        assertTrue(source.contains("codeElement.isJsonNull()"));
        assertTrue(source.contains("!codeElement.isJsonPrimitive()"));
        assertTrue(source.contains("!codeElement.getAsJsonPrimitive().isNumber()"));
        assertTrue(source.contains("new SysException(ResponseCode.FAIL_SER, \"服务响应格式错误\")"));
        assertTrue(source.contains("JsonElement msgElement = object.get(\"msg\")"));
        assertTrue(source.contains("msgElement != null"));
        assertTrue(source.contains("msgElement.isJsonNull()"));
        assertTrue(source.contains("msgElement.isJsonPrimitive()"));
        assertTrue(source.contains("msgElement.getAsJsonPrimitive().isString()"));
        assertTrue(source.contains("ResponseCode.FAIL_SER.getMsg()"));
        assertFalse(source.contains("object.get(\"msg\").getAsString()"));
        assertTrue(source.contains("return ResponseApiBody.success(parseResponseRes(json))"));
        assertFalse(source.contains("JsonElement pageData = JsonParser.parseString(json)"));
        assertFalse(source.contains("ResponseApiBody.success(pageData)"));
    }

    @Test
    public void adminYpatListAcceptsAndForwardsPublishFilters() throws IOException {
        String source = readSource(
                "src/main/java/com/ypat/controller/AdminYpatController.java",
                "backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java",
                "AdminYpatController.java should exist");

        assertTrue(source.contains("private static final int MAX_SIZE = 50"));
        assertTrue(source.contains("qo.setSize(normalizeSize(size))"));
        assertTrue(source.contains("return Math.min(size, MAX_SIZE)"));
        assertTrue(source.contains("@RequestParam(value = \"target\", required = false) String target"));
        assertTrue(source.contains("@RequestParam(value = \"patstyle\", required = false) String patstyle"));
        assertTrue(source.contains("@RequestParam(value = \"chargeway\", required = false) String chargeway"));
        assertTrue(source.contains("@RequestParam(value = \"city\", required = false) String city"));
        assertTrue(source.contains("@RequestParam(value = \"workId\", required = false) String workId"));
        assertTrue(source.contains("qo.setTarget(target)"));
        assertTrue(source.contains("qo.setPatstyle(patstyle)"));
        assertTrue(source.contains("qo.setChargeway(chargeway)"));
        assertTrue(source.contains("qo.setCity(city)"));
        assertTrue(source.contains("qo.setWorkId(workId)"));
    }

    @Test
    public void adminYpatAuditTreatsVoidDownstreamResponseAsSuccess() throws IOException {
        String source = readSource(
                "src/main/java/com/ypat/controller/AdminYpatController.java",
                "backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java",
                "AdminYpatController.java should exist");
        String auditMethod = methodBody(source,
                "public ResponseApiBody audit(",
                "    /**\n     * 上推荐 / 取消推荐。");

        assertTrue(auditMethod.contains("ypatServiceClient.audit(id, flag, null, reason)"));
        assertTrue(auditMethod.contains("return ResponseApiBody.success(\"审核完成\")"));
        assertFalse(auditMethod.contains("JsonParser.parseString(res)"));
        assertFalse(auditMethod.contains("result.put(\"success\""));
    }

    @Test
    public void adminYpatRecomTreatsVoidDownstreamResponseAsSuccess() throws IOException {
        String source = readSource(
                "src/main/java/com/ypat/controller/AdminYpatController.java",
                "backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java",
                "AdminYpatController.java should exist");
        String recomMethod = methodBody(source,
                "public ResponseApiBody recom(",
                "    /**\n     * 发布作品（后台代提交）。");

        assertTrue(recomMethod.contains("ypatServiceClient.upRecom(id, recomflag)"));
        assertTrue(recomMethod.contains("return ResponseApiBody.success(\"推荐状态已更新\")"));
        assertFalse(recomMethod.contains("JsonParser.parseString(res)"));
    }

    @Test
    public void adminYpatSubmitUsesStorageServiceForAvatarAndWatermarkedWorks() throws IOException {
        String source = readSource(
                "src/main/java/com/ypat/controller/AdminYpatController.java",
                "backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java",
                "AdminYpatController.java should exist");
        String submitMethod = methodBody(source,
                "public ResponseApiBody submit(",
                "    private void validateSubmitFields");

        assertTrue(source.contains("private StorageService storageService"));
        assertTrue(source.contains("storageService.upload(file.getInputStream(), ImageConst.IMAGE_TYPE, file.getContentType(), StorageBizPath.AVATAR)"));
        assertTrue(source.contains("imageMarkUtil.waterMake(inputStream)"));
        assertTrue(source.contains("storageService.upload(waterStream, ImageConst.IMAGE_TYPE, \"image/jpeg\", StorageBizPath.YPAT)"));
        assertTrue(submitMethod.contains("ypatServiceClient.submit(ypatInfoQo)"));
        assertTrue(submitMethod.contains("return ResponseApiBody.success(null)"));
        assertFalse(submitMethod.contains("JsonParser.parseString(res)"));
        assertFalse(source.contains("fastDFSClient.uploanFile1"));
        assertFalse(source.contains("systemConfig.getFdfs_path() + fileId"));
    }

    @Test
    public void parseResponseResReturnsResAndWrapsMalformedResponses() throws Exception {
        AdminYpatController controller = new AdminYpatController();

        JsonElement data = invokeParseResponseRes(controller, "{\"code\":200,\"res\":{\"ok\":true}}");
        assertTrue(data.isJsonObject());
        assertTrue(data.getAsJsonObject().get("ok").getAsBoolean());

        JsonElement rawPage = invokeParseResponseRes(controller, "{\"content\":[],\"totalElements\":0}");
        assertTrue(rawPage.isJsonObject());
        assertTrue(rawPage.getAsJsonObject().has("content"));

        assertParseResponseFail(controller, "{\"code\":500,\"msg\":{}}", ResponseCode.FAIL_SER.getMsg());
        assertParseResponseFail(controller, "{\"code\":500,\"msg\":[]}", ResponseCode.FAIL_SER.getMsg());
        assertParseResponseFail(controller, "{\"code\":500,\"msg\":null}", ResponseCode.FAIL_SER.getMsg());
        assertParseResponseFail(controller, "{\"code\":500,\"msg\":123}", ResponseCode.FAIL_SER.getMsg());
        assertParseResponseFail(controller, "{\"code\":500,\"msg\":true}", ResponseCode.FAIL_SER.getMsg());
        assertParseResponseFail(controller, "{\"code\":1002,\"msg\":\"参数错误\"}", 1002, "参数错误");
        assertParseResponseFail(controller, "{\"code\":null,\"msg\":\"bad\"}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":\"x\",\"msg\":\"bad\"}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200,\"msg\":\"ok\"}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200,\"res\":null}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200,\"res\":[]}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200,\"res\":123}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200,\"res\":true}", "服务响应格式错误");
        assertParseResponseFail(controller, "{\"code\":200,\"res\":\"x\"}", "服务响应格式错误");
        assertParseResponseFail(controller, "[]", "服务响应格式错误");
        assertParseResponseFail(controller, "123", "服务响应格式错误");
        assertParseResponseFail(controller, "null", "服务响应格式错误");
        assertParseResponseFail(controller, "{not-json", "服务响应格式错误");
        assertParseResponseFail(controller, "", "服务响应格式错误");
    }

    @Test
    public void normalizePageUsesDefaultForMissingAndNegativeRequests() throws Exception {
        AdminYpatController controller = new AdminYpatController();

        assertEquals(0, invokeNormalizePage(controller, null));
        assertEquals(0, invokeNormalizePage(controller, -1));
        assertEquals(0, invokeNormalizePage(controller, 0));
        assertEquals(3, invokeNormalizePage(controller, 3));
    }

    @Test
    public void normalizeSizeUsesDefaultsAndCapsLargeRequests() throws Exception {
        AdminYpatController controller = new AdminYpatController();

        assertEquals(10, invokeNormalizeSize(controller, null));
        assertEquals(10, invokeNormalizeSize(controller, 0));
        assertEquals(10, invokeNormalizeSize(controller, 10));
        assertEquals(50, invokeNormalizeSize(controller, 999));
    }

    private String readSource(String modulePath, String repoPath, String message) throws IOException {
        Path path = Paths.get(modulePath);
        if (!Files.exists(path)) {
            path = Paths.get(repoPath);
        }
        assertTrue(message, Files.exists(path));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private JsonElement invokeParseResponseRes(AdminYpatController controller, String json) throws Exception {
        Method method = AdminYpatController.class.getDeclaredMethod("parseResponseRes", String.class);
        method.setAccessible(true);
        return (JsonElement) method.invoke(controller, json);
    }

    private int invokeNormalizePage(AdminYpatController controller, Integer page) throws Exception {
        Method method = AdminYpatController.class.getDeclaredMethod("normalizePage", Integer.class);
        method.setAccessible(true);
        return (Integer) method.invoke(controller, new Object[]{page});
    }

    private int invokeNormalizeSize(AdminYpatController controller, Integer size) throws Exception {
        Method method = AdminYpatController.class.getDeclaredMethod("normalizeSize", Integer.class);
        method.setAccessible(true);
        return (Integer) method.invoke(controller, new Object[]{size});
    }

    private String methodBody(String source, String startToken, String endToken) {
        int start = source.indexOf(startToken);
        assertTrue(start >= 0);
        int end = source.indexOf(endToken, start);
        assertTrue(end > start);
        return source.substring(start, end);
    }

    private void assertParseResponseFail(AdminYpatController controller, String json, String expectedMessage) throws Exception {
        assertParseResponseFail(controller, json, ResponseCode.FAIL_SER.getCode(), expectedMessage);
    }

    private void assertParseResponseFail(AdminYpatController controller, String json, int expectedCode, String expectedMessage) throws Exception {
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
