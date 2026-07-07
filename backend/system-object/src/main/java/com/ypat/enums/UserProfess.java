package com.ypat.enums;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author dingyinxin
 * @Descent 用户类型
 */
public enum UserProfess {
    sys("0","摄影师", true),
    mt("1","模特", true),
    zzs("2","化妆师", true),
    xts("3","修图师", true),
    gr("4","个人", false),
    yy("5","演员", false),
    sj("6","商家", true),
    qt("7","其他", false),
    sr("8","素人模特", false),
    sxj("9","摄像师", true);

    public String value;
    public String name;
    public boolean publicOption;

    UserProfess(String value, String name, boolean publicOption) {
        this.value = value;
        this.name = name;
        this.publicOption = publicOption;
    }

    public static String getNameByCode(String code){
        for(UserProfess target : UserProfess.values()){
            if(target.value.equals(code)){
                return target.name;
            }
        }
        return "";
    }

    public static List<String> getPublicValues(){
        return Arrays.asList(sj.value, sys.value, zzs.value, sxj.value, xts.value, mt.value);
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
