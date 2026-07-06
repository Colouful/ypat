package com.ypat.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "system.third.storage")
public class CosStorageProperties {
    private String provider = "fastdfs";
    private String secretId;
    private String secretKey;
    private String region;
    private String bucket;
    private String publicBaseUrl;
    private String envPrefix = "dev";

    public String normalizedProvider() {
        return isBlank(provider) ? "fastdfs" : provider.trim().toLowerCase();
    }

    public boolean isCosEnabled() {
        return "cos".equals(normalizedProvider());
    }

    public void validate() {
        if (!isCosEnabled()) return;
        require("YPAT_COS_SECRET_ID", secretId);
        require("YPAT_COS_SECRET_KEY", secretKey);
        require("YPAT_COS_REGION", region);
        require("YPAT_COS_BUCKET", bucket);
        require("YPAT_COS_PUBLIC_BASE_URL", publicBaseUrl);
        require("YPAT_COS_ENV_PREFIX", envPrefix);
    }

    private void require(String name, String value) {
        if (isBlank(value)) {
            throw new IllegalStateException("COS storage requires " + name);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public String getEnvPrefix() {
        return envPrefix;
    }

    public void setEnvPrefix(String envPrefix) {
        this.envPrefix = envPrefix;
    }
}
