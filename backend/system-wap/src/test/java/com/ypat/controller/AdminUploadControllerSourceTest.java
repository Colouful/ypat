package com.ypat.controller;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminUploadControllerSourceTest {

    @Test
    public void adminUploadsUseBackendStorageService() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/controller/AdminUploadController.java");

        assertTrue(source.contains("private StorageService storageService"));
        assertTrue(source.contains("storageService.upload(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), StorageBizPath.ADMIN)"));
        assertTrue(source.contains("storedObject.getUrl()"));
        assertFalse(source.contains("private FastDFSClient fastDFSClient"));
        assertFalse(source.contains("systemConfig.getFdfs_path() + fileId"));
    }

    @Test
    public void ypatUploadsWatermarkBeforeStorageServiceUpload() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/controller/AdminUploadController.java");

        assertTrue(source.contains("imageMarkUtil.waterMake(file.getInputStream())"));
        assertTrue(source.contains("storageService.upload("));
        assertTrue(source.contains("ImageConst.IMAGE_TYPE"));
        assertTrue(source.contains("\"image/jpeg\""));
        assertTrue(source.contains("StorageBizPath.YPAT"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(file.replace("backend/system-wap/", ""));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
