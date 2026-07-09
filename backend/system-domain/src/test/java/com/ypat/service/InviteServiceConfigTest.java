package com.ypat.service;

import com.ypat.InviteConfigQo;
import com.ypat.SysException;
import com.ypat.entity.InviteConfig;
import com.ypat.repository.InviteConfigRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InviteServiceConfigTest {
    private InviteService inviteService;
    private List<InviteConfig> configs;

    @Before
    public void setUp() {
        inviteService = new InviteService();
        configs = new ArrayList<InviteConfig>();
        ReflectionTestUtils.setField(inviteService, "inviteConfigRepository", inviteConfigRepository(configs));
    }

    @Test
    public void getConfigReturnsSafeDefaultsWhenNoRowExists() {
        InviteConfigQo config = inviteService.getConfig();

        assertEquals("1", config.getEnabled());
        assertEquals(Integer.valueOf(3), config.getRewardPpd());
        assertEquals("拍拍豆", config.getRewardUnit());
        assertEquals("好友通过你的邀请码注册后，自动到账 3 拍拍豆。", config.getRuleText());
        assertEquals("好友邀请你加入爱去拍，找摄影师、找模特更方便", config.getShareTitle());
        assertEquals("我正在使用爱去拍，找摄影师、找模特特别方便，推荐你也来体验。", config.getLandingTitle());
    }

    @Test
    public void saveConfigPersistsValidatedConfig() {
        InviteConfigQo input = new InviteConfigQo();
        input.setEnabled("0");
        input.setRewardPpd(8);
        input.setRuleText("邀请好友成功后，奖励 8 拍拍豆。");
        input.setShareTitle("邀请你体验爱去拍");
        input.setLandingTitle("我正在用爱去拍找拍摄伙伴，推荐你也来看看。");

        InviteConfigQo saved = inviteService.saveConfig(input);

        assertEquals("0", saved.getEnabled());
        assertEquals(Integer.valueOf(8), saved.getRewardPpd());
        assertEquals(1, configs.size());
        assertEquals("邀请你体验爱去拍", configs.get(0).getShareTitle());
    }

    @Test(expected = SysException.class)
    public void saveConfigRejectsNegativeReward() {
        InviteConfigQo input = new InviteConfigQo();
        input.setEnabled("1");
        input.setRewardPpd(-1);
        input.setRuleText("邀请好友成功后奖励拍拍豆。");
        input.setShareTitle("邀请你体验爱去拍");
        input.setLandingTitle("我正在用爱去拍找拍摄伙伴，推荐你也来看看。");

        inviteService.saveConfig(input);
    }

    private static InviteConfigRepository inviteConfigRepository(List<InviteConfig> configs) {
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("toString".equals(method.getName())) return "InviteConfigRepositoryProxy";
                if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                if ("equals".equals(method.getName())) return proxy == args[0];
                if ("findAll".equals(method.getName())) return configs;
                if ("save".equals(method.getName())) {
                    InviteConfig config = (InviteConfig) args[0];
                    if (configs.isEmpty()) configs.add(config);
                    else configs.set(0, config);
                    return config;
                }
                throw new UnsupportedOperationException(method.getName());
            }
        };
        return (InviteConfigRepository) Proxy.newProxyInstance(
                InviteConfigRepository.class.getClassLoader(),
                new Class[]{InviteConfigRepository.class},
                handler
        );
    }
}
