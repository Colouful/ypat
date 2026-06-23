package com.ypat.enums;

public enum MessType {
    send("1","约拍"),
    oauth("2","实名认证审核"),
    audit("3","发布信息审核"),
    view("4","已查看消息"),
    order("5", "订单通知");

    public String value;
    public String name;

    MessType(String value, String name) {
        this.value = value;
        this.name = name;
    }
}
