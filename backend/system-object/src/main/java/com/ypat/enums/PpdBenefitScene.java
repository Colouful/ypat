package com.ypat.enums;

public enum PpdBenefitScene {
    SUBMIT_YPAT("发布约拍"),
    APPLY_YPAT("发起约拍申请"),
    VIEW_CONTACT("查看联系方式");

    private final String label;

    PpdBenefitScene(String label) {
        this.label = label;
    }

    public String getCode() {
        return name();
    }

    public String getLabel() {
        return label;
    }

    public static PpdBenefitScene fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PpdBenefitScene item : values()) {
            if (item.name().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
