package com.ypat.service;

import com.ypat.PaymentOrderQo;
import com.ypat.SysException;
import com.ypat.enums.PaymentBusinessType;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PaymentCallbackServiceTest {

    @Test(expected = SysException.class)
    public void amountMismatchRejectsEntitlementGrant() {
        FakePaymentOrderService payment = new FakePaymentOrderService(true, order(PaymentBusinessType.DEPOSIT.value, 1));
        FakeDepositService deposit = new FakeDepositService();
        PaymentCallbackService service = service(payment, deposit, new FakeMemberService());

        service.markPaid("D1", "tx1", 2, new Date(), "event1", "digest1");
    }

    @Test
    public void duplicateNotificationDoesNotGrantBusinessAgain() {
        FakePaymentOrderService payment = new FakePaymentOrderService(false, order(PaymentBusinessType.DEPOSIT.value, 1));
        FakeDepositService deposit = new FakeDepositService();
        PaymentCallbackService service = service(payment, deposit, new FakeMemberService());

        boolean first = service.markPaid("D1", "tx1", 1, new Date(), "event1", "digest1");

        assertFalse(first);
        assertEquals(0, deposit.markCount);
    }

    @Test
    public void firstDepositNotificationMarksPaymentAndDeposit() {
        FakePaymentOrderService payment = new FakePaymentOrderService(true, order(PaymentBusinessType.DEPOSIT.value, 1));
        FakeDepositService deposit = new FakeDepositService();
        PaymentCallbackService service = service(payment, deposit, new FakeMemberService());

        boolean first = service.markPaid("D1", "tx1", 1, new Date(), "event1", "digest1");

        assertTrue(first);
        assertEquals(1, payment.markCount);
        assertEquals(1, deposit.markCount);
        assertEquals("D1", deposit.lastOutTradeNo);
    }

    private static PaymentCallbackService service(PaymentOrderService payment,
                                                  DepositService deposit,
                                                  MemberService member) {
        PaymentCallbackService service = new PaymentCallbackService();
        ReflectionTestUtils.setField(service, "paymentOrderService", payment);
        ReflectionTestUtils.setField(service, "depositService", deposit);
        ReflectionTestUtils.setField(service, "memberService", member);
        return service;
    }

    private static PaymentOrderQo order(String businessType, Integer amountFen) {
        PaymentOrderQo qo = new PaymentOrderQo();
        qo.setOutTradeNo("D1");
        qo.setBusinessType(businessType);
        qo.setBusinessOrderNo("D1");
        qo.setAmountFen(amountFen);
        return qo;
    }

    private static class FakePaymentOrderService extends PaymentOrderService {
        private final boolean markResult;
        private final PaymentOrderQo order;
        int markCount;

        FakePaymentOrderService(boolean markResult, PaymentOrderQo order) {
            this.markResult = markResult;
            this.order = order;
        }

        @Override
        public PaymentOrderQo findByOutTradeNo(String outTradeNo) {
            return order;
        }

        @Override
        public boolean markPaidIfPending(String outTradeNo, String txId, Date paidAt, String eventId, String digest) {
            markCount++;
            return markResult;
        }
    }

    private static class FakeDepositService extends DepositService {
        int markCount;
        String lastOutTradeNo;

        @Override
        public boolean markPaid(String outTradeNo, String txId, Date paidAt) {
            markCount++;
            lastOutTradeNo = outTradeNo;
            return true;
        }
    }

    private static class FakeMemberService extends MemberService {
        @Override
        public boolean markPaid(String outTradeNo, String wxTransactionId, Date paidAt) {
            return true;
        }
    }
}
