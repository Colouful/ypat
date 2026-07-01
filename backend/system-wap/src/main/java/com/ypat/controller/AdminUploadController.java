package com.ypat.controller;

import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.comm.ImageConst;
import com.ypat.config.SystemConfig;
import com.ypat.util.FastDFSClient;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private FastDFSClient fastDFSClient;

    @Autowired
    private ImageMarkUtil imageMarkUtil;

    @Autowired
    private SystemConfig systemConfig;

    /**
     * 通用图片上传（无水印）。
     *
     * @param files 文件数组
     * @return 上传后的可访问 URL 列表
     */
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
            String fileId = fastDFSClient.uploanFile1(file.getInputStream(), file.getOriginalFilename());
            if (fileId == null) {
                throw new SysException(ResponseCode.FAIL_MARK.getCode(), "文件上传失败");
            }
            urls.add(systemConfig.getFdfs_path() + fileId);
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
            String fileId = fastDFSClient.uploanFile1(
                    imageMarkUtil.waterMake(file.getInputStream()),
                    ImageConst.IMAGE_TYPE);
            if (fileId == null) {
                throw new SysException(ResponseCode.FAIL_MARK.getCode(), "作品图片上传失败");
            }
            urls.add(systemConfig.getFdfs_path() + fileId);
        }

        Map<String, Object> res = new HashMap<>(2);
        res.put("urls", urls);
        logger.info("管理端约拍作品上传成功：count={}", urls.size());
        return ResponseApiBody.success(res);
    }
}
