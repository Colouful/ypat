package com.ypat.service;

import com.ypat.entity.Bill;
import com.ypat.entity.Order;
import com.ypat.entity.Product;
import com.ypat.entity.Record;
import com.ypat.entity.User;
import com.ypat.enums.OrderType;
import com.ypat.enums.RecordType;
import com.ypat.enums.YesNo;
import com.ypat.repository.BillRepository;
import com.ypat.repository.OrderRepository;
import com.ypat.repository.ProductRepository;
import com.ypat.repository.RecordRepository;
import com.ypat.repository.UserRepository;
import com.ypat.util.Constant;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;

public class BillServicePpdRechargeTest {

    @Test
    public void ppdRechargeAddsConfiguredBeanAmountAndRecordsSameAmount() {
        BillService service = new BillService();
        Capture capture = new Capture();

        Order order = new Order();
        order.setOut_trade_no("PPD202607090001");
        order.setUserid(7L);
        order.setProductid(3L);
        order.setType(OrderType.PPD.value);
        order.setStatus(YesNo.no.value);
        order.setTotal_fee(990);

        Product product = new Product();
        product.setId(3L);
        product.setCurrval(19);
        product.setOldval(990);

        User user = new User();
        user.setId(7L);
        user.setPpd(5);

        ReflectionTestUtils.setField(service, "orderRepository", proxy(OrderRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findByOut_trade_no".equals(method.getName())) return order;
                if ("save".equals(method.getName())) {
                    capture.savedOrder = (Order) args[0];
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        }));
        ReflectionTestUtils.setField(service, "productRepository", proxy(ProductRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findById".equals(method.getName())) return product;
                return defaultValue(method.getReturnType());
            }
        }));
        ReflectionTestUtils.setField(service, "userRepository", proxy(UserRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findById".equals(method.getName())) return user;
                if ("save".equals(method.getName())) {
                    capture.savedUser = (User) args[0];
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        }));
        ReflectionTestUtils.setField(service, "billRepository", proxy(BillRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("save".equals(method.getName())) {
                    capture.savedBill = (Bill) args[0];
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        }));
        ReflectionTestUtils.setField(service, "recordRepository", proxy(RecordRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("save".equals(method.getName())) {
                    capture.savedRecord = (Record) args[0];
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        }));

        Bill bill = new Bill();
        bill.setOut_trade_no("PPD202607090001");
        bill.setTotal_fee(99000);
        bill.setResult_code(Constant.SUCCESS);

        service.save(bill);

        assertEquals(Integer.valueOf(24), capture.savedUser.getPpd());
        assertEquals(Integer.valueOf(19), capture.savedRecord.getPpd());
        assertEquals(RecordType.PAY.value, capture.savedRecord.getType());
        assertEquals(YesNo.yes.value, capture.savedOrder.getStatus());
        assertEquals(OrderType.PPD.value, capture.savedBill.getType());
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

    private static class Capture {
        Bill savedBill;
        Order savedOrder;
        Record savedRecord;
        User savedUser;
    }
}
