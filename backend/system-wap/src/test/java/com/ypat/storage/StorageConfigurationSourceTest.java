package com.ypat.storage;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        assertTrue(source.contains("system.third.storage.provider = ${YPAT_STORAGE_PROVIDER:fastdfs}"));
        assertTrue(source.contains("system.third.storage.secret_id = ${YPAT_COS_SECRET_ID:}"));
        assertTrue(source.contains("system.third.storage.secret_key = ${YPAT_COS_SECRET_KEY:}"));
        assertTrue(source.contains("system.third.storage.region = ${YPAT_COS_REGION:}"));
        assertTrue(source.contains("system.third.storage.bucket = ${YPAT_COS_BUCKET:}"));
        assertTrue(source.contains("system.third.storage.public_base_url = ${YPAT_COS_PUBLIC_BASE_URL:}"));
        assertTrue(source.contains("system.third.storage.env_prefix = ${YPAT_COS_ENV_PREFIX:dev}"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(file.replace("backend/system-wap/", ""));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
