package com.ypat.service;

import com.ypat.DepositConfigQo;
import com.ypat.DepositOrderQo;
import com.ypat.entity.DepositConfig;
import com.ypat.entity.DepositOrder;
import com.ypat.entity.User;
import com.ypat.repository.DepositConfigRepository;
import com.ypat.repository.DepositOrderRepository;
import com.ypat.repository.UserRepository;
import org.junit.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DepositServiceTest {

    @Test
    public void getConfigUsesOneFenDisplayAmountWhenTestEnabled() {
        DepositService service = new DepositService();
        ReflectionTestUtils.setField(service, "depositConfigRepository", depositConfigRepository(config("1", 19900, 1)));

        DepositConfigQo qo = service.getConfig();

        assertEquals(Integer.valueOf(1), qo.getDisplayAmountFen());
    }

    @Test
    public void getConfigUsesSqlDefaultsWhenConfigMissing() {
        DepositService service = new DepositService();
        ReflectionTestUtils.setField(service, "depositConfigRepository", depositConfigRepository(null));

        DepositConfigQo qo = service.getConfig();

        assertEquals("1", qo.getTestEnabled());
        assertEquals(Integer.valueOf(19900), qo.getAmountFen());
        assertEquals(Integer.valueOf(1), qo.getTestAmountFen());
        assertEquals(Integer.valueOf(1), qo.getDisplayAmountFen());
        assertEquals(Integer.valueOf(90), qo.getRefundWaitDays());
        assertEquals(Integer.valueOf(15), qo.getEarlyRefundFeeRate());
    }

    @Test
    public void createPendingOrderUsesTestAmountWhenTestEnabled() {
        DepositService service = new DepositService();
        SavedDepositOrder saved = new SavedDepositOrder();
        ReflectionTestUtils.setField(service, "depositConfigRepository", depositConfigRepository(config("1", 19900, 1)));
        ReflectionTestUtils.setField(service, "depositOrderRepository", depositOrderRepository(0, null, saved));

        DepositOrderQo qo = service.createPendingOrder(7L, "MINIAPP");

        assertNotNull(qo.getOutTradeNo());
        assertTrue(qo.getOutTradeNo().startsWith("D"));
        assertEquals(Integer.valueOf(1), qo.getAmountFen());
        assertSame(saved.order, saved.lastSaved);
    }

    @Test
    public void createPendingOrderReusesRecentPendingSameChannelAndAmount() {
        DepositService service = new DepositService();
        DepositOrder existing = order("D202607080001", 7L, "MINIAPP", 1, "PENDING", new Date());
        SavedDepositOrder saved = new SavedDepositOrder();
        ReflectionTestUtils.setField(service, "depositConfigRepository", depositConfigRepository(config("1", 19900, 1)));
        ReflectionTestUtils.setField(service, "depositOrderRepository", depositOrderRepository(0, existing, saved));

        DepositOrderQo qo = service.createPendingOrder(7L, "MINIAPP");

        assertEquals("D202607080001", qo.getOutTradeNo());
        assertEquals(Integer.valueOf(1), qo.getAmountFen());
        assertNull(saved.lastSaved);
    }

    @Test
    public void markPaidSetsUserCreditFlagOnlyForFirstPendingUpdate() {
        DepositService service = new DepositService();
        DepositOrder order = order("D202607080002", 7L, "MINIAPP", 1, "PENDING", new Date());
        User user = new User();
        user.setId(7L);
        SaveCounter userSaves = new SaveCounter();
        ReflectionTestUtils.setField(service, "depositOrderRepository", depositOrderRepository(1, order, new SavedDepositOrder()));
        ReflectionTestUtils.setField(service, "userRepository", userRepository(user, userSaves));

        boolean result = service.markPaid("D202607080002", "wx-tx-1", new Date());

        assertTrue(result);
        assertEquals("1", user.getCreditflag());
        assertEquals(1, userSaves.count);
    }

    @Test
    public void markPaidSkipsUserUpdateForDuplicateCallback() {
        DepositService service = new DepositService();
        SaveCounter userSaves = new SaveCounter();
        ReflectionTestUtils.setField(service, "depositOrderRepository", depositOrderRepository(0, null, new SavedDepositOrder()));
        ReflectionTestUtils.setField(service, "userRepository", userRepository(new User(), userSaves));

        boolean result = service.markPaid("D202607080003", "wx-tx-2", new Date());

        assertFalse(result);
        assertEquals(0, userSaves.count);
    }

    private static DepositConfig config(String testEnabled, int amountFen, int testAmountFen) {
        DepositConfig config = new DepositConfig();
        config.setId(1L);
        config.setEnabled("1");
        config.setAmountFen(amountFen);
        config.setTestEnabled(testEnabled);
        config.setTestAmountFen(testAmountFen);
        config.setDisplayAmountFen(amountFen);
        config.setRefundWaitDays(90);
        config.setEarlyRefundFeeRate(15);
        config.setUpdatedAt(new Date());
        return config;
    }

    private static DepositOrder order(String outTradeNo, Long userId, String channel, int amountFen, String status, Date createdAt) {
        DepositOrder order = new DepositOrder();
        order.setOutTradeNo(outTradeNo);
        order.setUserId(userId);
        order.setChannel(channel);
        order.setAmountFen(amountFen);
        order.setStatus(status);
        order.setCreatedAt(createdAt);
        order.setUpdatedAt(createdAt);
        return order;
    }

    private static DepositConfigRepository depositConfigRepository(final DepositConfig config) {
        return proxy(DepositConfigRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findOne".equals(method.getName())) return config;
                if ("save".equals(method.getName())) return args[0];
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static DepositOrderRepository depositOrderRepository(final int markPaidRows,
                                                                 final DepositOrder recent,
                                                                 final SavedDepositOrder saved) {
        return proxy(DepositOrderRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findByUserIdAndStatusOrderByCreatedAtDesc".equals(method.getName())) {
                    throw new AssertionError("createPendingOrder should use exact reuse query");
                }
                if ("findByUserIdAndStatusAndChannelAndAmountFenAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc".equals(method.getName())) {
                    assertEquals(Long.valueOf(7L), args[0]);
                    assertEquals("PENDING", args[1]);
                    assertEquals("MINIAPP", args[2]);
                    assertEquals(Integer.valueOf(1), args[3]);
                    assertTrue(args[4] instanceof Date);
                    return new PageImpl<DepositOrder>(recent == null ? Collections.<DepositOrder>emptyList() : Collections.singletonList(recent));
                }
                if ("save".equals(method.getName())) {
                    saved.lastSaved = (DepositOrder) args[0];
                    saved.order = saved.lastSaved;
                    return args[0];
                }
                if ("markPaidIfPending".equals(method.getName())) return markPaidRows;
                if ("findByOutTradeNo".equals(method.getName())) return recent;
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static UserRepository userRepository(final User user, final SaveCounter saves) {
        return proxy(UserRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findById".equals(method.getName())) return user;
                if ("save".equals(method.getName())) {
                    saves.count++;
                    return args[0];
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

    private static class SavedDepositOrder {
        DepositOrder order;
        DepositOrder lastSaved;
    }

    private static class SaveCounter {
        int count;
    }
}
