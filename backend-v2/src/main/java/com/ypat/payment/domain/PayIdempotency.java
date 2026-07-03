package com.ypat.payment.domain;

import jakarta.persistence.*;
import java.util.Date;

/**
 * PR-20: idempotency record for one WeChat pay callback.
 *
 * Primary key is {@code out_trade_no} (the merchant-side order
 * id). Two callbacks with the same out_trade_no race on this
 * row's primary key insert; the loser sees a duplicate-key
 * error and is treated as a duplicate delivery.
 */
@Entity
@Table(name = "t_pay_idempotency")
public class PayIdempotency {

    @Id
    @Column(name = "out_trade_no", length = 64, nullable = false)
    private String outTradeNo;

    @Column(name = "transaction_id", length = 64)
    private String transactionId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @Lob
    @Column(name = "notify_payload")
    private String notifyPayload;

    @Column(name = "ledger_id")
    private Long ledgerId;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "error_message", length = 255)
    private String errorMessage;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    public String getOutTradeNo() { return outTradeNo; }
    public String getTransactionId() { return transactionId; }
    public Long getAmount() { return amount; }
    public Long getUserId() { return userId; }
    public String getStatus() { return status; }
    public String getNotifyPayload() { return notifyPayload; }
    public Long getLedgerId() { return ledgerId; }
    public String getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }

    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setAmount(Long amount) { this.amount = amount; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setStatus(String status) { this.status = status; }
    public void setNotifyPayload(String notifyPayload) { this.notifyPayload = notifyPayload; }
    public void setLedgerId(Long ledgerId) { this.ledgerId = ledgerId; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}