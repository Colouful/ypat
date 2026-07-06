package com.ypat.storage;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.config.SystemConfig;
import com.ypat.util.FastDFSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;

public class FastDfsStorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(FastDfsStorageService.class);

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private SystemConfig systemConfig;

    @Override
    public StoredObject upload(InputStream inputStream, String originalFilename, String contentType, StorageBizPath bizPath) throws IOException {
        String fileId = fastDFSClient.uploanFile1(inputStream, originalFilename);
        if (fileId == null) {
            throw new SysException(ResponseCode.FAIL_UPLOAD, "Storage upload failed");
        }
        String url = joinPublicFileUrl(systemConfig.getFdfs_path(), fileId);
        return new StoredObject(fileId, url);
    }

    @Override
    public boolean supportsUrl(String url) {
        return extractObjectKey(url) != null;
    }

    @Override
    public String extractObjectKey(String url) {
        return extractFastDfsFileId(systemConfig.getFdfs_path(), url);
    }

    @Override
    public void deleteByUrl(String url) {
        String fileId = extractObjectKey(url);
        if (fileId == null) return;
        int slash = fileId.indexOf('/');
        if (slash <= 0) return;
        try {
            int result = fastDFSClient.deleteFile(fileId.substring(0, slash), fileId.substring(slash + 1));
            if (result != 0) {
                logger.warn("FastDFS delete failed fileId={} result={}", fileId, result);
            }
        } catch (RuntimeException e) {
            logger.warn("FastDFS delete failed fileId={} err={}", fileId, e.toString());
        }
    }

    public static String joinPublicFileUrl(String publicBaseUrl, String fileId) {
        String base = trimSlashes(publicBaseUrl, false);
        String path = trimSlashes(fileId, true);
        if (base == null || path == null) {
            throw new SysException(ResponseCode.FAIL_UPLOAD, "文件访问地址未配置");
        }
        return base + "/" + path;
    }

    public static String extractFastDfsFileId(String publicBaseUrl, String url) {
        String base = trimSlashes(publicBaseUrl, false);
        if (base == null || url == null || url.trim().isEmpty()) return null;
        String text = url.trim();
        if (!text.startsWith(base + "/")) return null;
        String fileId = trimSlashes(stripQueryAndFragment(text.substring(base.length())), true);
        return fileId != null && fileId.startsWith("group") && fileId.indexOf('/') > 0 ? fileId : null;
    }

    private static String stripQueryAndFragment(String fileId) {
        int query = fileId.indexOf('?');
        int fragment = fileId.indexOf('#');
        int end = fileId.length();
        if (query >= 0) end = Math.min(end, query);
        if (fragment >= 0) end = Math.min(end, fragment);
        return fileId.substring(0, end);
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
}
