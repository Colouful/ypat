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
import javax.persistence.Version;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_deposit_config")
public class DepositConfig implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1)
    private String enabled;

    @Column(name = "amount_fen", nullable = false)
    private Integer amountFen;

    @Column(name = "test_enabled", nullable = false, length = 1)
    private String testEnabled;

    @Column(name = "test_amount_fen", nullable = false)
    private Integer testAmountFen;

    @Column(name = "display_amount_fen", nullable = false)
    private Integer displayAmountFen;

    @Column(name = "realname_audit_fee_fen", nullable = false)
    private Integer realnameAuditFeeFen;

    @Column(name = "refund_wait_days", nullable = false)
    private Integer refundWaitDays;

    @Column(name = "early_refund_fee_rate", nullable = false)
    private Integer earlyRefundFeeRate;

    @Column(name = "agreement_summary", length = 512)
    private String agreementSummary;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Version
    private Integer version;

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
    public Integer getRealnameAuditFeeFen() { return realnameAuditFeeFen; }
    public void setRealnameAuditFeeFen(Integer realnameAuditFeeFen) { this.realnameAuditFeeFen = realnameAuditFeeFen; }
    public Integer getRefundWaitDays() { return refundWaitDays; }
    public void setRefundWaitDays(Integer refundWaitDays) { this.refundWaitDays = refundWaitDays; }
    public Integer getEarlyRefundFeeRate() { return earlyRefundFeeRate; }
    public void setEarlyRefundFeeRate(Integer earlyRefundFeeRate) { this.earlyRefundFeeRate = earlyRefundFeeRate; }
    public String getAgreementSummary() { return agreementSummary; }
    public void setAgreementSummary(String agreementSummary) { this.agreementSummary = agreementSummary; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
