package com.ypat.enums;

/**
 * @author dingyinxin
 */

public enum MessStatus {
    tg("0","通过"),
    btg("1","未通过");

    public String value;
    public String name;

    MessStatus(String value, String name) {
        this.value = value;
        this.name = name;
    }
}
