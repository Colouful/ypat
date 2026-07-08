package com.ypat;

import java.io.Serializable;

public class PaymentPayParams implements Serializable {
    private String timeStamp;
    private String nonceStr;
    private String packageValue;
    private String signType;
    private String paySign;

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
