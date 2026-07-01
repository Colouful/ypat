package com.ypat.enums;

/**
 * 作品状态
 * 0=暂存 1=待审核 2=审核通过 3=审核未通过 4=已下架
 */
public enum WorkStatus {
    zc("0","暂存"),
    ytj("1","待审核"),
    shtg("2","审核通过"),
    shbtg("3","审核未通过"),
    xj("4","已下架");

    public String value;
    public String name;

    WorkStatus(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code){
        if(code == null) return "";
        for(WorkStatus s : WorkStatus.values()){
            if(s.value.equals(code)) return s.name;
        }
        return "";
    }

    public static boolean isValid(String code){
        if(code == null) return false;
        for(WorkStatus s : WorkStatus.values()){
            if(s.value.equals(code)) return true;
        }
        return false;
    }
}
