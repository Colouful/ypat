package com.ypat.enums;

public enum PaymentBusinessType {
    DEPOSIT("DEPOSIT"),
    MEMBER("MEMBER"),
    PPD("PPD");

    public final String value;

    PaymentBusinessType(String value) {
        this.value = value;
    }
}
