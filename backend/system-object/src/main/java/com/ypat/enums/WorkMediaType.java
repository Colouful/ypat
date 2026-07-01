package com.ypat.enums;

/**
 * 作品媒体类型
 * 1=图片 2=视频
 */
public enum WorkMediaType {
    IMAGE("1", "图片"),
    VIDEO("2", "视频");

    public String value;
    public String name;

    WorkMediaType(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code){
        if(code == null) return "";
        for(WorkMediaType t : WorkMediaType.values()){
            if(t.value.equals(code)) return t.name;
        }
        return "";
    }

    public static boolean isValid(String code){
        if(code == null) return false;
        for(WorkMediaType t : WorkMediaType.values()){
            if(t.value.equals(code)) return true;
        }
        return false;
    }
}
