package com.ypat.storage;

import com.ypat.SysException;
import com.ypat.config.SystemConfig;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StorageUrlPolicyTest {

    @Test
    public void allowsActiveStorageUrls() {
        StorageUrlPolicy policy = policyWith(new FakeStorageService(true), "https://fastdfs.panghu.work/");

        assertTrue(policy.supports("https://cdn.example.test/pro/avatar/a.png"));
        assertEquals("https://cdn.example.test/pro/avatar/a.png", policy.requireSupported(" https://cdn.example.test/pro/avatar/a.png "));
    }

    @Test
    public void allowsConfiguredFastDfsUrlsForCompatibility() {
        StorageUrlPolicy policy = policyWith(new FakeStorageService(false), "https://fastdfs.panghu.work/");

        assertTrue(policy.supports("https://fastdfs.panghu.work/group1/M00/00/00/avatar.jpg"));
    }

    @Test
    public void rejectsExternalUrls() {
        StorageUrlPolicy policy = policyWith(new FakeStorageService(false), "https://fastdfs.panghu.work/");

        assertFalse(policy.supports("https://example.com/avatar.jpg"));
    }

    @Test(expected = SysException.class)
    public void requireSupportedRejectsExternalUrls() {
        StorageUrlPolicy policy = policyWith(new FakeStorageService(false), "https://fastdfs.panghu.work/");

        policy.requireSupported("https://example.com/avatar.jpg");
    }

    private StorageUrlPolicy policyWith(StorageService storageService, String fastDfsBaseUrl) {
        StorageUrlPolicy policy = new StorageUrlPolicy();
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setFdfs_path(fastDfsBaseUrl);
        ReflectionTestUtils.setField(policy, "storageService", storageService);
        ReflectionTestUtils.setField(policy, "systemConfig", systemConfig);
        return policy;
    }

    private static class FakeStorageService implements StorageService {
        private final boolean supportsUrl;

        private FakeStorageService(boolean supportsUrl) {
            this.supportsUrl = supportsUrl;
        }

        @Override
        public StoredObject upload(InputStream inputStream, String originalFilename, String contentType, StorageBizPath bizPath) {
            return null;
        }

        @Override
        public boolean supportsUrl(String url) {
            return supportsUrl;
        }

        @Override
        public String extractObjectKey(String url) {
            return null;
        }

        @Override
        public void deleteByUrl(String url) {
        }
    }
}
