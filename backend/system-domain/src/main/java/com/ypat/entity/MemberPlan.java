package com.ypat.entity;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * 会员套餐。运营可配置多档（包月/包季/包年），价格由服务端决定，前端不能传入最终价格。
 */
@Entity
@Table(name = "t_member_plan")
public class MemberPlan implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String code;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    /** 当前售价（分）。 */
    @Column(name = "price_fen", nullable = false)
    private Integer priceFen;

    /** 划线价（分），可空。 */
    @Column(name = "origin_price_fen")
    private Integer originPriceFen;

    @Column(name = "gift_ppd")
    private Integer giftPpd;

    @Column(name = "level_code", length = 16)
    private String levelCode;

    @Column(length = 1)
    private String recommended;

    /** 权益摘要，前端展示。 */
    @Column(length = 512)
    private String benefits;

    @Column(nullable = false, length = 1)
    private String status; // 1 上架 / 0 下架

    @Column(name = "sort_no")
    private Integer sortNo;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    private Date credate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
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
