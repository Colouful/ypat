package com.ypat.wallet.application;

import com.ypat.wallet.domain.LedgerEntry;
import com.ypat.wallet.domain.Wallet;
import com.ypat.wallet.infrastructure.LedgerEntryRepository;
import com.ypat.wallet.infrastructure.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * PR-19: wallet service.
 *
 * Single entry point for every balance change. Every public
 * method is {@code @Transactional} so the SELECT FOR UPDATE,
 * the ledger INSERT, and the balance UPDATE land in one
 * transaction. Two concurrent deposits on the same user will
 * serialize on the wallet row lock.
 *
 * Failure modes handled by {@code balance >= 0} check +
 * {@code chk_wallet_balance_nonneg} DB constraint:
 *   - Withdraw larger than balance -> DB CHECK rejects the
 *     UPDATE -> transaction rolls back -> caller sees
 *     InsufficientBalanceException (PR-19 follow-up maps).
 */
@Service
public class WalletService {

    private final WalletRepository wallets;
    private final LedgerEntryRepository ledger;

    public WalletService(WalletRepository wallets, LedgerEntryRepository ledger) {
        this.wallets = wallets;
        this.ledger = ledger;
    }

    @Transactional(readOnly = true)
    public long balance(long userId) {
        return wallets.findByUserId(userId).map(Wallet::getBalance).orElse(0L);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> history(long userId) {
        return ledger.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Apply a delta to the user's wallet. Negative deltas are
     * withdrawals and must not push the balance below zero.
     *
     * @param delta     positive for deposit, negative for withdraw
     * @param reason    machine-readable category
     * @param refType   optional reference type
     * @param refId     optional reference id
     * @param actorUserId  who triggered (null = system)
     * @param note      optional human-readable note
     */
    @Transactional
    public LedgerEntry apply(long userId, long delta, String reason,
                              String refType, String refId,
                              Long actorUserId, String note) {
        Wallet w = wallets.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Wallet missing for user " + userId
                                + " — V2__wallet_ledger pre-seed failed?"));

        long newBalance = w.getBalance() + delta;
        if (newBalance < 0) {
            throw new IllegalStateException(
                    "Insufficient balance: have " + w.getBalance()
                            + ", delta " + delta);
        }

        Date now = new Date();
        w.setBalance(newBalance);
        w.setUpdatedAt(now);
        wallets.save(w);                     // UPDATE t_wallet SET ...

        LedgerEntry e = new LedgerEntry();
        e.setUserId(userId);
        e.setDelta(delta);
        e.setBalanceAfter(newBalance);
        e.setReason(reason);
        e.setRefType(refType);
        e.setRefId(refId);
        e.setActorUserId(actorUserId);
        e.setNote(note);
        e.setCreatedAt(now);
        return ledger.save(e);                // INSERT t_wallet_ledger
    }
}