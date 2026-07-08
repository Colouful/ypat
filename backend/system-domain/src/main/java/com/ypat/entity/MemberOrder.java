package com.ypat.entity;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import java.io.Serializable;
import java.util.Date;

/**
 * 会员订单。
 *
 * 状态机：
 *   0 待支付（创建后立即写入）
 *   1 已支付（回调 markPaid 后写入，必须只在 status=0 时更新以保证幂等）
 *   2 已取消
 *   3 已退款
 *
 * 幂等保证：`out_trade_no` 唯一约束 + markPaid 条件更新。
 */
@Entity
@Table(
        name = "t_member_order",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_out_trade_no", columnNames = "out_trade_no")
        },
        indexes = {
                @Index(name = "idx_user_status", columnList = "user_id, status")
        }
)
public class MemberOrder implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "out_trade_no", nullable = false, length = 64)
    private String outTradeNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "plan_code", nullable = false, length = 32)
    private String planCode;

    @Column(name = "plan_name_snapshot", length = 64)
    private String planNameSnapshot;

    @Column(name = "level_code_snapshot", length = 16)
    private String levelCodeSnapshot;

    /** 下单时锁定的套餐价格（分），由服务端从 plan_id 查出写入，前端不参与。 */
    @Column(name = "price_fen", nullable = false)
    private Integer priceFen;

    @Column(name = "origin_price_fen")
    private Integer originPriceFen;

    @Column(name = "gift_ppd")
    private Integer giftPpd;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(nullable = false, length = 1)
    private String status;

    @Column(name = "channel", length = 16)
    private String channel;

    @Column(name = "prepay_id", length = 128)
    private String prepayId;

    @Column(name = "wx_transaction_id", length = 64)
    private String wxTransactionId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    private Date paidAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    private Date credate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Version
    @Column(nullable = false)
    private Integer version;

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
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
