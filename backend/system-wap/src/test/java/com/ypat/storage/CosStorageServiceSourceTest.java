package com.ypat.storage;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CosStorageServiceSourceTest {

    @Test
    public void cosStorageUsesSdkWithoutLoggingSecrets() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/storage/CosStorageService.java");

        assertTrue(source.contains("new COSClient"));
        assertTrue(source.contains("new BasicCOSCredentials"));
        assertTrue(source.contains("PutObjectRequest"));
        assertTrue(source.contains("ObjectMetadata"));
        assertTrue(source.contains("properties.getBucket()"));
        assertTrue(source.contains("new StoredObject(key, publicUrl(key))"));
        assertFalse(source.contains("logger.info(properties.getSecretKey()"));
        assertFalse(source.contains("logger.error(properties.getSecretKey()"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(file.replace("backend/system-wap/", ""));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
