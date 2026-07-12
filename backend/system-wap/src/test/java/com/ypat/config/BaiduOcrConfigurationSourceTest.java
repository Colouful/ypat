package com.ypat.config;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class BaiduOcrConfigurationSourceTest {

    @Test
    public void wapComposeServicesPassBaiduIdCardCredentials() throws Exception {
        assertWapComposeExposesCredentials("docker-compose.yml");
        assertWapComposeExposesCredentials("docker-compose.staging.yml");
        assertWapComposeExposesCredentials("docker-compose.production.yml");
    }

    @Test
    public void rootEnvExampleDeclaresBaiduIdCardCredentials() throws Exception {
        String source = read(".env.example");

        assertTrue(source.contains("YPAT_BD_IDCARD_AK="));
        assertTrue(source.contains("YPAT_BD_IDCARD_SK="));
    }

    private void assertWapComposeExposesCredentials(String file) throws Exception {
        String wapService = serviceBlock(read(file), "wap");

        assertTrue(file + " must expose YPAT_BD_IDCARD_AK to wap",
                wapService.contains("YPAT_BD_IDCARD_AK: \"${YPAT_BD_IDCARD_AK:-}\""));
        assertTrue(file + " must expose YPAT_BD_IDCARD_SK to wap",
                wapService.contains("YPAT_BD_IDCARD_SK: \"${YPAT_BD_IDCARD_SK:-}\""));
    }

    private String read(String file) throws Exception {
        Path path = firstExistingPath(
                Paths.get("../../" + file),
                Paths.get("../" + file),
                Paths.get(file)
        );
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private Path firstExistingPath(Path... candidates) {
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return candidates[0];
    }

    private String serviceBlock(String source, String serviceName) {
        String marker = "\n  " + serviceName + ":\n";
        int start = source.indexOf(marker);
        assertTrue("missing service " + serviceName, start >= 0);
        int next = source.length();
        for (String line : source.substring(start + marker.length()).split("\n")) {
            if (line.startsWith("  ") && !line.startsWith("    ") && line.endsWith(":")) {
                next = source.indexOf("\n" + line, start + marker.length());
                break;
            }
        }
        return source.substring(start, next);
    }
}
