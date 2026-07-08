package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class PaymentOrderQo extends PageQo implements Serializable {
    private Long id;
    private String paymentNo;
    private String businessType;
    private String businessOrderNo;
    private String outTradeNo;
    private Long userId;
    private String channel;
    private Integer amountFen;
    private String status;
    private String prepayId;
    private String h5Url;
    private String transactionId;
    private String wechatTradeState;
    private String notifyEventId;
    private String notifyDigest;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date paidAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getBusinessOrderNo() { return businessOrderNo; }
    public void setBusinessOrderNo(String businessOrderNo) { this.businessOrderNo = businessOrderNo; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public Integer getAmountFen() { return amountFen; }
    public void setAmountFen(Integer amountFen) { this.amountFen = amountFen; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPrepayId() { return prepayId; }
    public void setPrepayId(String prepayId) { this.prepayId = prepayId; }
    public String getH5Url() { return h5Url; }
    public void setH5Url(String h5Url) { this.h5Url = h5Url; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getWechatTradeState() { return wechatTradeState; }
    public void setWechatTradeState(String wechatTradeState) { this.wechatTradeState = wechatTradeState; }
    public String getNotifyEventId() { return notifyEventId; }
    public void setNotifyEventId(String notifyEventId) { this.notifyEventId = notifyEventId; }
    public String getNotifyDigest() { return notifyDigest; }
    public void setNotifyDigest(String notifyDigest) { this.notifyDigest = notifyDigest; }
    public Date getPaidAt() { return paidAt; }
    public void setPaidAt(Date paidAt) { this.paidAt = paidAt; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
