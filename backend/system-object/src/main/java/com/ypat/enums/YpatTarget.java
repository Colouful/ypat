package com.ypat.enums;
public enum YpatTarget {
    xwgm("0","约摄影师"),
    wysf("1","约模特");

    public String value;
    public String name;

    YpatTarget(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for(YpatTarget target : YpatTarget.values()){
            if(target.value.equals(code)){
                return target.name;
            }
        }
        return "";
    }
}
