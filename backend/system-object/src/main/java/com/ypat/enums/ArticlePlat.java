package com.ypat.enums;
/**
 * @author dingyinxin
 */

public enum ArticlePlat {
    zc("00","爱去拍平台");

    public String value;
    public String name;

    ArticlePlat(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for(ArticlePlat target : ArticlePlat.values()){
            if(target.value.equals(code)){
                return target.name;
            }
        }
        return "";
    }
}
