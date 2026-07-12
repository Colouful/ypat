package com.ypat.controller;

import com.google.gson.JsonElement;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminUserAuditSourceTest {

    @Test
    public void adminUserAuditParsesBlankDownstreamResponseAsEmptyObject() throws Exception {
        AdminUserController controller = new AdminUserController();

        assertTrue(invokeParseAuditResponse(controller, null).isJsonObject());
        assertTrue(invokeParseAuditResponse(controller, "").isJsonObject());
        assertTrue(invokeParseAuditResponse(controller, "   ").isJsonObject());
        assertTrue(invokeParseAuditResponse(controller, "{\"ok\":true}")
                .getAsJsonObject()
                .get("ok")
                .getAsBoolean());
    }

    @Test
    public void adminUserAuditUsesDedicatedParserAndBlankCheck() throws IOException {
        String source = readSource(
                "src/main/java/com/ypat/controller/AdminUserController.java",
                "backend/system-wap/src/main/java/com/ypat/controller/AdminUserController.java",
                "AdminUserController.java should exist");

        assertTrue(source.contains("private JsonElement parseAuditResponse(String result)"));
        assertTrue(source.contains("StringUtils.isBlank(result)"));
        assertTrue(source.contains("return JsonParser.parseString(\"{}\")"));
        assertTrue(source.contains("JsonElement resData = parseAuditResponse(result);"));
        assertFalse(source.contains("JsonElement resData = JsonParser.parseString(result);"));
    }

    private JsonElement invokeParseAuditResponse(AdminUserController controller, String result) throws Exception {
        Method method = AdminUserController.class.getDeclaredMethod("parseAuditResponse", String.class);
        method.setAccessible(true);
        return (JsonElement) method.invoke(controller, result);
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
