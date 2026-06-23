package com.ypat.enums;
public enum YpatChargeWay {
    xwgm("0","希望互勉"),
    wysf("1","我要收费"),
    kff("2","可付费"),
    fyxs("3","费用协商");

    public String value;
    public String name;

    YpatChargeWay(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for(YpatChargeWay chargeWay : YpatChargeWay.values()){
            if(chargeWay.value.equals(code)){
                return chargeWay.name;
            }
        }
        return "";
    }
}
