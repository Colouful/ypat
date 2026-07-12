package com.ypat.service;

import com.ypat.entity.Order;
import com.ypat.entity.User;
import com.ypat.enums.OrderType;
import com.ypat.enums.UserStatus;
import com.ypat.enums.YesNo;
import com.ypat.repository.OrderRepository;
import com.ypat.repository.UserRepository;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OrderServiceRealnamePaymentTest {

    @Test
    public void saveRealnamePaymentOrderKeepsPreviousPendingOrders() {
        OrderService service = new OrderService();
        Capture capture = new Capture();
        ReflectionTestUtils.setField(service, "orderRepository", proxy(OrderRepository.class, (p, method, args) -> {
            if ("delete".equals(method.getName())) capture.deleted = true;
            if ("save".equals(method.getName())) {
                capture.savedOrder = (Order) args[0];
                return args[0];
            }
            return defaultValue(method.getReturnType());
        }));

        service.saveRealnamePaymentOrder(order());

        assertFalse(capture.deleted);
        assertEquals(YesNo.no.value, capture.savedOrder.getStatus());
    }

    @Test
    public void markRealnamePaidUnlocksOauthSubmission() {
        OrderService service = new OrderService();
        Capture capture = new Capture();
        Order order = order();
        User user = new User();
        user.setId(7L);
        user.setStatus(UserStatus.zc.value);

        ReflectionTestUtils.setField(service, "orderRepository", proxy(OrderRepository.class, (p, method, args) -> {
            if ("findByOut_trade_no".equals(method.getName())) return order;
            if ("save".equals(method.getName())) {
                capture.savedOrder = (Order) args[0];
                return args[0];
            }
            return defaultValue(method.getReturnType());
        }));
        ReflectionTestUtils.setField(service, "userRepository", proxy(UserRepository.class, (p, method, args) -> {
            if ("findByIdForUpdate".equals(method.getName())) return user;
            if ("save".equals(method.getName())) {
                capture.savedUser = (User) args[0];
                return args[0];
            }
            return defaultValue(method.getReturnType());
        }));

        assertTrue(service.markRealnamePaid("R202607110001"));
        assertEquals(YesNo.yes.value, capture.savedOrder.getStatus());
        assertEquals("SUCCESS", capture.savedOrder.getResult_code());
        assertEquals(UserStatus.zfcg.value, capture.savedUser.getStatus());
    }

    private static Order order() {
        Order order = new Order();
        order.setOut_trade_no("R202607110001");
        order.setUserid(7L);
        order.setType(OrderType.REAL.value);
        order.setStatus(YesNo.no.value);
        order.setTotal_fee(2900);
        return order;
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            if (Object.class.equals(method.getDeclaringClass())) {
                if ("toString".equals(method.getName())) return type.getSimpleName() + "Proxy";
                if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                if ("equals".equals(method.getName())) return proxy == args[0];
            }
            return handler.invoke(proxy, method, args);
        }));
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (boolean.class.equals(type)) return false;
        if (int.class.equals(type)) return 0;
        if (long.class.equals(type)) return 0L;
        return null;
    }

    private static class Capture {
        Order savedOrder;
        User savedUser;
        boolean deleted;
    }
}
