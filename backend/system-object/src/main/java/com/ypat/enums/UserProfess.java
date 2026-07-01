package com.ypat.enums;

/**
 *
 * @author dingyinxin
 * @Descent 用户类型
 */
public enum UserProfess {
    sys("0","摄影师"),
    mt("1","模特"),
    zzs("2","妆造师"),
    xts("3","修图师"),
    gr("4","个人"),
    yy("5","演员"),
    sj("6","商家"),
    qt("7","其他"),
    sr("8","素人模特");

    public String value;
    public String name;

    UserProfess(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for(UserProfess target : UserProfess.values()){
            if(target.value.equals(code)){
                return target.name;
            }
        }
        return "";
    }

    /**
     * 校验 code 是否为合法值
     */
    public static boolean isValid(String code){
        if(code == null) return false;
        for(UserProfess p : UserProfess.values()){
            if(p.value.equals(code)) return true;
        }
        return false;
    }

}
