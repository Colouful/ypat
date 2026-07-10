package com.ypat.payment;

import com.ypat.PaymentOrderQo;
import com.ypat.enums.PaymentStatus;
import com.ypat.service.PaymentOrderServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Date;

@Service
public class WechatPaymentReconcileService {

    private static final Logger logger = LoggerFactory.getLogger(WechatPaymentReconcileService.class);
    private static final String WECHAT_SUCCESS = "SUCCESS";

    @Autowired
    private WechatPaymentService wechatPaymentService;
    @Autowired
    private PaymentOrderServiceClient paymentOrderServiceClient;

    public boolean syncPaidIfWechatSuccess(String outTradeNo) {
        PaymentOrderQo payment = paymentOrderServiceClient.get(outTradeNo);
        if (payment == null || !PaymentStatus.PENDING.value.equals(payment.getStatus())) {
            return false;
        }

        WechatNotifyPayload payload;
        try {
            payload = wechatPaymentService.queryOrder(outTradeNo);
        } catch (Exception ex) {
            logger.warn("wechat.payment.reconcile.query_failed outTradeNo={}", outTradeNo, ex);
            return false;
        }
        if (payload == null || !WECHAT_SUCCESS.equals(payload.getTradeState())) {
            return false;
        }

        Boolean synced = paymentOrderServiceClient.markPaid(payload.getOutTradeNo(),
                payload.getTransactionId(),
                payload.getAmountFen(),
                paidAtMs(payload.getSuccessTime()),
                reconcileEventId(payload),
                "WECHAT_QUERY");
        boolean result = Boolean.TRUE.equals(synced);
        logger.info("wechat.payment.reconcile.success outTradeNo={} txId={} first={}",
                payload.getOutTradeNo(), payload.getTransactionId(), result);
        return result;
    }

    private Long paidAtMs(String successTime) {
        if (successTime == null || successTime.trim().isEmpty()) {
            return System.currentTimeMillis();
        }
        return Date.from(OffsetDateTime.parse(successTime).toInstant()).getTime();
    }

    private String reconcileEventId(WechatNotifyPayload payload) {
        if (payload.getTransactionId() != null && !payload.getTransactionId().trim().isEmpty()) {
            return "QUERY:" + payload.getTransactionId();
        }
        return "QUERY:" + payload.getOutTradeNo();
    }
}
