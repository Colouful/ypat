package com.ypat.controller;

import com.ypat.MemberBenefitRuleQo;
import com.ypat.MemberBenefitConfigQo;
import com.ypat.MemberOperationLogQo;
import com.ypat.MemberOrderQo;
import com.ypat.MemberPlanQo;
import com.ypat.MemberStatusQo;
import com.ypat.MemberBenefitQuoteQo;
import com.ypat.MemberUserAdminQo;
import com.ypat.ResponseApiBody;
import com.ypat.SysException;
import com.ypat.service.MemberServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AdminMemberControllerTest {

    private AdminMemberController controller;
    private FakeMemberServiceClient client;

    @Before
    public void setUp() {
        controller = new AdminMemberController();
        client = new FakeMemberServiceClient();
        ReflectionTestUtils.setField(controller, "memberServiceClient", client);
    }

    @Test(expected = SysException.class)
    public void grantRequiresReason() {
        MemberUserAdminQo qo = new MemberUserAdminQo();
        qo.setDays(7);
        qo.setReason("");
        controller.grant(2L, qo);
    }

    @Test
    public void savePlanForwardsPayload() {
        MemberPlanQo qo = new MemberPlanQo();
        qo.setName("月卡");
        qo.setPriceFen(4800);

        ResponseApiBody response = controller.savePlan(qo);

        MemberPlanQo result = (MemberPlanQo) response.getRes();
        assertEquals("月卡", client.lastSavedPlan.getName());
        assertEquals(Integer.valueOf(4800), result.getPriceFen());
    }

    @Test
    public void listsAggregatedBenefitConfigs() {
        ResponseApiBody response = controller.benefitConfigs();
        assertEquals(1, ((List<?>) response.getRes()).size());
    }

    @Test
    public void updateBenefitConfigUsesPathScene() {
        MemberBenefitConfigQo qo = new MemberBenefitConfigQo();
        qo.setScene("SUBMIT_YPAT");

        ResponseApiBody response = controller.updateBenefitConfig("APPLY_YPAT", qo);

        assertEquals("APPLY_YPAT", client.lastSavedConfig.getScene());
        assertEquals(qo, response.getRes());
    }

    private static class FakeMemberServiceClient implements MemberServiceClient {
        MemberPlanQo lastSavedPlan;
        MemberBenefitConfigQo lastSavedConfig;

        @Override
        public List<MemberPlanQo> plans() {
            return java.util.Collections.emptyList();
        }

        @Override
        public MemberPlanQo plan(Long planId) {
            return null;
        }

        @Override
        public MemberStatusQo status(Long userId) {
            return null;
        }

        @Override
        public MemberBenefitQuoteQo quote(Long userId, String scene) {
            return null;
        }

        @Override
        public MemberOrderQo createOrder(Long userId, Long planId) {
            return null;
        }

        @Override
        public Boolean cancelOrder(String outTradeNo) {
            return true;
        }

        @Override
        public MemberOrderQo getOrder(String outTradeNo, Long userId) {
            return null;
        }

        @Override
        public Map<String, Object> findOrders(MemberOrderQo qo) {
            return new HashMap<>();
        }

        @Override
        public Map<String, Object> adminPlans(MemberPlanQo qo) {
            return new HashMap<>();
        }

        @Override
        public MemberPlanQo savePlan(MemberPlanQo qo) {
            this.lastSavedPlan = qo;
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
            MemberBenefitConfigQo qo = new MemberBenefitConfigQo();
            qo.setScene("SUBMIT_YPAT");
            return java.util.Collections.singletonList(qo);
        }

        @Override
        public MemberBenefitConfigQo saveBenefitConfig(MemberBenefitConfigQo qo) {
            this.lastSavedConfig = qo;
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
            return null;
        }
    }
}
