package com.ypat.storage;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

public class CosStorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(CosStorageService.class);

    private final CosStorageProperties properties;
    private final CosObjectKeyFactory keyFactory;

    public CosStorageService(CosStorageProperties properties) {
        this.properties = properties;
        this.properties.validate();
        this.keyFactory = new CosObjectKeyFactory(properties.getEnvPrefix());
    }

    @Override
    public StoredObject upload(InputStream inputStream, String originalFilename, String contentType, StorageBizPath bizPath) throws IOException {
        String key = keyFactory.createKey(bizPath, originalFilename, new Date(), UUID.randomUUID().toString().replace("-", ""));
        ObjectMetadata metadata = new ObjectMetadata();
        if (contentType != null && !contentType.trim().isEmpty()) {
            metadata.setContentType(contentType);
        }
        COSClient client = createClient();
        try {
            PutObjectRequest request = new PutObjectRequest(properties.getBucket(), key, inputStream, metadata);
            client.putObject(request);
            return new StoredObject(key, publicUrl(key));
        } catch (RuntimeException e) {
            logger.error("COS upload failed provider=cos bizPath={} type={} error={}", bizPath.path(), contentType, e.getClass().getSimpleName());
            throw new SysException(ResponseCode.FAIL_UPLOAD, "Storage upload failed");
        } finally {
            client.shutdown();
        }
    }

    @Override
    public boolean supportsUrl(String url) {
        return keyFactory.supportsUrl(properties.getPublicBaseUrl(), url);
    }

    @Override
    public String extractObjectKey(String url) {
        return keyFactory.extractObjectKey(properties.getPublicBaseUrl(), url);
    }

    @Override
    public void deleteByUrl(String url) {
        String key = extractObjectKey(url);
        if (key == null) return;
        COSClient client = createClient();
        try {
            client.deleteObject(properties.getBucket(), key);
        } catch (RuntimeException e) {
            logger.warn("COS delete failed key={} err={}", key, e.getClass().getSimpleName());
        } finally {
            client.shutdown();
        }
    }

    private COSClient createClient() {
        COSCredentials credentials = new BasicCOSCredentials(properties.getSecretId(), properties.getSecretKey());
        ClientConfig config = new ClientConfig(new Region(properties.getRegion()));
        return new COSClient(credentials, config);
    }

    private String publicUrl(String key) {
        String base = properties.getPublicBaseUrl().trim();
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/" + key;
    }
}
