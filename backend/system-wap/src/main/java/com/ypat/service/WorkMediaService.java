package com.ypat.service;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.WorkMedia;
import com.ypat.repository.WorkMediaRepository;
import com.ypat.util.FastDFSClient;
import com.ypat.util.ImageMarkUtil;
import com.ypat.util.MimeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 作品媒体上传服务（system-wap 层，直接对接 FastDFS）
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkMediaService {

    private static final Logger logger = LoggerFactory.getLogger(WorkMediaService.class);

    @Autowired private FastDFSClient fastDFSClient;
    @Autowired private ImageMarkUtil imageMarkUtil;
    @Autowired private com.ypat.config.SystemConfig systemConfig;
    @Autowired private WorkMediaRepository workMediaRepository;

    public Map<String, Object> uploadImage(MultipartFile file, Long userId) {
        validateImage(file);
        try {
            byte[] bytes = file.getBytes();
            if (bytes == null || bytes.length == 0) {
                throw new SysException(ResponseCode.FAIL_UPLOAD);
            }
            String mime = MimeDetector.detect(new ByteArrayInputStream(bytes));
            if (!MimeDetector.isImage(mime)) {
                throw new SysException(ResponseCode.FAIL_FILE_TYPE, "图片格式不支持（仅 JPEG/PNG/WebP）");
            }
            String fileId = fastDFSClient.uploanFile1(
                imageMarkUtil.waterMake(new ByteArrayInputStream(bytes)), "jpg");
            if (fileId == null) {
                throw new SysException(ResponseCode.FAIL_UPLOAD);
            }
            String url = joinPublicFileUrl(systemConfig.getFdfs_path(), fileId);

            WorkMedia media = new WorkMedia();
            media.setUserId(userId);
            media.setType("1");
            media.setUrl(url);
            media.setFileSize((long) bytes.length);
            media.setMime(mime);
            media.setSortNo(0);
            media.setUploadStatus("1");
            media.setCreatedAt(new java.util.Date());
            media = workMediaRepository.save(media);

            return buildResult(media, bytes.length, null, null, null);
        } catch (IOException e) {
            logger.error("图片上传失败", e);
            throw new SysException(ResponseCode.FAIL_UPLOAD);
        }
    }

    public Map<String, Object> uploadVideo(MultipartFile file, Long userId) {
        validateVideo(file);
        try {
            byte[] bytes = file.getBytes();
            if (bytes == null || bytes.length == 0) {
                throw new SysException(ResponseCode.FAIL_UPLOAD);
            }
            String mime = MimeDetector.detect(new ByteArrayInputStream(bytes));
            if (!MimeDetector.isVideo(mime)) {
                throw new SysException(ResponseCode.FAIL_FILE_TYPE, "视频格式不支持（仅 MP4/MOV）");
            }
            String fileId = fastDFSClient.uploanFile1(new ByteArrayInputStream(bytes), "mp4");
            if (fileId == null) {
                throw new SysException(ResponseCode.FAIL_UPLOAD);
            }
            String url = joinPublicFileUrl(systemConfig.getFdfs_path(), fileId);

            WorkMedia media = new WorkMedia();
            media.setUserId(userId);
            media.setType("2");
            media.setUrl(url);
            media.setFileSize((long) bytes.length);
            media.setMime(mime);
            media.setSortNo(0);
            media.setUploadStatus("1");
            media.setCreatedAt(new java.util.Date());
            media = workMediaRepository.save(media);

            return buildResult(media, bytes.length, null, null, null);
        } catch (IOException e) {
            logger.error("视频上传失败", e);
            throw new SysException(ResponseCode.FAIL_UPLOAD);
        }
    }

    public void deleteMedia(Long mediaId, Long userId) {
        if (mediaId == null || userId == null) {
            throw new SysException(ResponseCode.FAIL_AUTH);
        }
        WorkMedia media = workMediaRepository.findOne(mediaId);
        if (media == null) {
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        if (!userId.equals(media.getUserId())) {
            throw new SysException(ResponseCode.FAIL_WORK_FORBIDDEN);
        }
        if (media.getWorkId() != null) {
            throw new SysException(ResponseCode.FAIL_VAL, "媒体已绑定作品，不能删除");
        }
        workMediaRepository.delete(media);
        try {
            String url = media.getUrl();
            String fileId = extractFastDfsFileId(systemConfig.getFdfs_path(), url);
            if (fileId != null) {
                int slash = fileId.indexOf('/');
                if (slash > 0) {
                    String group = fileId.substring(0, slash);
                    String path = fileId.substring(slash + 1);
                    fastDFSClient.deleteFile(group, path);
                }
            }
        } catch (RuntimeException e) {
            logger.warn("FastDFS 删除失败 mediaId={} err={}", mediaId, e.toString());
        }
    }

    static String joinPublicFileUrl(String publicBaseUrl, String fileId) {
        String base = trimSlashes(publicBaseUrl, false);
        String path = trimSlashes(fileId, true);
        if (base == null || path == null) {
            throw new SysException(ResponseCode.FAIL_UPLOAD, "文件访问地址未配置");
        }
        return base + "/" + path;
    }

    static String extractFastDfsFileId(String publicBaseUrl, String url) {
        String base = trimSlashes(publicBaseUrl, false);
        if (base == null || url == null || url.trim().isEmpty()) return null;
        String text = url.trim();
        if (!text.startsWith(base)) return null;
        String fileId = trimSlashes(text.substring(base.length()), true);
        return fileId != null && fileId.startsWith("group") && fileId.indexOf('/') > 0 ? fileId : null;
    }

    private static String trimSlashes(String value, boolean leading) {
        if (value == null) return null;
        String text = value.trim();
        if (text.isEmpty()) return null;
        if (leading) {
            while (text.startsWith("/")) text = text.substring(1);
        } else {
            while (text.endsWith("/")) text = text.substring(0, text.length() - 1);
        }
        return text.isEmpty() ? null : text;
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new SysException(ResponseCode.FAIL_PARA, "文件不能为空");
        }
        if (!MimeDetector.isValidFileName(file.getOriginalFilename())) {
            throw new SysException(ResponseCode.FAIL_FILE_TYPE, "文件名不合法");
        }
        if (!MimeDetector.isImageExt(file.getOriginalFilename())) {
            throw new SysException(ResponseCode.FAIL_FILE_TYPE, "图片扩展名必须是 jpg/jpeg/png/webp");
        }
        if (file.getSize() > MimeDetector.MAX_IMAGE_SIZE) {
            throw new SysException(ResponseCode.FAIL_IMG_TOTAL_OVERSIZE);
        }
    }

    private void validateVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new SysException(ResponseCode.FAIL_PARA, "文件不能为空");
        }
        if (!MimeDetector.isValidFileName(file.getOriginalFilename())) {
            throw new SysException(ResponseCode.FAIL_FILE_TYPE, "文件名不合法");
        }
        if (!MimeDetector.isVideoExt(file.getOriginalFilename())) {
            throw new SysException(ResponseCode.FAIL_FILE_TYPE, "视频扩展名必须是 mp4/mov");
        }
        if (file.getSize() > MimeDetector.MAX_VIDEO_SIZE) {
            throw new SysException(ResponseCode.FAIL_VIDEO_OVERSIZE);
        }
    }

    private Map<String, Object> buildResult(WorkMedia m, long size, Integer w, Integer h, Integer dur) {
        Map<String, Object> res = new HashMap<>();
        res.put("id", m.getId());
        res.put("url", m.getUrl());
        res.put("type", "1".equals(m.getType()) ? "IMAGE" : "VIDEO");
        res.put("fileSize", m.getFileSize());
        res.put("width", w);
        res.put("height", h);
        res.put("duration", dur);
        return res;
    }
}
