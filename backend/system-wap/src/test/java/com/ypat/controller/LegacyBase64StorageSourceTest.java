package com.ypat.controller;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LegacyBase64StorageSourceTest {

    @Test
    public void ypatSubmitBase64ImagesUseStorageServiceWithWatermark() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/controller/YpatInfoController.java");

        assertTrue(source.contains("private StorageService storageService"));
        assertTrue(source.contains("private StorageUrlPolicy storageUrlPolicy"));
        assertTrue(source.contains("storageUrlPolicy.requireSupported(fileBase64)"));
        assertTrue(source.contains("Base64ImagePayload.fromBytes(bytes)"));
        assertTrue(source.contains("imageMarkUtil.waterMake(inputStream)"));
        assertTrue(source.contains("storageService.upload(waterStream, ImageConst.IMAGE_TYPE, \"image/jpeg\", StorageBizPath.YPAT)"));
        assertTrue(source.contains("storedObject.getUrl()"));
        assertFalse(source.contains("fastDFSClient.uploanFile1(waterStream"));
    }

    @Test
    public void realnameImagesUseStorageServiceRealnamePath() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/controller/OauthController.java");

        assertTrue(source.contains("private StorageService storageService"));
        assertTrue(source.contains("private StorageUrlPolicy storageUrlPolicy"));
        assertTrue(source.contains("storageUrlPolicy.requireSupported(fileBase64)"));
        assertTrue(source.contains("Base64ImagePayload.fromBytes(bytes)"));
        assertTrue(source.contains("storageService.upload(image.inputStream(), image.getFilename(), image.getContentType(), StorageBizPath.REALNAME)"));
        assertTrue(source.contains("storedObject.getUrl()"));
        assertTrue(source.contains("storageService.upload(uploadfile.getInputStream(), uploadfile.getOriginalFilename(), uploadfile.getContentType(), StorageBizPath.REALNAME)"));
        assertFalse(source.contains("fastDFSClient.uploanFile1(new ByteArrayInputStream(bytes)"));
        assertFalse(source.contains("fastDFSClient.uploanFile1(uploadfile.getInputStream()"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(file.replace("backend/system-wap/", ""));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
