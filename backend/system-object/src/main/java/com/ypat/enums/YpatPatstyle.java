package com.ypat.enums;
public enum YpatPatstyle {
    s0("0","复古"),
    s1("1","INS"),
    s2("2","胶片"),
    s3("3","少女"),
    s4("4","暗黑"),
    s5("5","情绪"),
    s6("6","夜景"),
    s7("7","欧美"),
    s8("8","商务"),
    s9("9","韩系"),
    s10("10","日系"),
    s11("11","情侣"),
    s12("12","样片");

    public String value;
    public String name;

    YpatPatstyle(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for(YpatPatstyle target : YpatPatstyle.values()){
            if(target.value.equals(code)){
                return target.name;
            }
        }
        return "";
    }
}
