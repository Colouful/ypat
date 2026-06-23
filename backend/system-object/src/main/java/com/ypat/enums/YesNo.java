package com.ypat.enums;

public enum YesNo {
    no("0","否"),
    yes("1","是");

    public String value;
    public String name;

    YesNo(String value, String name) {
        this.value = value;
        this.name = name;
    }
}
