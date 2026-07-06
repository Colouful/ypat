package com.ypat.controller;

import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.comm.ImageConst;
import com.ypat.storage.StorageBizPath;
import com.ypat.storage.StorageService;
import com.ypat.storage.StoredObject;
import com.ypat.util.ImageMarkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理端 - 通用文件上传 Controller。
 *
 * <p>用于横幅、文章封面等不需要水印的图片上传。</p>
 */
@RestController
@RequestMapping("/admin")
public class AdminUploadController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUploadController.class);

    @Autowired
    private StorageService storageService;

    @Autowired
    private ImageMarkUtil imageMarkUtil;

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "webp"));

    /**
     * 校验图片文件类型、大小和扩展名。
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "仅允许上传图片文件");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "图片大小不能超过10MB");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "文件扩展名不合法");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(ext)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "仅支持 jpg/jpeg/png/gif/webp 格式的图片");
        }
    }
    @PostMapping("/upload")
    public ResponseApiBody upload(@RequestParam("files") MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "请选择要上传的文件");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            validateImageFile(file);
            StoredObject storedObject = storageService.upload(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), StorageBizPath.ADMIN);
            if (storedObject == null || storedObject.getUrl() == null) {
                throw new SysException(ResponseCode.FAIL_MARK.getCode(), "文件上传失败");
            }
            urls.add(storedObject.getUrl());
        }

        Map<String, Object> res = new HashMap<>(2);
        res.put("urls", urls);
        logger.info("管理端文件上传成功：count={}", urls.size());
        return ResponseApiBody.success(res);
    }

    /**
     * 约拍作品图片上传（带水印）。
     *
     * @param files 文件数组
     * @return 上传后的可访问 URL 列表
     */
    @PostMapping("/ypat/upload")
    public ResponseApiBody uploadYpat(@RequestParam("files") MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "请选择要上传的作品图片");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            validateImageFile(file);
            StoredObject storedObject = storageService.upload(
                    imageMarkUtil.waterMake(file.getInputStream()),
                    ImageConst.IMAGE_TYPE,
                    "image/jpeg",
                    StorageBizPath.YPAT);
            if (storedObject == null || storedObject.getUrl() == null) {
                throw new SysException(ResponseCode.FAIL_MARK.getCode(), "作品图片上传失败");
            }
            urls.add(storedObject.getUrl());
        }

        Map<String, Object> res = new HashMap<>(2);
        res.put("urls", urls);
        logger.info("管理端约拍作品上传成功：count={}", urls.size());
        return ResponseApiBody.success(res);
    }
}
