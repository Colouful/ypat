package com.ypat;

import java.io.Serializable;

public class PaymentCreateResult implements Serializable {
    private String outTradeNo;
    private String businessType;
    private String channel;
    private Integer amountFen;
    private PaymentPayParams payParams;
    private String h5Url;

    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public Integer getAmountFen() { return amountFen; }
    public void setAmountFen(Integer amountFen) { this.amountFen = amountFen; }
    public PaymentPayParams getPayParams() { return payParams; }
    public void setPayParams(PaymentPayParams payParams) { this.payParams = payParams; }
    public String getH5Url() { return h5Url; }
    public void setH5Url(String h5Url) { this.h5Url = h5Url; }
}
