package com.ypat.payment;

import com.ypat.PaymentOrderQo;
import com.ypat.enums.PaymentStatus;
import com.ypat.service.PaymentOrderServiceClient;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WechatPaymentReconcileServiceTest {

    @Test
    public void syncPaidIfWechatSuccessMarksPendingPaymentPaid() {
        WechatPaymentReconcileService service = new WechatPaymentReconcileService();
        FakeWechatPaymentService paymentService = new FakeWechatPaymentService();
        FakePaymentOrderServiceClient paymentOrderClient = new FakePaymentOrderServiceClient();
        ReflectionTestUtils.setField(service, "wechatPaymentService", paymentService);
        ReflectionTestUtils.setField(service, "paymentOrderServiceClient", paymentOrderClient);

        boolean synced = service.syncPaidIfWechatSuccess("D202607092046544F591B0");

        assertTrue(synced);
        assertEquals("D202607092046544F591B0", paymentService.queriedOutTradeNo);
        assertEquals("D202607092046544F591B0", paymentOrderClient.markPaidOutTradeNo);
        assertEquals("4200003205202607093033151848", paymentOrderClient.markPaidTxId);
        assertEquals(Integer.valueOf(1), paymentOrderClient.markPaidAmountFen);
        assertEquals(Long.valueOf(1783601325000L), paymentOrderClient.markPaidAtMs);
    }

    private static class FakeWechatPaymentService extends WechatPaymentService {
        String queriedOutTradeNo;

        FakeWechatPaymentService() {
            super(null);
        }

        @Override
        public WechatNotifyPayload queryOrder(String outTradeNo) {
            queriedOutTradeNo = outTradeNo;
            WechatNotifyPayload payload = new WechatNotifyPayload();
            payload.setOutTradeNo(outTradeNo);
            payload.setTransactionId("4200003205202607093033151848");
            payload.setTradeState("SUCCESS");
            payload.setAmountFen(1);
            payload.setSuccessTime("2026-07-09T20:48:45+08:00");
            return payload;
        }
    }

    private static class FakePaymentOrderServiceClient implements PaymentOrderServiceClient {
        String markPaidOutTradeNo;
        String markPaidTxId;
        Integer markPaidAmountFen;
        Long markPaidAtMs;

        @Override
        public PaymentOrderQo createPending(String businessType, String businessOrderNo, String outTradeNo,
                                            Long userId, String channel, Integer amountFen) {
            return null;
        }

        @Override
        public PaymentOrderQo get(String outTradeNo) {
            PaymentOrderQo qo = new PaymentOrderQo();
            qo.setOutTradeNo(outTradeNo);
            qo.setAmountFen(1);
            qo.setStatus(PaymentStatus.PENDING.value);
            return qo;
        }

        @Override
        public PaymentOrderQo prepared(String outTradeNo, String channel, String prepayId, String h5Url) {
            return null;
        }

        @Override
        public Boolean markPaid(String outTradeNo, String txId, Integer amountFen,
                                Long paidAtMs, String eventId, String digest) {
            markPaidOutTradeNo = outTradeNo;
            markPaidTxId = txId;
            markPaidAmountFen = amountFen;
            markPaidAtMs = paidAtMs;
            return true;
        }

        @Override
        public Map<String, Object> adminOrders(PaymentOrderQo qo) {
            return new HashMap<>();
        }
    }
}
