package com.ypat.service;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class YpatInfoAdminFilterSourceTest {
    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    @Test
    public void ypatAdminFiltersIncludeNewPublishFields() throws Exception {
        String source = read("src/main/java/com/ypat/service/YpatInfoService.java");

        assertTrue(source.contains("queryQo.getTarget()"));
        assertTrue(source.contains("root.get(\"target\")"));
        assertTrue(source.contains("queryQo.getPatstyle()"));
        assertTrue(source.contains("root.get(\"patstyle\")"));
        assertTrue(source.contains("queryQo.getChargeway()"));
        assertTrue(source.contains("root.get(\"chargeway\")"));
        assertTrue(source.contains("queryQo.getWorkId()"));
        assertTrue(source.contains("root.get(\"workId\")"));
    }
}
