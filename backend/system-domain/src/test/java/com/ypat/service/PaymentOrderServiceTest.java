package com.ypat.service;

import com.ypat.repository.PaymentOrderRepository;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PaymentOrderServiceTest {

    @Test
    public void markPaidIfPendingReturnsTrueOnlyForFirstPendingUpdate() {
        PaymentOrderService service = new PaymentOrderService();
        MarkPaidCapture capture = new MarkPaidCapture(1, 0);
        ReflectionTestUtils.setField(service, "paymentOrderRepository", paymentOrderRepository(capture));
        Date paidAt = new Date(1773000000000L);

        boolean first = service.markPaidIfPending("P202607080001", "wx-tx-1", paidAt, "event-1", "digest-1");
        boolean duplicate = service.markPaidIfPending("P202607080001", "wx-tx-1", paidAt, "event-1", "digest-1");

        assertTrue(first);
        assertFalse(duplicate);
        assertSame(paidAt, capture.lastPaidAt);
        assertSame(paidAt, capture.lastNow);
    }

    @Test
    public void markPaidIfPendingUsesCurrentTimeWhenPaidAtIsNull() {
        PaymentOrderService service = new PaymentOrderService();
        MarkPaidCapture capture = new MarkPaidCapture(1);
        ReflectionTestUtils.setField(service, "paymentOrderRepository", paymentOrderRepository(capture));

        assertTrue(service.markPaidIfPending("P202607080002", "wx-tx-2", null, "event-2", "digest-2"));
        assertTrue(capture.lastPaidAt != null);
        assertSame(capture.lastPaidAt, capture.lastNow);
    }

    private static PaymentOrderRepository paymentOrderRepository(final MarkPaidCapture capture) {
        return proxy(PaymentOrderRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("markPaidIfPending".equals(method.getName())) {
                    capture.lastOutTradeNo = (String) args[0];
                    capture.lastTxId = (String) args[1];
                    capture.lastPaidAt = (Date) args[2];
                    capture.lastNow = (Date) args[3];
                    capture.lastEventId = (String) args[4];
                    capture.lastDigest = (String) args[5];
                    return capture.nextResult();
                }
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (Object.class.equals(method.getDeclaringClass())) {
                    if ("toString".equals(method.getName())) return type.getSimpleName() + "Proxy";
                    if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                    if ("equals".equals(method.getName())) return proxy == args[0];
                }
                return handler.invoke(proxy, method, args);
            }
        }));
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (boolean.class.equals(type)) return false;
        if (int.class.equals(type)) return 0;
        if (long.class.equals(type)) return 0L;
        return null;
    }

    private static class MarkPaidCapture {
        private final int[] results;
        private int index;
        String lastOutTradeNo;
        String lastTxId;
        Date lastPaidAt;
        Date lastNow;
        String lastEventId;
        String lastDigest;

        MarkPaidCapture(int... results) {
            this.results = results;
        }

        int nextResult() {
            if (index >= results.length) return 0;
            return results[index++];
        }
    }
}
