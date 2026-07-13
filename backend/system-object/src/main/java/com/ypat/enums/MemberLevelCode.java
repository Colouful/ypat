package com.ypat.enums;

public enum MemberLevelCode {
    BASIC("基础会员"),
    PLUS("高级会员"),
    PRO("专业会员");

    private final String label;

    MemberLevelCode(String label) {
        this.label = label;
    }

    public String getCode() {
        return name();
    }

    public String getLabel() {
        return label;
    }

    public static MemberLevelCode fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (MemberLevelCode item : values()) {
            if (item.name().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
