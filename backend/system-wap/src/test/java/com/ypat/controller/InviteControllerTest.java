package com.ypat.controller;

import com.ypat.InviteConfigQo;
import com.ypat.InviteRelationQo;
import com.ypat.InviteSummaryQo;
import com.ypat.model.SecurityUserDetails;
import com.ypat.service.InviteServiceClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class InviteControllerTest {

    private InviteController controller;
    private FakeInviteServiceClient client;

    @Before
    public void setUp() {
        controller = new InviteController();
        client = new FakeInviteServiceClient();
        ReflectionTestUtils.setField(controller, "inviteServiceClient", client);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void myInfoForwardsCurrentUserId() {
        setAuthenticatedUser("42");
        InviteSummaryQo summary = controller.myInfo();
        assertEquals(Long.valueOf(42), client.summaryUserid);
        assertNotNull(summary);
    }

    @Test
    public void myInfoTolerantsAnonymous() {
        // 未登录场景，Controller 不应抛 NPE，应把 null 透传给 service。
        InviteSummaryQo summary = controller.myInfo();
        assertNull(client.summaryUserid);
        assertNotNull(summary);
    }

    @Test
    public void ruleAlwaysReturnsRewardPpd() {
        Map<String, Object> rule = controller.rule();
        assertEquals("1", rule.get("enabled"));
        assertEquals(3, rule.get("rewardPpd"));
        assertEquals("拍拍豆", rule.get("rewardUnit"));
        assertNotNull(rule.get("ruleText"));
        assertEquals("好友邀请你加入爱去拍，找摄影师、找模特更方便", rule.get("shareTitle"));
        assertNotNull(rule.get("landingTitle"));
    }

    @Test
    public void recordsInjectsAuthenticatedInviterId() {
        setAuthenticatedUser("99");
        InviteRelationQo qo = new InviteRelationQo();
        qo.setPage(0);
        qo.setSize(20);
        controller.records(qo);
        assertEquals(Long.valueOf(99), client.lastRecordsQo.getInviterUserid());
        assertEquals(Integer.valueOf(20), client.lastRecordsQo.getSize());
    }

    @Test
    public void recordsForcesEmptyResultForAnonymous() {
        InviteRelationQo qo = new InviteRelationQo();
        Map<String, Object> result = controller.records(qo);
        // 未登录时 Controller 用 -1 兜底，service 返回空分页，不暴露任何关系
        assertEquals(Long.valueOf(-1L), client.lastRecordsQo.getInviterUserid());
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

    private static class FakeInviteServiceClient implements InviteServiceClient {
        Long summaryUserid;
        InviteRelationQo lastRecordsQo;

        @Override
        public InviteSummaryQo summary(Long userid) {
            this.summaryUserid = userid;
            InviteSummaryQo qo = new InviteSummaryQo();
            qo.setInviteCode(userid == null ? null : "IV" + Long.toString(userid, 36).toUpperCase());
            qo.setTotalInvited(0L);
            qo.setTotalReward(0);
            qo.setRewardPpd(3);
            return qo;
        }

        @Override
        public Map<String, Object> findPage(InviteRelationQo qo) {
            this.lastRecordsQo = qo;
            Map<String, Object> page = new HashMap<String, Object>();
            page.put("content", new java.util.ArrayList<Object>());
            page.put("totalElements", 0L);
            page.put("totalPages", 0);
            page.put("number", 0);
            page.put("size", qo.getSize() == null ? 10 : qo.getSize());
            return page;
        }

        @Override
        public InviteConfigQo config() {
            InviteConfigQo qo = new InviteConfigQo();
            qo.setEnabled("1");
            qo.setRewardPpd(3);
            qo.setRewardUnit("拍拍豆");
            qo.setRuleText("好友通过你的邀请码注册后，自动到账 3 拍拍豆。");
            qo.setShareTitle("好友邀请你加入爱去拍，找摄影师、找模特更方便");
            qo.setLandingTitle("我正在使用爱去拍，找摄影师、找模特特别方便，推荐你也来体验。");
            return qo;
        }

        @Override
        public InviteConfigQo saveConfig(InviteConfigQo qo) {
            return qo;
        }

        @Override
        public Map<String, Object> adminFindPage(InviteRelationQo qo) {
            return findPage(qo);
        }
    }
}
