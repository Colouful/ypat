package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class DepositConfigQo implements Serializable {
    private Long id;
    private String enabled;
    private Integer amountFen;
    private String testEnabled;
    private Integer testAmountFen;
    private Integer displayAmountFen;
    private Integer refundWaitDays;
    private Integer earlyRefundFeeRate;
    private String agreementSummary;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public Integer getAmountFen() { return amountFen; }
    public void setAmountFen(Integer amountFen) { this.amountFen = amountFen; }
    public String getTestEnabled() { return testEnabled; }
    public void setTestEnabled(String testEnabled) { this.testEnabled = testEnabled; }
    public Integer getTestAmountFen() { return testAmountFen; }
    public void setTestAmountFen(Integer testAmountFen) { this.testAmountFen = testAmountFen; }
    public Integer getDisplayAmountFen() { return displayAmountFen; }
    public void setDisplayAmountFen(Integer displayAmountFen) { this.displayAmountFen = displayAmountFen; }
    public Integer getRefundWaitDays() { return refundWaitDays; }
    public void setRefundWaitDays(Integer refundWaitDays) { this.refundWaitDays = refundWaitDays; }
    public Integer getEarlyRefundFeeRate() { return earlyRefundFeeRate; }
    public void setEarlyRefundFeeRate(Integer earlyRefundFeeRate) { this.earlyRefundFeeRate = earlyRefundFeeRate; }
    public String getAgreementSummary() { return agreementSummary; }
    public void setAgreementSummary(String agreementSummary) { this.agreementSummary = agreementSummary; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
