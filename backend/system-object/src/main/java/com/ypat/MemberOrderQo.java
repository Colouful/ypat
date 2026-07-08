package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class MemberOrderQo extends PageQo implements Serializable {
    private Long id;
    private String outTradeNo;
    private Long userId;
    private Long planId;
    private String planCode;
    private String planNameSnapshot;
    private String levelCodeSnapshot;
    private Integer priceFen;
    private Integer originPriceFen;
    private Integer giftPpd;
    private Integer durationDays;
    private String status;
    private String channel;
    private String prepayId;
    private String wxTransactionId;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date paidAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date credate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }
    public String getPlanNameSnapshot() { return planNameSnapshot; }
    public void setPlanNameSnapshot(String planNameSnapshot) { this.planNameSnapshot = planNameSnapshot; }
    public String getLevelCodeSnapshot() { return levelCodeSnapshot; }
    public void setLevelCodeSnapshot(String levelCodeSnapshot) { this.levelCodeSnapshot = levelCodeSnapshot; }
    public Integer getPriceFen() { return priceFen; }
    public void setPriceFen(Integer priceFen) { this.priceFen = priceFen; }
    public Integer getOriginPriceFen() { return originPriceFen; }
    public void setOriginPriceFen(Integer originPriceFen) { this.originPriceFen = originPriceFen; }
    public Integer getGiftPpd() { return giftPpd; }
    public void setGiftPpd(Integer giftPpd) { this.giftPpd = giftPpd; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getPrepayId() { return prepayId; }
    public void setPrepayId(String prepayId) { this.prepayId = prepayId; }
    public String getWxTransactionId() { return wxTransactionId; }
    public void setWxTransactionId(String wxTransactionId) { this.wxTransactionId = wxTransactionId; }
    public Date getPaidAt() { return paidAt; }
    public void setPaidAt(Date paidAt) { this.paidAt = paidAt; }
    public Date getCredate() { return credate; }
    public void setCredate(Date credate) { this.credate = credate; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public String getStatusTxt() {
        if (status == null) return null;
        switch (status) {
            case "0": return "待支付";
            case "1": return "已支付";
            case "2": return "已取消";
            case "3": return "已退款";
            default: return status;
        }
    }
}
