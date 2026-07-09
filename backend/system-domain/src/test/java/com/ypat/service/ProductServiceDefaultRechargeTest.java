package com.ypat.service;

import com.ypat.entity.Product;
import com.ypat.repository.ProductRepository;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProductServiceDefaultRechargeTest {

    @Test
    public void initializesDefaultPpdRechargeProductsWhenProductTableIsEmpty() {
        ProductService service = new ProductService();
        List<Product> saved = new ArrayList<>();

        ReflectionTestUtils.setField(service, "productRepository", proxy(ProductRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("count".equals(method.getName())) return 0L;
                if ("save".equals(method.getName())) {
                    saved.add((Product) args[0]);
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        }));

        service.ensureDefaultPpdRechargeProducts();

        assertEquals(4, saved.size());
        assertEquals("10拍豆", saved.get(0).getName());
        assertEquals(Integer.valueOf(10), saved.get(0).getCurrval());
        assertEquals(Integer.valueOf(990), saved.get(0).getOldval());
        assertEquals("0", saved.get(0).getStatus());
        assertEquals("0", saved.get(0).getRecommended());
        assertEquals("30拍豆", saved.get(1).getName());
        assertEquals("1", saved.get(1).getRecommended());
        assertEquals("60拍豆", saved.get(2).getName());
        assertEquals("100拍豆", saved.get(3).getName());
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
}
