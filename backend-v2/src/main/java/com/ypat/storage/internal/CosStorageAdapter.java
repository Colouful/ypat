package com.ypat.storage.internal;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.ypat.storage.api.MediaMetadata;
import com.ypat.storage.api.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * COS adapter — the primary write target for new uploads.
 *
 * PR-10 ships the wiring only; the bucket name and credentials
 * are pulled from {@code application.yml}. PR-11 brings the
 * actual call sites in the work-upload code, and the migration
 * script that backfills existing FastDFS rows.
 *
 * Health-check note:
 *   {@code Region} is fixed at construction. If the configured
 *   region is wrong (typo in the env var), the first upload
 *   fails with "UnknownHostException" / 403. We don't add a
 *   startup probe here because the COS SDK's client init is
 *   lazy by default — the cost of misconfiguration surfaces at
 *   first request, which is loud enough.
 */
@Component
public class CosStorageAdapter implements StorageService {

    private final COSClient client;
    private final String bucket;

    public CosStorageAdapter(
            @Value("${ypat.storage.cos.secret-id}") String secretId,
            @Value("${ypat.storage.cos.secret-key}") String secretKey,
            @Value("${ypat.storage.cos.region}") String region,
            @Value("${ypat.storage.cos.bucket}") String bucket) {

        COSCredentials creds = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig config = new ClientConfig(new Region(region));
        this.client = new COSClient(creds, config);
        this.bucket = bucket;
    }

    @Override
    public String upload(InputStream in, MediaMetadata meta) throws IOException {
        ObjectMetadata cosMeta = new ObjectMetadata();
        cosMeta.setContentLength(meta.sizeBytes());
        if (meta.contentType() != null) {
            cosMeta.setContentType(meta.contentType());
        }
        PutObjectRequest req = new PutObjectRequest(bucket, meta.objectKey(), in, cosMeta);
        try {
            client.putObject(req);
        } catch (CosServiceException e) {
            throw new IOException("COS putObject failed: " + e.getErrorMessage(), e);
        }
        return meta.objectKey();
    }

    @Override
    public InputStream read(String objectKey) throws IOException {
        try {
            GetObjectRequest req = new GetObjectRequest(bucket, objectKey);
            COSObject obj = client.getObject(req);
            return obj.getObjectContent();
        } catch (CosServiceException e) {
            if (e.getStatusCode() == 404) {
                throw new FileNotFoundException("COS 404: " + objectKey);
            }
            throw new IOException("COS getObject failed: " + e.getErrorMessage(), e);
        }
    }

    @Override
    public void delete(String objectKey) throws IOException {
        try {
            client.deleteObject(bucket, objectKey);
        } catch (CosServiceException e) {
            if (e.getStatusCode() == 404) {
                return;     // idempotent — missing key is fine
            }
            throw new IOException("COS deleteObject failed: " + e.getErrorMessage(), e);
        }
    }
}