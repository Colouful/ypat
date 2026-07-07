package com.ypat.enums;

public enum InternalTestDataFlag {
    real("real", "真实数据"),
    internalTest("internal_test", "内测数据");

    public String value;
    public String name;

    InternalTestDataFlag(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        if (code == null) return "";
        for (InternalTestDataFlag target : InternalTestDataFlag.values()) {
            if (target.value.equals(code)) {
                return target.name;
            }
        }
        return "";
    }

    public static boolean isValid(String code) {
        if (code == null) return false;
        for (InternalTestDataFlag target : InternalTestDataFlag.values()) {
            if (target.value.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
