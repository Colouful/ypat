package com.ypat.service;

import com.ypat.entity.Order;
import com.ypat.entity.Product;
import com.ypat.entity.Record;
import com.ypat.entity.User;
import com.ypat.enums.OrderType;
import com.ypat.enums.RecordType;
import com.ypat.enums.YesNo;
import com.ypat.repository.OrderRepository;
import com.ypat.repository.ProductRepository;
import com.ypat.repository.RecordRepository;
import com.ypat.repository.UserRepository;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OrderServicePpdPaymentTest {

    @Test
    public void savePpdPaymentOrderKeepsPreviousPendingOrders() {
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

        service.savePpdPaymentOrder(order());

        assertFalse(capture.deleted);
        assertEquals(YesNo.no.value, capture.savedOrder.getStatus());
    }

    @Test
    public void markPpdPaidCreditsConfiguredAmountAndCreatesRecord() {
        OrderService service = new OrderService();
        Capture capture = new Capture();
        Order order = order();
        Product product = product();
        User user = user();

        ReflectionTestUtils.setField(service, "orderRepository", proxy(OrderRepository.class, (p, method, args) -> {
            if ("findByOut_trade_no".equals(method.getName())) return order;
            if ("save".equals(method.getName())) {
                capture.savedOrder = (Order) args[0];
                return args[0];
            }
            return defaultValue(method.getReturnType());
        }));
        ReflectionTestUtils.setField(service, "productRepository", proxy(ProductRepository.class, (p, method, args) -> {
            if ("findById".equals(method.getName())) return product;
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
        ReflectionTestUtils.setField(service, "recordRepository", proxy(RecordRepository.class, (p, method, args) -> {
            if ("save".equals(method.getName())) {
                capture.savedRecord = (Record) args[0];
                return args[0];
            }
            return defaultValue(method.getReturnType());
        }));

        assertTrue(service.markPpdPaid("PPD202607110001"));
        assertEquals(YesNo.yes.value, capture.savedOrder.getStatus());
        assertEquals(Integer.valueOf(24), capture.savedUser.getPpd());
        assertEquals(Integer.valueOf(19), capture.savedRecord.getPpd());
        assertEquals(RecordType.PAY.value, capture.savedRecord.getType());
    }

    private static Order order() {
        Order order = new Order();
        order.setOut_trade_no("PPD202607110001");
        order.setUserid(7L);
        order.setProductid(3L);
        order.setType(OrderType.PPD.value);
        order.setStatus(YesNo.no.value);
        return order;
    }

    private static Product product() {
        Product product = new Product();
        product.setId(3L);
        product.setCurrval(19);
        return product;
    }

    private static User user() {
        User user = new User();
        user.setId(7L);
        user.setPpd(5);
        return user;
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
        Record savedRecord;
        boolean deleted;
    }
}
