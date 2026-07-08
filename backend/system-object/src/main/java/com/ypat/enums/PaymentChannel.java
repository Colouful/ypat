package com.ypat.enums;

public enum PaymentChannel {
    MINIAPP("MINIAPP"),
    H5("H5"),
    APP("APP");

    public final String value;

    PaymentChannel(String value) {
        this.value = value;
    }

    public static boolean supportedForCreate(String raw) {
        return MINIAPP.value.equals(raw) || H5.value.equals(raw);
    }
}
