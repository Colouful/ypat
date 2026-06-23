package com.ypat.enums;

/**
 * 0.拍拍充值的账单 1.实名认证充值的账单 2.保证金充值的的账单。
 */
public enum OrderType {
    PPD("0","拍拍充值"),
    REAL("1","实名认证充值"),
    CRED("2","保证金充值");

    public String value;
    public String name;

    OrderType(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for(OrderType orderType : OrderType.values()){
            if(orderType.value.equals(code)){
                return orderType.name;
            }
        }
        return null;
    }

}
