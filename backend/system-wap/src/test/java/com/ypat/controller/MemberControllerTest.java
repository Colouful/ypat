package com.ypat.controller;

import com.ypat.MemberBenefitQuoteQo;
import com.ypat.MemberBenefitConfigQo;
import com.ypat.MemberBenefitRuleQo;
import com.ypat.MemberOperationLogQo;
import com.ypat.MemberOrderQo;
import com.ypat.MemberPlanQo;
import com.ypat.MemberStatusQo;
import com.ypat.MemberUserAdminQo;
import com.ypat.PaymentCreateResult;
import com.ypat.PaymentOrderQo;
import com.ypat.PaymentPayParams;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.UserQo;
import com.ypat.enums.PaymentBusinessType;
import com.ypat.enums.PaymentChannel;
import com.ypat.model.SecurityUserDetails;
import com.ypat.payment.WechatPaymentService;
import com.ypat.service.MemberServiceClient;
import com.ypat.service.PaymentOrderServiceClient;
import com.ypat.service.UserServiceClient;
import com.ypat.third.baidu.ai.GsonUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
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
    private FakePaymentOrderServiceClient paymentOrderClient;
    private FakeUserServiceClient userClient;
    private FakeWechatPaymentService paymentService;

    @Before
    public void setUp() {
        controller = new MemberController();
        client = new FakeMemberServiceClient();
        paymentOrderClient = new FakePaymentOrderServiceClient();
        userClient = new FakeUserServiceClient();
        paymentService = new FakeWechatPaymentService();
        ReflectionTestUtils.setField(controller, "memberServiceClient", client);
        ReflectionTestUtils.setField(controller, "paymentOrderServiceClient", paymentOrderClient);
        ReflectionTestUtils.setField(controller, "userServiceClient", userClient);
        ReflectionTestUtils.setField(controller, "wechatPaymentService", paymentService);
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
    public void plansReturnsActivePlansWithoutAuthentication() {
        SecurityContextHolder.clearContext();
        List<MemberPlanQo> result = controller.plans();
        assertEquals(1, result.size());
    }

    @Test
    public void statusForwardsUserId() {
        MemberStatusQo status = controller.status();
        assertEquals(Long.valueOf(42), client.statusUserid);
        assertNotNull(status);
    }

    @Test
    public void quoteForwardsUserId() {
        MemberBenefitQuoteQo quote = controller.quote("SUBMIT_YPAT");
        assertEquals(Long.valueOf(42), client.quoteUserId);
        assertEquals("SUBMIT_YPAT", client.quoteScene);
        assertEquals(Integer.valueOf(3), quote.getActualPpd());
    }

    @Test
    public void quoteAllowsEverySupportedScene() {
        controller.quote("SUBMIT_YPAT");
        controller.quote("APPLY_YPAT");
        controller.quote("VIEW_CONTACT");
        assertEquals("VIEW_CONTACT", client.quoteScene);
    }

    @Test(expected = SysException.class)
    public void quoteRejectsUnknownSceneBeforeCallingService() {
        controller.quote("UNKNOWN");
    }

    @Test
    public void createOrderBuildsWxPayParams() {
        PaymentCreateResult result = controller.createOrder(7L, PaymentChannel.MINIAPP.value, request());
        assertNotNull(result);
        assertNotNull(result.getOutTradeNo());
        assertEquals(PaymentBusinessType.MEMBER.value, result.getBusinessType());
        assertEquals(PaymentChannel.MINIAPP.value, result.getChannel());
        assertEquals(Integer.valueOf(9900), result.getAmountFen());
        assertEquals("prepay_id=test-prepay-id", result.getPayParams().getPackageValue());
        assertEquals("RSA", result.getPayParams().getSignType());
        assertEquals("openid-42", paymentService.lastCommand.getOpenid());
        assertEquals("127.0.0.1", paymentService.lastCommand.getClientIp());
        assertEquals(Long.valueOf(42), client.lastCreateUserId);
        assertEquals(Long.valueOf(7L), client.lastCreatePlanId);
        assertEquals("M2024010101010100142ABCDEF", paymentOrderClient.createdOutTradeNo);
        assertEquals("test-prepay-id", paymentOrderClient.preparedPrepayId);
        assertEquals("test-prepay-id", client.lastPreparedPrepayId);
    }

    @Test
    public void createOrderBuildsH5PaymentUrl() {
        PaymentCreateResult result = controller.createOrder(7L, PaymentChannel.H5.value, request());
        assertNotNull(result);
        assertEquals(PaymentChannel.H5.value, result.getChannel());
        assertEquals("https://wx.example.test/h5", result.getH5Url());
        assertEquals(null, paymentService.lastCommand.getOpenid());
        assertEquals("https://wx.example.test/h5", paymentOrderClient.preparedH5Url);
    }

    @Test
    public void createOrderThrowsWhenWxPayFails() {
        paymentService.fail = true;
        try {
            controller.createOrder(7L, PaymentChannel.MINIAPP.value, request());
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

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        return request;
    }

    private static class FakeMemberServiceClient implements MemberServiceClient {
        Long statusUserid;
        Long quoteUserId;
        String quoteScene;
        Long lastCreateUserId;
        Long lastCreatePlanId;
        Long lastListUserId;
        String lastPreparedPrepayId;

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
        public MemberBenefitQuoteQo quote(Long userId, String scene) {
            this.quoteUserId = userId;
            this.quoteScene = scene;
            MemberBenefitQuoteQo qo = new MemberBenefitQuoteQo();
            qo.setScene(scene);
            qo.setOriginalPpd(5);
            qo.setDiscountPpd(2);
            qo.setActualPpd(3);
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
        public Map<String, Object> adminPlans(MemberPlanQo qo) {
            return new HashMap<>();
        }

        @Override
        public MemberPlanQo savePlan(MemberPlanQo qo) {
            return qo;
        }

        @Override
        public Map<String, Object> adminRules(MemberBenefitRuleQo qo) {
            return new HashMap<>();
        }

        @Override
        public MemberBenefitRuleQo saveRule(MemberBenefitRuleQo qo) {
            return qo;
        }

        @Override
        public List<MemberBenefitConfigQo> adminBenefitConfigs() {
            return java.util.Collections.emptyList();
        }

        @Override
        public MemberBenefitConfigQo saveBenefitConfig(MemberBenefitConfigQo qo) {
            return qo;
        }

        @Override
        public Map<String, Object> adminUsers(MemberUserAdminQo qo) {
            return new HashMap<>();
        }

        @Override
        public Boolean adminGrant(Long userId, Integer days, Long operatorId, String reason) {
            return true;
        }

        @Override
        public Boolean adminExtend(Long userId, Integer days, Long operatorId, String reason) {
            return true;
        }

        @Override
        public Boolean adminCancel(Long userId, Long operatorId, String reason) {
            return true;
        }

        @Override
        public Map<String, Object> adminOrders(MemberOrderQo qo) {
            return new HashMap<>();
        }

        @Override
        public Map<String, Object> adminLogs(MemberOperationLogQo qo) {
            return new HashMap<>();
        }

        @Override
        public Boolean markPaid(String outTradeNo, String wxTransactionId, Long paidAtMs) {
            return true;
        }

        @Override
        public MemberOrderQo prepared(String outTradeNo, String channel, String prepayId) {
            this.lastPreparedPrepayId = prepayId;
            return getOrder(outTradeNo, 42L);
        }
    }

    private static class FakePaymentOrderServiceClient implements PaymentOrderServiceClient {
        String createdOutTradeNo;
        String preparedPrepayId;
        String preparedH5Url;

        @Override
        public PaymentOrderQo createPending(String businessType, String businessOrderNo, String outTradeNo,
                                            Long userId, String channel, Integer amountFen) {
            this.createdOutTradeNo = outTradeNo;
            PaymentOrderQo qo = new PaymentOrderQo();
            qo.setBusinessType(businessType);
            qo.setBusinessOrderNo(businessOrderNo);
            qo.setOutTradeNo(outTradeNo);
            qo.setUserId(userId);
            qo.setChannel(channel);
            qo.setAmountFen(amountFen);
            return qo;
        }

        @Override
        public PaymentOrderQo get(String outTradeNo) {
            return null;
        }

        @Override
        public PaymentOrderQo prepared(String outTradeNo, String channel, String prepayId, String h5Url) {
            this.preparedPrepayId = prepayId;
            this.preparedH5Url = h5Url;
            PaymentOrderQo qo = new PaymentOrderQo();
            qo.setOutTradeNo(outTradeNo);
            qo.setChannel(channel);
            qo.setPrepayId(prepayId);
            qo.setH5Url(h5Url);
            return qo;
        }

        @Override
        public Boolean markPaid(String outTradeNo, String txId, Integer amountFen,
                                Long paidAtMs, String eventId, String digest) {
            return true;
        }

        @Override
        public Map<String, Object> adminOrders(PaymentOrderQo qo) {
            return new HashMap<>();
        }
    }

    private static class FakeUserServiceClient implements UserServiceClient {
        @Override
        public String get(Long id) {
            UserQo user = new UserQo();
            user.setId(id);
            user.setOpenid("openid-" + id);
            return GsonUtils.toJson(user);
        }

        @Override
        public String findByMobile(String mobile) { return null; }

        @Override
        public String linkway(Long id, Long userid, Long messid) { return null; }

        @Override
        public String add(UserQo userQo) { return null; }

        @Override
        public String upd(UserQo userQo) { return null; }

        @Override
        public String findPage(UserQo userQo) { return null; }

        @Override
        public String myRecAdd(com.ypat.MessInfoQo messInfoQo) { return null; }

        @Override
        public String myScAdd(Long userid, Long ypatid) { return null; }

        @Override
        public String myScCancel(Long userid, Long ypatid) { return null; }

        @Override
        public String findByCityAndProfess(Long userid, String city) { return null; }
    }

    private static class FakeWechatPaymentService extends WechatPaymentService {
        boolean fail;
        WechatPaymentCommand lastCommand;

        FakeWechatPaymentService() {
            super(null);
        }

        @Override
        public PaymentCreateResult create(WechatPaymentCommand command) {
            this.lastCommand = command;
            if (fail) throw new SysException(ResponseCode.FAIL_PAY);

            PaymentCreateResult result = new PaymentCreateResult();
            result.setOutTradeNo(command.getOutTradeNo());
            result.setBusinessType(command.getBusinessType());
            result.setChannel(command.getChannel());
            result.setAmountFen(command.getAmountFen());
            if (PaymentChannel.MINIAPP.value.equals(command.getChannel())) {
                PaymentPayParams params = new PaymentPayParams();
                params.setTimeStamp("1783499261");
                params.setNonceStr("nonce");
                params.setPackageValue("prepay_id=test-prepay-id");
                params.setSignType("RSA");
                params.setPaySign("pay-sign");
                result.setPayParams(params);
            } else {
                result.setH5Url("https://wx.example.test/h5");
            }
            return result;
        }
    }
}
