package com.ypat.controller;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        assertTrue(source.contains("msgElement == null"));
        assertTrue(source.contains("msgElement.isJsonNull()"));
        assertTrue(source.contains("!msgElement.isJsonPrimitive()"));
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

    private String readSource(String modulePath, String repoPath, String message) throws IOException {
        Path path = Paths.get(modulePath);
        if (!Files.exists(path)) {
            path = Paths.get(repoPath);
        }
        assertTrue(message, Files.exists(path));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
