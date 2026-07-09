package com.ypat.controller;

import com.ypat.InviteConfigQo;
import com.ypat.InviteRelationQo;
import com.ypat.InviteSummaryQo;
import com.ypat.ResponseApiBody;
import com.ypat.service.InviteServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AdminInviteControllerTest {
    private AdminInviteController controller;
    private FakeInviteServiceClient client;

    @Before
    public void setUp() {
        controller = new AdminInviteController();
        client = new FakeInviteServiceClient();
        ReflectionTestUtils.setField(controller, "inviteServiceClient", client);
    }

    @Test
    public void configWrapsFeignResult() {
        ResponseApiBody body = controller.config();

        assertNotNull(body);
        assertEquals("1", ((InviteConfigQo) body.getRes()).getEnabled());
    }

    @Test
    public void saveConfigForwardsBody() {
        InviteConfigQo qo = new InviteConfigQo();
        qo.setEnabled("0");
        qo.setRewardPpd(5);
        qo.setRuleText("邀请好友成功后奖励 5 拍拍豆。");
        qo.setShareTitle("邀请你体验爱去拍");
        qo.setLandingTitle("我正在用爱去拍找拍摄伙伴，推荐你也来看看。");

        controller.saveConfig(qo);

        assertEquals("0", client.savedConfig.getEnabled());
        assertEquals(Integer.valueOf(5), client.savedConfig.getRewardPpd());
    }

    @Test
    public void recordsForwardsFilters() {
        InviteRelationQo qo = new InviteRelationQo();
        qo.setInviterUserid(12L);
        qo.setInviteCode("IVC");

        controller.records(qo);

        assertEquals(Long.valueOf(12L), client.lastRecordsQo.getInviterUserid());
        assertEquals("IVC", client.lastRecordsQo.getInviteCode());
    }

    private static class FakeInviteServiceClient implements InviteServiceClient {
        InviteConfigQo savedConfig;
        InviteRelationQo lastRecordsQo;

        @Override
        public InviteSummaryQo summary(Long userid) {
            return new InviteSummaryQo();
        }

        @Override
        public Map<String, Object> findPage(InviteRelationQo qo) {
            this.lastRecordsQo = qo;
            return new HashMap<String, Object>();
        }

        @Override
        public InviteConfigQo config() {
            InviteConfigQo qo = new InviteConfigQo();
            qo.setEnabled("1");
            qo.setRewardPpd(3);
            return qo;
        }

        @Override
        public InviteConfigQo saveConfig(InviteConfigQo qo) {
            this.savedConfig = qo;
            return qo;
        }

        @Override
        public Map<String, Object> adminFindPage(InviteRelationQo qo) {
            this.lastRecordsQo = qo;
            return new HashMap<String, Object>();
        }
    }
}
