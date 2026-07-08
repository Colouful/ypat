package com.ypat;

import java.io.Serializable;

/**
 * 前端拿到后用 uni.requestPayment 调起微信支付。
 * 包结构与现有 OrderController.add 返回保持一致，便于前端复用支付调用。
 */
public class MemberOrderCreateResult implements Serializable {
    private String outTradeNo;
    private String appId;
    private String channel;
    private Integer amountFen;
    private PaymentPayParams payParams;
    private String h5Url;
    private String timeStamp;
    private String nonceStr;
    private String packageValue;
    private String signType;
    private String paySign;

    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public Integer getAmountFen() { return amountFen; }
    public void setAmountFen(Integer amountFen) { this.amountFen = amountFen; }
    public PaymentPayParams getPayParams() { return payParams; }
    public void setPayParams(PaymentPayParams payParams) { this.payParams = payParams; }
    public String getH5Url() { return h5Url; }
    public void setH5Url(String h5Url) { this.h5Url = h5Url; }
    public String getTimeStamp() { return timeStamp; }
    public void setTimeStamp(String timeStamp) { this.timeStamp = timeStamp; }
    public String getNonceStr() { return nonceStr; }
    public void setNonceStr(String nonceStr) { this.nonceStr = nonceStr; }
    public String getPackageValue() { return packageValue; }
    public void setPackageValue(String packageValue) { this.packageValue = packageValue; }
    public String getSignType() { return signType; }
    public void setSignType(String signType) { this.signType = signType; }
    public String getPaySign() { return paySign; }
    public void setPaySign(String paySign) { this.paySign = paySign; }
}
