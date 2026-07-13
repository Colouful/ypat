package com.ypat.storage.api;

import java.util.Objects;

/**
 * Public metadata that travels with every upload / read request.
 *
 * Lives in {@code com.ypat.storage.api} because it is part of the
 * storage module's public contract. Other modules must construct
 * these values; they may not poke at internal provider-specific
 * fields directly.
 *
 * Why a record and not Lombok:
 *   - Records are JDK 17 native. No annotation-processor needed.
 *   - Forces explicit accessor naming, which matters when the
 *     field set grows over time.
 */
public final class MediaMetadata {

    public enum Provider {
        /** Tencent Cloud COS — the new primary write target (PR-10). */
        COS,
        /** Legacy FastDFS — kept as read-fallback only (PR-11 brings the adapter). */
        FASTDFS
    }

    private final Provider provider;
    private final String objectKey;
    private final String contentType;
    private final long sizeBytes;

    public MediaMetadata(Provider provider,
                         String objectKey,
                         String contentType,
                         long sizeBytes) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.objectKey = Objects.requireNonNull(objectKey, "objectKey");
        this.contentType = contentType;       // nullable: let the provider sniff
        this.sizeBytes = sizeBytes;
    }

    public Provider provider() { return provider; }
    public String objectKey() { return objectKey; }
    public String contentType() { return contentType; }
    public long sizeBytes() { return sizeBytes; }
}