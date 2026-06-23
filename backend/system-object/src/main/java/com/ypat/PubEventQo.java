package com.ypat;

import com.ypat.enums.PubEventKey;

public class PubEventQo extends PageQo implements java.io.Serializable {

    private Long id;
    private String toUserName;
    private String fromUserName;
    private Long createTime;
    private String msgType;
    private String event;
    private String eventKey;
    private String ticket;
    private String dateStr;
    private Integer msgTimes;
    private String eventKeyTxt;
    private String dateStrStart;
    private String dateStrEnd;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public Integer getMsgTimes() {
        return msgTimes;
    }

    public void setMsgTimes(Integer msgTimes) {
        this.msgTimes = msgTimes;
    }

    public String getEventKeyTxt() {
        return PubEventKey.getNameByCode(this.eventKey);
    }

    public void setEventKeyTxt(String eventKeyTxt) {
        this.eventKeyTxt = eventKeyTxt;
    }

    public String getDateStrStart() {
        return dateStrStart;
    }

    public void setDateStrStart(String dateStrStart) {
        this.dateStrStart = dateStrStart;
    }

    public String getDateStrEnd() {
        return dateStrEnd;
    }

    public void setDateStrEnd(String dateStrEnd) {
        this.dateStrEnd = dateStrEnd;
    }
}
