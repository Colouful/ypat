package com.ypat.enums;

public enum InternalTestResourceStatus {
    enabled("enabled", "启用"),
    disabled("disabled", "禁用");

    public String value;
    public String name;

    InternalTestResourceStatus(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        if (code == null) return "";
        for (InternalTestResourceStatus target : InternalTestResourceStatus.values()) {
            if (target.value.equals(code)) {
                return target.name;
            }
        }
        return "";
    }

    public static boolean isValid(String code) {
        if (code == null) return false;
        for (InternalTestResourceStatus target : InternalTestResourceStatus.values()) {
            if (target.value.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
