package com.ypat.enums;
/**
 * @author dingyinxin
 */

public enum ArticleStatus {
    zc("0","暂存"),
    fb("1","发布"),
    cg("2","撤回");

    public String value;
    public String name;

    ArticleStatus(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for(ArticleStatus target : ArticleStatus.values()){
            if(target.value.equals(code)){
                return target.name;
            }
        }
        return "";
    }
}
