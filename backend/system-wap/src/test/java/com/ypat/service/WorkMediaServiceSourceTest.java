package com.ypat.service;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkMediaServiceSourceTest {

    @Test
    public void workImageUploadsUseStorageServiceAfterWatermarking() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/service/WorkMediaService.java");

        assertTrue(source.contains("private StorageService storageService"));
        assertTrue(source.contains("imageMarkUtil.waterMake(new ByteArrayInputStream(bytes))"));
        assertTrue(source.contains("storageService.upload("));
        assertTrue(source.contains("ImageConst.IMAGE_TYPE"));
        assertTrue(source.contains("\"image/jpeg\""));
        assertTrue(source.contains("StorageBizPath.WORK"));
        assertTrue(source.contains("storedObject.getUrl()"));
        assertFalse(source.contains("fastDFSClient.uploanFile1"));
    }

    @Test
    public void workVideoUploadsUseStorageServiceWithoutWatermarking() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/service/WorkMediaService.java");

        assertTrue(source.contains("storageService.upload(new ByteArrayInputStream(bytes), file.getOriginalFilename(), mime, StorageBizPath.WORK)"));
        assertFalse(source.contains("imageMarkUtil.waterMake(new ByteArrayInputStream(bytes)), \"mp4\""));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(file.replace("backend/system-wap/", ""));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
