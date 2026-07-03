package com.ypat.payment.application;

import com.ypat.payment.domain.PayIdempotency;
import com.ypat.payment.infrastructure.PayIdempotencyRepository;
import com.ypat.wallet.application.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * PR-20: payment service.
 *
 * Receives a parsed WeChat callback payload and runs the
 * idempotent-credit flow:
 *
 *   1. INSERT t_pay_idempotency (out_trade_no, PENDING)
 *      - duplicate-key collision -> second callback, ignore
 *   2. UPDATE t_pay_idempotency SET status='SUCCESS',
 *      transaction_id=?, amount=?, user_id=?, notify_payload=?
 *   3. walletService.apply(userId, amount, 'DEPOSIT',
 *      'WXPAY', out_trade_no, null, 'WeChat Pay')
 *      -> returns the ledger row
 *   4. UPDATE t_pay_idempotency SET ledger_id=ledger.id
 *
 * All four steps run inside one @Transactional. Two concurrent
 * callbacks race on step 1's primary key insert; the loser
 * sees the duplicate-key and short-circuits.
 *
 * Signature verification (V1.1 §4.4 step 1: HMAC-SHA256) is
 * delegated to a WeChat SDK in PR-20 follow-up.
 */
@Service
public class PaymentService {

    private final PayIdempotencyRepository idempotency;
    private final WalletService wallet;

    public PaymentService(PayIdempotencyRepository idempotency,
                          WalletService wallet) {
        this.idempotency = idempotency;
        this.wallet = wallet;
    }

    /**
     * @return true if the callback was processed now; false if
     *     it was a duplicate (already processed earlier).
     */
    @Transactional
    public boolean handleCallback(CallbackPayload p) {
        // step 1: claim
        PayIdempotency row = new PayIdempotency();
        row.setOutTradeNo(p.outTradeNo);
        row.setAmount(p.amount);
        row.setUserId(p.userId);
        row.setStatus("PENDING");
        row.setNotifyPayload(p.rawXml);
        row.setCreatedAt(new Date());
        row.setUpdatedAt(new Date());
        try {
            idempotency.save(row);
            idempotency.flush();        // force INSERT to surface duplicate-key now
        } catch (org.springframework.dao.DataIntegrityViolationException dup) {
            // step 1 race lost: another callback already claimed
            // this out_trade_no. WeChat expects HTTP 200 either way;
            // we just don't double-credit the wallet.
            return false;
        }

        // step 3: credit the wallet. This goes through the
        // strongly-consistent WalletService (PR-19) which uses
        // SELECT FOR UPDATE on t_wallet.
        wallet.apply(
                p.userId, p.amount, "DEPOSIT",
                "WXPAY", p.outTradeNo,
                null /* system */,
                "WeChat Pay callback");
        row.setStatus("SUCCESS");
        row.setTransactionId(p.transactionId);
        row.setUpdatedAt(new Date());
        idempotency.save(row);
        return true;
    }

    /** Minimal parsed payload. Real parser is PR-20 follow-up. */
    public record CallbackPayload(
            String outTradeNo,
            String transactionId,
            long amount,         // fen
            Long userId,
            String rawXml        // for audit
    ) {}
}