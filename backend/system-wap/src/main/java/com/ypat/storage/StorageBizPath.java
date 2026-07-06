package com.ypat.storage;

public enum StorageBizPath {
    ADMIN("admin"),
    YPAT("ypat"),
    WORK("work"),
    AVATAR("avatar"),
    REALNAME("realname");

    private final String path;

    StorageBizPath(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
