package com.ypat.storage.api;

import java.io.IOException;
import java.io.InputStream;

/**
 * Storage abstraction for backend-v2.
 *
 * Replaces the legacy FastDFS-only path with a provider-agnostic
 * interface. PR-10 ships a COS implementation; PR-11 ships the
 * FastDFS read-fallback adapter.
 *
 * Contract:
 *   - {@link #upload} returns a stable {@code objectKey} that can
 *     be passed back to {@link #read} or {@link #delete} later.
 *     The key MUST NOT include any host / bucket prefix; URL
 *     assembly is the {@code StorageUrlResolver}'s job (PR-11).
 *   - {@link #read} returns the raw bytes. Callers wrap it in
 *     their own stream handling; we do not cache.
 *   - {@link #delete} is idempotent. Calling it on a missing key
 *     is a no-op, not an error.
 *
 * Not in this interface:
 *   - Multi-part / chunked upload. That's a provider-specific
 *     concern (COS has it, FastDFS does not). When work media
 *     upload needs it (PR-11), the routing service dispatches to
 *     a typed method on the concrete adapter instead.
 *   - Signed URL generation. Belongs to StorageUrlResolver, not
 *     the storage layer.
 */
public interface StorageService {

    /**
     * @return objectKey for the uploaded object. Never null.
     * @throws IOException on transport / network failure or if the
     *     provider rejects the upload (size limit, content-type
     *     mismatch, etc).
     */
    String upload(InputStream in, MediaMetadata meta) throws IOException;

    /**
     * Open a stream for reading. Caller must close.
     * @throws IOException if the object does not exist or the
     *     provider returns 4xx / 5xx. Implementations SHOULD
     *     throw {@link java.io.FileNotFoundException} on 404 so
     *     the routing service can fall back to a different
     *     provider.
     */
    InputStream read(String objectKey) throws IOException;

    /** Idempotent delete. Missing keys are silently ignored. */
    void delete(String objectKey) throws IOException;
}