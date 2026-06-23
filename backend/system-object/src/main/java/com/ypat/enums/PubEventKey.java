package com.ypat.enums;

public enum PubEventKey {
    WX("1","微信"),
    BD("2","百度"),
    PC("3","PC");

    public String value;
    public String name;

    PubEventKey(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for(PubEventKey target : PubEventKey.values()){
            if(target.value.equals(code)){
                return target.name;
            }
        }
        return "";
    }
}
