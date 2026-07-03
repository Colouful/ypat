package com.ypat.wallet.domain;

import jakarta.persistence.*;
import java.util.Date;

/**
 * PR-19: append-only wallet ledger entry.
 *
 * Every balance change is one row here. The {@code balanceAfter}
 * column makes point-in-time audits trivial: balance at row N
 * equals {@code balance_after}.
 *
 * Fields:
 *   - delta            +/- amount of this entry
 *   - balanceAfter      running balance after applying delta
 *   - reason            machine-readable category (DEPOSIT,
 *                       WITHDRAW, REFUND, ADMIN_ADJUST, ...)
 *   - refType / refId   optional foreign key into another
 *                       table (e.g. payment callback reference)
 *   - actorUserId       who triggered the change (null for
 *                       system / async events)
 *   - note              human-readable note (audit log)
 */
@Entity
@Table(name = "t_wallet_ledger")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "delta", nullable = false)
    private Long delta;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @Column(name = "reason", nullable = false, length = 64)
    private String reason;

    @Column(name = "ref_type", length = 32)
    private String refType;

    @Column(name = "ref_id", length = 64)
    private String refId;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "note", length = 255)
    private String note;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getDelta() { return delta; }
    public Long getBalanceAfter() { return balanceAfter; }
    public String getReason() { return reason; }
    public String getRefType() { return refType; }
    public String getRefId() { return refId; }
    public Long getActorUserId() { return actorUserId; }
    public String getNote() { return note; }
    public Date getCreatedAt() { return createdAt; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setDelta(Long delta) { this.delta = delta; }
    public void setBalanceAfter(Long balanceAfter) { this.balanceAfter = balanceAfter; }
    public void setReason(String reason) { this.reason = reason; }
    public void setRefType(String refType) { this.refType = refType; }
    public void setRefId(String refId) { this.refId = refId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }
    public void setNote(String note) { this.note = note; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}