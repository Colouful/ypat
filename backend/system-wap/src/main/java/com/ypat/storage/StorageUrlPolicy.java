package com.ypat.storage;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.config.SystemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StorageUrlPolicy {
    @Autowired
    private StorageService storageService;

    @Autowired
    private SystemConfig systemConfig;

    public boolean supports(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        return storageService.supportsUrl(url)
                || FastDfsStorageService.extractFastDfsFileId(systemConfig.getFdfs_path(), url) != null;
    }

    public String requireSupported(String url) {
        if (supports(url)) {
            return url.trim();
        }
        throw new SysException(ResponseCode.FAIL_PARA, "图片地址必须来自平台存储");
    }
}
