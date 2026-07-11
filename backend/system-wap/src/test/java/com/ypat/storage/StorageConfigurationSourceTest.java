package com.ypat.storage;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StorageConfigurationSourceTest {

    @Test
    public void storageConfigurationSelectsFastdfsOrCosFromProvider() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/storage/StorageConfiguration.java");

        assertTrue(source.contains("@Configuration"));
        assertTrue(source.contains("@Bean"));
        assertTrue(source.contains("StorageService storageService"));
        assertTrue(source.contains("properties.isCosEnabled()"));
        assertTrue(source.contains("return new CosStorageService(properties)"));
        assertTrue(source.contains("return new FastDfsStorageService()"));
    }

    @Test
    public void sysConfExposesCosEnvironmentVariables() throws Exception {
        String source = read("backend/system-wap/src/main/resources/conf/sys_conf.properties");

        assertTrue(source.contains("system.third.storage.provider = ${YPAT_STORAGE_PROVIDER:cos}"));
        assertTrue(source.contains("system.third.storage.secret_id = ${YPAT_COS_SECRET_ID:}"));
        assertTrue(source.contains("system.third.storage.secret_key = ${YPAT_COS_SECRET_KEY:}"));
        assertTrue(source.contains("system.third.storage.region = ${YPAT_COS_REGION:}"));
        assertTrue(source.contains("system.third.storage.bucket = ${YPAT_COS_BUCKET:}"));
        assertTrue(source.contains("system.third.storage.public_base_url = ${YPAT_COS_PUBLIC_BASE_URL:}"));
        assertTrue(source.contains("system.third.storage.env_prefix = ${YPAT_COS_ENV_PREFIX:dev}"));
    }

    @Test
    public void wapComposeServicesPassCosEnvironmentVariables() throws Exception {
        assertWapComposeExposesCosEnvironment("docker-compose.yml", "dev");
        assertWapComposeExposesCosEnvironment("docker-compose.staging.yml", "pre");
        assertWapComposeExposesCosEnvironment("docker-compose.production.yml", "pro");
    }

    private void assertWapComposeExposesCosEnvironment(String file, String envPrefix) throws Exception {
        String source = read(file);
        String wapService = serviceBlock(source, "wap");
        String restapiService = serviceBlock(source, "restapi");

        assertTrue(wapService.contains("YPAT_STORAGE_PROVIDER: \"${YPAT_STORAGE_PROVIDER:-cos}\""));
        assertTrue(wapService.contains("YPAT_COS_SECRET_ID: \"${YPAT_COS_SECRET_ID:-}\""));
        assertTrue(wapService.contains("YPAT_COS_SECRET_KEY: \"${YPAT_COS_SECRET_KEY:-}\""));
        assertTrue(wapService.contains("YPAT_COS_REGION: \"${YPAT_COS_REGION:-ap-guangzhou}\""));
        assertTrue(wapService.contains("YPAT_COS_BUCKET: \"${YPAT_COS_BUCKET:-}\""));
        assertTrue(wapService.contains("YPAT_COS_PUBLIC_BASE_URL: \"${YPAT_COS_PUBLIC_BASE_URL:-}\""));
        assertTrue(wapService.contains("YPAT_COS_ENV_PREFIX: \"${YPAT_COS_ENV_PREFIX:-" + envPrefix + "}\""));
        assertFalse(restapiService.contains("YPAT_STORAGE_PROVIDER"));
        assertFalse(restapiService.contains("YPAT_COS_SECRET_ID"));
        assertFalse(restapiService.contains("YPAT_COS_SECRET_KEY"));
        assertFalse(restapiService.contains("YPAT_COS_REGION"));
        assertFalse(restapiService.contains("YPAT_COS_BUCKET"));
        assertFalse(restapiService.contains("YPAT_COS_PUBLIC_BASE_URL"));
        assertFalse(restapiService.contains("YPAT_COS_ENV_PREFIX"));
    }

    private String read(String file) throws Exception {
        boolean rootFile = !file.contains("/");
        Path path = firstExistingPath(
                rootFile ? Paths.get("../../" + file) : Paths.get(file),
                rootFile ? Paths.get("../" + file) : Paths.get(file),
                Paths.get(file),
                Paths.get(file.replace("backend/system-wap/", "")),
                Paths.get("../" + file)
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
