package com.ypat.wallet.domain;

import jakarta.persistence.*;
import java.util.Date;

/**
 * PR-19: Wallet aggregate root.
 *
 * One row per user (UNIQUE user_id). The aggregate owns:
 *   - {@link #balance}            current balance, never negative
 *   - {@link #version}            optimistic-lock counter
 *   - the {@link #updatedAt}      audit field
 *
 * Strict invariants:
 *   - balance >= 0 (DB CHECK + JPA @PostLoad)
 *   - one row per user_id (UNIQUE)
 *
 * Concurrency: every change goes through
 * {@link com.ypat.wallet.application.WalletService} which
 * acquires SELECT ... FOR UPDATE on this row before any
 * {@link com.ypat.wallet.domain.LedgerEntry} is inserted.
 * JPA's optimistic locking (@Version) is a second line of
 * defense against lost updates.
 */
@Entity
@Table(name = "t_wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "balance", nullable = false)
    private Long balance;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getBalance() { return balance; }
    public Long getVersion() { return version; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setBalance(Long balance) { this.balance = balance; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}