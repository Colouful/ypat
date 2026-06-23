package com.ypat.enums;
public enum UserStatus {
    zc("0","暂存"),
    ytj("1","待审核"),
    shtg("2","审核通过"),
    shbtg("3","审核未通过"),
    zfcg("4","支付成功");

    public String value;
    public String name;

    UserStatus(String value, String name) {
        this.value = value;
        this.name = name;
    }
    public static String getNameByCode(String code){
        for(UserStatus target : UserStatus.values()){
            if(target.value.equals(code)){
                return target.name;
            }
        }
        return "";
    }

}
