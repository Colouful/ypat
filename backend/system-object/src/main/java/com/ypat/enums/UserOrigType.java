package com.ypat.enums;

public enum UserOrigType {
    wx("0","微信"),
    bd("1","百度"),
    pc("2","PC");

    public String value;
    public String name;

    UserOrigType(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for(UserOrigType target : UserOrigType.values()){
            if(target.value.equals(code)){
                return target.name;
            }
        }
        return "";
    }
}
