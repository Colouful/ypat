package com.ypat.enums;
public enum YpatTarget {
    xwgm("0","约摄影师"),
    wysf("1","约模特"),
    xsxj("2","约摄像师"),
    sjfw("3","约商家"),
    hzsj("4","约化妆师"),
    xtsj("5","约修图师");

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

    /**
     * 校验 code 是否为合法值（兼容 6 类）
     */
    public static boolean isValid(String code){
        if(code == null) return false;
        for(YpatTarget t : YpatTarget.values()){
            if(t.value.equals(code)) return true;
        }
        return false;
    }
}
