package com.ypat.storage;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {
    StoredObject upload(InputStream inputStream, String originalFilename, String contentType, StorageBizPath bizPath) throws IOException;

    boolean supportsUrl(String url);

    String extractObjectKey(String url);

    void deleteByUrl(String url);
}
