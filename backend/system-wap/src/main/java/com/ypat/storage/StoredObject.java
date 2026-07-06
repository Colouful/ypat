package com.ypat.storage;

public class StoredObject {
    private final String objectKey;
    private final String url;

    public StoredObject(String objectKey, String url) {
        this.objectKey = objectKey;
        this.url = url;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getUrl() {
        return url;
    }
}
