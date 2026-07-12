package com.ypat.enums;

public enum PaymentBusinessType {
    DEPOSIT("DEPOSIT"),
    MEMBER("MEMBER"),
    PPD("PPD"),
    REALNAME("REALNAME");

    public final String value;

    PaymentBusinessType(String value) {
        this.value = value;
    }
}
