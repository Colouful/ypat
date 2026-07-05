package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class MemberPlanQo implements Serializable {
    private Long id;
    private String code;
    private String name;
    private Integer durationDays;
    private Integer priceFen;
    private Integer originPriceFen;
    private Integer giftPpd;
    private String levelCode;
    private String recommended;
    private String benefits;
    private String status;
    private Integer sortNo;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date credate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public Integer getPriceFen() { return priceFen; }
    public void setPriceFen(Integer priceFen) { this.priceFen = priceFen; }
    public Integer getOriginPriceFen() { return originPriceFen; }
    public void setOriginPriceFen(Integer originPriceFen) { this.originPriceFen = originPriceFen; }
    public Integer getGiftPpd() { return giftPpd; }
    public void setGiftPpd(Integer giftPpd) { this.giftPpd = giftPpd; }
    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    public String getRecommended() { return recommended; }
    public void setRecommended(String recommended) { this.recommended = recommended; }
    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public Date getCredate() { return credate; }
    public void setCredate(Date credate) { this.credate = credate; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
