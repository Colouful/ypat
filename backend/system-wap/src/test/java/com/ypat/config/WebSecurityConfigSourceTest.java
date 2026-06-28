package com.ypat.config;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WebSecurityConfigSourceTest {

    @Test
    public void wapSecurityDoesNotPermitManageWildcardOrAllGetRequests() throws IOException {
        String source = readSource();

        assertFalse(source.contains("\"/manage/**\""));
        assertFalse(source.contains("HttpMethod.GET, \"/**\""));
        assertFalse(source.contains("HttpMethod.GET,\"/**\""));
        assertTrue(source.contains("\"/user/login\""));
        assertTrue(source.contains("\"/user/sms/code\""));
        assertTrue(source.contains("\"/ypat/get\""));
    }

    private String readSource() throws IOException {
        Path path = Paths.get("src/main/java/com/ypat/config/WebSecurityConfig.java");
        if (!Files.exists(path)) {
            path = Paths.get("backend/system-wap/src/main/java/com/ypat/config/WebSecurityConfig.java");
        }
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
