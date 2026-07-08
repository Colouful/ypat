package com.ypat.enums;

public enum PaymentStatus {
    PENDING("PENDING"),
    PAID("PAID"),
    FAILED("FAILED"),
    CLOSED("CLOSED"),
    REFUNDED("REFUNDED");

    public final String value;

    PaymentStatus(String value) {
        this.value = value;
    }
}
