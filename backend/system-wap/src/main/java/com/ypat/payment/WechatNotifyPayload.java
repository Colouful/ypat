package com.ypat.payment;

import java.io.Serializable;

public class WechatNotifyPayload implements Serializable {
    private String eventId;
    private String outTradeNo;
    private String transactionId;
    private String tradeState;
    private Integer amountFen;
    private String successTime;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getTradeState() { return tradeState; }
    public void setTradeState(String tradeState) { this.tradeState = tradeState; }
    public Integer getAmountFen() { return amountFen; }
    public void setAmountFen(Integer amountFen) { this.amountFen = amountFen; }
    public String getSuccessTime() { return successTime; }
    public void setSuccessTime(String successTime) { this.successTime = successTime; }
}
