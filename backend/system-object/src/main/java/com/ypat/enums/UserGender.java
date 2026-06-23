package com.ypat.enums;
public enum UserGender {
    wz("0","未知"),
    nan("1","男"),
    nv("2","女");

    public String value;
    public String name;

    UserGender(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for(UserGender target : UserGender.values()){
            if(target.value.equals(code)){
                return target.name;
            }
        }
        return "";
    }

}
