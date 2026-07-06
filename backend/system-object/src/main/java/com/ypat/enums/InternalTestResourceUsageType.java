package com.ypat.enums;

public enum InternalTestResourceUsageType {
    avatar("avatar", "头像"),
    ypat("ypat", "约拍"),
    work("work", "作品");

    public String value;
    public String name;

    InternalTestResourceUsageType(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        if (code == null) return "";
        for (InternalTestResourceUsageType target : InternalTestResourceUsageType.values()) {
            if (target.value.equals(code)) {
                return target.name;
            }
        }
        return "";
    }

    public static boolean isValid(String code) {
        if (code == null) return false;
        for (InternalTestResourceUsageType target : InternalTestResourceUsageType.values()) {
            if (target.value.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
