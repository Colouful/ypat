package com.ypat.enums;

public enum InternalTestResourceMediaType {
    image("image", "图片"),
    video("video", "视频");

    public String value;
    public String name;

    InternalTestResourceMediaType(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        if (code == null) return "";
        for (InternalTestResourceMediaType target : InternalTestResourceMediaType.values()) {
            if (target.value.equals(code)) {
                return target.name;
            }
        }
        return "";
    }

    public static boolean isValid(String code) {
        if (code == null) return false;
        for (InternalTestResourceMediaType target : InternalTestResourceMediaType.values()) {
            if (target.value.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
