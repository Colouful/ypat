package com.ypat.enums;

public enum MessagePushEventType {
    IN_APP_CREATED("IN_APP_CREATED", "站内消息创建"),
    WECHAT_SUBSCRIBE_SENT("WECHAT_SUBSCRIBE_SENT", "微信订阅发送");

    public String value;
    public String name;

    MessagePushEventType(String value, String name) {
        this.value = value;
        this.name = name;
    }
}
