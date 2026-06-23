package com.ypat.enums;
public enum YpatStatus {
    zc("0","暂存"),
    ytj("1","待审核"),
    shtg("2","审核通过"),
    shbtg("3","审核未通过");

    public String value;
    public String name;

    YpatStatus(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for(YpatStatus target : YpatStatus.values()){
            if(target.value.equals(code)){
                return target.name;
            }
        }
        return "";
    }
}
