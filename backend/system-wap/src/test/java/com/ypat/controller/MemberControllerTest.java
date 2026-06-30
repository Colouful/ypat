package com.ypat.controller;

import com.ypat.MemberOrderCreateResult;
import com.ypat.MemberOrderQo;
import com.ypat.MemberPlanQo;
import com.ypat.MemberStatusQo;
import com.ypat.SysException;
import com.ypat.config.SystemConfig;
import com.ypat.model.SecurityUserDetails;
import com.ypat.service.MemberServiceClient;
import com.ypat.third.wxpay.sdk.WXPayClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MemberControllerTest {

    private MemberController controller;
    private FakeMemberServiceClient client;
    private FakeWXPayClient wxPayClient;
    private SystemConfig systemConfig;

    @Before
    public void setUp() {
        controller = new MemberController();
        client = new FakeMemberServiceClient();
        wxPayClient = new FakeWXPayClient();
        systemConfig = new SystemConfig();
        ReflectionTestUtils.setField(systemConfig, "wx_appid", "wx-appid");
        ReflectionTestUtils.setField(systemConfig, "wx_key", "wx-test-key");
        ReflectionTestUtils.setField(controller, "memberServiceClient", client);
        ReflectionTestUtils.setField(controller, "wxPayClient", wxPayClient);
        ReflectionTestUtils.setField(controller, "systemConfig", systemConfig);
        setAuthenticatedUser("42");
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void plansReturnsActivePlans() {
        Object result = controller.plans();
        assertNotNull(result);
    }

    @Test
    public void statusForwardsUserId() {
        MemberStatusQo status = controller.status();
        assertEquals(Long.valueOf(42), client.statusUserid);
        assertNotNull(status);
    }

    @Test
    public void createOrderBuildsWxPayParams() {
        wxPayClient.success = true;
        MemberOrderCreateResult result = controller.createOrder(7L);
        assertNotNull(result);
        assertNotNull(result.getOutTradeNo());
        assertEquals("wx-appid", result.getAppId());
        assertEquals("prepay_id=test-prepay-id", result.getPackageValue());
        assertEquals("HMAC-SHA256", result.getSignType());
        assertNotNull(result.getPaySign());
        assertEquals(Long.valueOf(42), client.lastCreateUserId);
        assertEquals(Long.valueOf(7L), client.lastCreatePlanId);
    }

    @Test
    public void createOrderThrowsWhenWxPayFails() {
        wxPayClient.success = false;
        try {
            controller.createOrder(7L);
            fail("expected SysException");
        } catch (SysException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test(expected = SysException.class)
    public void statusThrowsForAnonymous() {
        SecurityContextHolder.clearContext();
        controller.status();
    }

    @Test
    public void ordersForwardsUserId() {
        MemberOrderQo qo = new MemberOrderQo();
        Map<String, Object> result = controller.orders(qo);
        assertEquals(Long.valueOf(42), client.lastListUserId);
        assertNotNull(result);
    }

    private void setAuthenticatedUser(String userId) {
        SecurityUserDetails details = new SecurityUserDetails();
        details.setUserId(userId);
        details.setUsername("user-" + userId);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private static class FakeMemberServiceClient implements MemberServiceClient {
        Long statusUserid;
        Long lastCreateUserId;
        Long lastCreatePlanId;
        Long lastListUserId;

        @Override
        public List<MemberPlanQo> plans() {
            MemberPlanQo p = new MemberPlanQo();
            p.setId(1L);
            p.setCode("MONTH");
            p.setName("包月");
            p.setDurationDays(30);
            p.setPriceFen(9900);
            return java.util.Collections.singletonList(p);
        }

        @Override
        public MemberPlanQo plan(Long planId) {
            return null;
        }

        @Override
        public MemberStatusQo status(Long userId) {
            this.statusUserid = userId;
            MemberStatusQo qo = new MemberStatusQo();
            qo.setLevel("NONE");
            qo.setActive(false);
            return qo;
        }

        @Override
        public MemberOrderQo createOrder(Long userId, Long planId) {
            this.lastCreateUserId = userId;
            this.lastCreatePlanId = planId;
            MemberOrderQo qo = new MemberOrderQo();
            qo.setOutTradeNo("M2024010101010100142ABCDEF");
            qo.setPriceFen(9900);
            qo.setPlanCode("MONTH");
            qo.setDurationDays(30);
            qo.setUserId(userId);
            qo.setPlanId(planId);
            qo.setStatus("0");
            return qo;
        }

        @Override
        public Boolean cancelOrder(String outTradeNo) {
            return true;
        }

        @Override
        public MemberOrderQo getOrder(String outTradeNo, Long userId) {
            MemberOrderQo qo = new MemberOrderQo();
            qo.setOutTradeNo(outTradeNo);
            qo.setStatus("1");
            qo.setUserId(userId);
            qo.setPriceFen(9900);
            return qo;
        }

        @Override
        public Map<String, Object> findOrders(MemberOrderQo qo) {
            this.lastListUserId = qo.getUserId();
            Map<String, Object> page = new HashMap<>();
            page.put("content", java.util.Collections.emptyList());
            page.put("totalElements", 0L);
            page.put("totalPages", 0);
            page.put("number", 0);
            page.put("size", 10);
            return page;
        }

        @Override
        public Boolean markPaid(String outTradeNo, String wxTransactionId, Long paidAtMs) {
            return true;
        }
    }

    private static class FakeWXPayClient extends WXPayClient {
        boolean success;

        @Override
        public Map<String, String> unifiedOrder(Map<String, String> data) throws Exception {
            Map<String, String> resp = new HashMap<>();
            resp.put("return_code", success ? "SUCCESS" : "FAIL");
            resp.put("result_code", success ? "SUCCESS" : "FAIL");
            resp.put("prepay_id", success ? "test-prepay-id" : "");
            return resp;
        }
    }
}