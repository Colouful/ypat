package com.ypat.enums;

public enum UserImgType {
    head("0","头像"),
    front("1","身份证正面"),
    back("2","身份证反面"),
    hand("3","手持");

    public String value;
    public String name;

    UserImgType(String value, String name) {
        this.value = value;
        this.name = name;
    }
}
