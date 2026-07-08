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

@Entity
@Table(
        name = "t_payment_order",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_no", columnNames = "payment_no"),
                @UniqueConstraint(name = "uk_out_trade_no", columnNames = "out_trade_no")
        },
        indexes = {
                @Index(name = "idx_payment_order_business", columnList = "business_type, business_order_no"),
                @Index(name = "idx_user_status", columnList = "user_id, status"),
                @Index(name = "idx_payment_order_user_created", columnList = "user_id, created_at"),
                @Index(name = "idx_status_updated_at", columnList = "status, updated_at"),
                @Index(name = "idx_notify_event_id", columnList = "notify_event_id")
        }
)
public class PaymentOrder implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_no", nullable = false, length = 64)
    private String paymentNo;

    @Column(name = "business_type", nullable = false, length = 16)
    private String businessType;

    @Column(name = "business_order_no", nullable = false, length = 64)
    private String businessOrderNo;

    @Column(name = "out_trade_no", nullable = false, length = 64)
    private String outTradeNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 16)
    private String channel;

    @Column(name = "amount_fen", nullable = false)
    private Integer amountFen;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "prepay_id", length = 128)
    private String prepayId;

    @Column(name = "h5_url", length = 1024)
    private String h5Url;

    @Column(name = "transaction_id", length = 64)
    private String transactionId;

    @Column(name = "wechat_trade_state", length = 32)
    private String wechatTradeState;

    @Column(name = "notify_event_id", length = 128)
    private String notifyEventId;

    @Column(name = "notify_digest", length = 128)
    private String notifyDigest;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "paid_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date paidAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Version
    @Column(nullable = false)
    private Integer version;

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
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
