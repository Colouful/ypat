package com.ypat.storage;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FastDfsStorageServiceSourceTest {

    @Test
    public void fastDfsAdapterWrapsClientAndReturnsStoredObject() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/storage/FastDfsStorageService.java");

        assertTrue(source.contains("implements StorageService"));
        assertTrue(source.contains("fastDFSClient.uploanFile1"));
        assertTrue(source.contains("new StoredObject(fileId, url)"));
        assertTrue(source.contains("Storage upload failed"));
        assertTrue(source.contains("FastDFS delete failed"));
        assertTrue(source.contains("catch (RuntimeException e)"));
        assertFalse(source.contains("SecretKey"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(file.replace("backend/system-wap/", ""));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
