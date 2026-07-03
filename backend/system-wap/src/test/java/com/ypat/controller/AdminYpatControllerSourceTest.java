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
        assertTrue(source.contains("return ResponseApiBody.success(parseResponseRes(json))"));
        assertFalse(source.contains("JsonElement pageData = JsonParser.parseString(json)"));
        assertFalse(source.contains("ResponseApiBody.success(pageData)"));
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
