package com.ypat.controller;

import com.ypat.OrderQo;
import com.ypat.ProductQo;
import com.ypat.config.SystemConfig;
import com.ypat.enums.OrderType;
import com.ypat.model.SecurityUserDetails;
import com.ypat.service.OrderServiceClient;
import com.ypat.service.ProductServiceClient;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.third.wxpay.sdk.WXPayClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RealnameOrderSecuritySourceTest {

    private OrderController controller;
    private FakeWXPayClient wxPayClient;
    private FakeOrderServiceClient orderServiceClient;
    private FakeProductServiceClient productServiceClient;

    @Before
    public void setUp() {
        controller = new OrderController();
        wxPayClient = new FakeWXPayClient();
        orderServiceClient = new FakeOrderServiceClient();
        productServiceClient = new FakeProductServiceClient();

        ReflectionTestUtils.setField(controller, "wxPayClient", wxPayClient);
        ReflectionTestUtils.setField(controller, "orderServiceClient", orderServiceClient);
        ReflectionTestUtils.setField(controller, "productServiceClient", productServiceClient);
        ReflectionTestUtils.setField(controller, "systemConfig", systemConfig());
        setAuthenticatedUser("42");
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void realnameOrderIgnoresClientAmountAndChargesAuditFee() throws Exception {
        OrderQo order = new OrderQo();
        order.setType(OrderType.REAL.value);
        order.setTotal_fee(1);

        controller.add(order);

        assertEquals(Integer.valueOf(2900), wxPayClient.capturedTotalFee);
        assertEquals(Integer.valueOf(2900), orderServiceClient.savedOrder.getTotal_fee());
        assertEquals(OrderType.REAL.value, orderServiceClient.savedOrder.getType());
        assertEquals(Long.valueOf(42), orderServiceClient.savedOrder.getUserid());
    }

    @Test
    public void ppdOrderStillUsesProductAmount() throws Exception {
        productServiceClient.productOldval = 990;
        OrderQo order = new OrderQo();
        order.setType(OrderType.PPD.value);
        order.setProductid(7L);
        order.setTotal_fee(1);

        controller.add(order);

        assertEquals(Integer.valueOf(990), wxPayClient.capturedTotalFee);
        assertEquals(Integer.valueOf(990), orderServiceClient.savedOrder.getTotal_fee());
        assertEquals(OrderType.PPD.value, orderServiceClient.savedOrder.getType());
        assertEquals(Long.valueOf(7L), orderServiceClient.savedOrder.getProductid());
    }

    private SystemConfig systemConfig() {
        SystemConfig config = new SystemConfig();
        config.setWx_appid("wx-test-appid");
        config.setWx_key("0123456789abcdef0123456789abcdef");
        return config;
    }

    private void setAuthenticatedUser(String userId) {
        SecurityUserDetails details = new SecurityUserDetails();
        details.setUserId(userId);
        details.setUsername("user-" + userId);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private static class FakeWXPayClient extends WXPayClient {
        Integer capturedTotalFee;

        @Override
        public Map<String, String> unifiedOrder(OrderQo orderQo) {
            this.capturedTotalFee = orderQo.getTotal_fee();
            Map<String, String> response = new HashMap<>();
            response.put("return_code", "SUCCESS");
            response.put("result_code", "SUCCESS");
            response.put("prepay_id", "test-prepay-id");
            response.put("out_trade_no", "TEST_OUT_TRADE_NO");
            return response;
        }
    }

    private static class FakeOrderServiceClient implements OrderServiceClient {
        OrderQo savedOrder;

        @Override
        public String get(Long id) {
            return null;
        }

        @Override
        public String count(Long userid, String type) {
            return null;
        }

        @Override
        public String findPage(OrderQo orderQo) {
            return null;
        }

        @Override
        public String add(OrderQo orderQo) {
            this.savedOrder = orderQo;
            return "{}";
        }
    }

    private static class FakeProductServiceClient implements ProductServiceClient {
        Integer productOldval = 990;

        @Override
        public String get(Long id) {
            ProductQo product = new ProductQo();
            product.setId(id);
            product.setOldval(productOldval);
            product.setStatus("0");
            return GsonUtils.toJson(product);
        }

        @Override
        public String findPage(ProductQo productQo) {
            return null;
        }

        @Override
        public void add(ProductQo productQo) {
        }

        @Override
        public void upDown(ProductQo productQo) {
        }
    }
}
