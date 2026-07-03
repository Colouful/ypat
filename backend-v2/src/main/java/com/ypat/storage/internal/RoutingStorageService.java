package com.ypat.storage.internal;

import com.ypat.storage.api.MediaMetadata;
import com.ypat.storage.api.MediaMetadata.Provider;
import com.ypat.storage.api.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * Routes storage calls to the configured provider for a given
 * object. Holds the {@code Provider -> StorageService} map and a
 * fallback chain used by {@link #read} when the primary returns
 * 404 — historical FastDFS rows must remain readable until the
 * backfill job (PR-11 follow-up) catches up.
 *
 * Why a separate class instead of stuffing the routing into
 * {@code CosStorageAdapter}:
 *   - Adding the FastDFS adapter (PR-11) becomes a pure addition,
 *     no edits to the COS code.
 *   - Test can swap one adapter at a time.
 *   - The fallback chain is readable in one place.
 */
@Service
public class RoutingStorageService implements StorageService {

    /** Map of provider -> concrete adapter. */
    private final Map<Provider, StorageService> adapters;
    /** Provider used for new writes. Configurable per profile. */
    private final Provider writeProvider;
    /**
     * Read fallback order. The first provider that has the object
     * wins; 404 / 5xx move on to the next.
     */
    private final Provider[] readFallbackChain;

    public RoutingStorageService(
            CosStorageAdapter cos,
            @Value("${ypat.storage.write-provider:COS}") String writeProviderName,
            @Value("${ypat.storage.read-fallback:COS,FASTDFS}") String[] readFallback) {

        this.adapters = new EnumMap<>(Provider.class);
        this.adapters.put(Provider.COS, cos);
        // FASTDFS adapter lands with PR-11 (work-read migration).
        // Until then the chain silently treats FASTDFS as "no fallback".
        this.writeProvider = Provider.valueOf(writeProviderName);
        this.readFallbackChain = new Provider[readFallback.length];
        for (int i = 0; i < readFallback.length; i++) {
            this.readFallbackChain[i] = Provider.valueOf(readFallback[i]);
        }
    }

    @Override
    public String upload(InputStream in, MediaMetadata meta) throws IOException {
        StorageService target = adapters.get(meta.provider());
        if (target == null) {
            throw new IOException(
                    "No adapter registered for provider " + meta.provider()
                            + ". Did you forget to add it to RoutingStorageService.adapters?");
        }
        return target.upload(in, meta);
    }

    @Override
    public InputStream read(String objectKey) throws IOException {
        IOException last = null;
        for (Provider p : readFallbackChain) {
            StorageService adapter = adapters.get(p);
            if (adapter == null) continue;        // FASTDFS not registered yet
            try {
                return adapter.read(objectKey);
            } catch (IOException notFound) {
                last = notFound;
                // try next provider in the chain
            }
        }
        throw last != null ? last
                           : new IOException("No adapter available for object: " + objectKey);
    }

    @Override
    public void delete(String objectKey) throws IOException {
        StorageService target = adapters.get(writeProvider);
        if (target == null) {
            throw new IOException("Write provider " + writeProvider + " has no adapter");
        }
        target.delete(objectKey);
    }

    /** For tests / observability: which provider owns new writes. */
    public Provider writeProvider() {
        return writeProvider;
    }
}