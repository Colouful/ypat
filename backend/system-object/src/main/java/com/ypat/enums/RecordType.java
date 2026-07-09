package com.ypat.enums;

/**
 * 0.充值、1.好友邀请、2.系统赠送、3.发布约拍、4.申请约拍、5.查看联系方式、6.每日签到
 */
public enum RecordType {

    PAY("0","充值"),
    FRI("1","好友邀请"),
    SYS("2","系统赠送"),
    PUB("3","发布约拍"),
    APP("4","申请约拍"),
    VIEW("5","查看联系方式"),
    CHECKIN("6","每日签到");


    public String value;
    public String name;

    RecordType(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for(RecordType recordType : RecordType.values()){
            if(recordType.value.equals(code)){
                return recordType.name;
            }
        }
        return null;
    }
}
