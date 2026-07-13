package com.ypat.service;

import com.ypat.MemberBenefitQuoteQo;
import com.ypat.MemberBenefitConfigQo;
import com.ypat.MemberBenefitRuleQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.MemberBenefitRule;
import com.ypat.entity.MemberOrder;
import com.ypat.entity.PpdSceneConfig;
import com.ypat.entity.Record;
import com.ypat.entity.User;
import com.ypat.entity.UserMember;
import com.ypat.repository.MemberBenefitRuleRepository;
import com.ypat.repository.MemberOperationLogRepository;
import com.ypat.repository.MemberOrderRepository;
import com.ypat.repository.PpdSceneConfigRepository;
import com.ypat.repository.RecordRepository;
import com.ypat.repository.UserMemberRepository;
import com.ypat.repository.UserRepository;
import com.ypat.util.Constant;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MemberServiceBenefitTest {

    @Test
    public void quoteSubmitYpatUsesConfiguredDiscount() {
        MemberService service = new MemberService();
        UserMemberRepository userMembers = userMemberRepository(activeMember(), null);
        MemberBenefitRuleRepository rules = benefitRuleRepository(submitRule(2, 0, "1", "1"));
        ReflectionTestUtils.setField(service, "userMemberRepository", userMembers);
        ReflectionTestUtils.setField(service, "memberBenefitRuleRepository", rules);
        ReflectionTestUtils.setField(service, "ppdSceneConfigRepository", ppdSceneConfigRepository(null));

        MemberBenefitQuoteQo quote = service.quoteBenefit(1L, "SUBMIT_YPAT");

        assertEquals(Integer.valueOf(Constant.PUB_NEED_PPD), quote.getOriginalPpd());
        assertEquals(Integer.valueOf(2), quote.getDiscountPpd());
        assertEquals(Integer.valueOf(Math.max(0, Constant.PUB_NEED_PPD - 2)), quote.getActualPpd());
        assertTrue(quote.getMemberActive());
    }

    @Test
    public void quoteApplyYpatUsesConfiguredOriginalPriceAndChineseNames() {
        MemberService service = new MemberService();
        String[] queriedScene = new String[1];
        ReflectionTestUtils.setField(service, "userMemberRepository", userMemberRepository(activeMember(), null));
        ReflectionTestUtils.setField(service, "memberBenefitRuleRepository",
                benefitRuleRepository(rule("APPLY_YPAT", 2, 0, "1", "1"), queriedScene));
        ReflectionTestUtils.setField(service, "ppdSceneConfigRepository",
                ppdSceneConfigRepository(sceneConfig("APPLY_YPAT", 7, 3L)));

        MemberBenefitQuoteQo quote = service.quoteBenefit(1L, "APPLY_YPAT");

        assertEquals("APPLY_YPAT", queriedScene[0]);
        assertEquals("发起约拍申请", quote.getSceneName());
        assertEquals("基础会员", quote.getLevelName());
        assertEquals(Integer.valueOf(7), quote.getOriginalPpd());
        assertEquals(Integer.valueOf(2), quote.getDiscountPpd());
        assertEquals(Integer.valueOf(5), quote.getActualPpd());
    }

    @Test
    public void quoteViewContactUsesConfiguredPriceWhenRuleDisabled() {
        MemberService service = new MemberService();
        ReflectionTestUtils.setField(service, "userMemberRepository", userMemberRepository(activeMember(), null));
        ReflectionTestUtils.setField(service, "memberBenefitRuleRepository",
                benefitRuleRepository(rule("VIEW_CONTACT", 2, 0, "1", "0")));
        ReflectionTestUtils.setField(service, "ppdSceneConfigRepository",
                ppdSceneConfigRepository(sceneConfig("VIEW_CONTACT", 7, 4L)));

        MemberBenefitQuoteQo quote = service.quoteBenefit(1L, "VIEW_CONTACT");

        assertEquals(Integer.valueOf(0), quote.getDiscountPpd());
        assertEquals(Integer.valueOf(7), quote.getActualPpd());
    }

    @Test
    public void quoteUnknownSceneIsRejected() {
        MemberService service = new MemberService();
        try {
            service.quoteBenefit(1L, "UNKNOWN");
        } catch (SysException ex) {
            assertEquals(ResponseCode.FAIL_PARA.getCode(), ex.getCode());
            return;
        }
        throw new AssertionError("未知场景应被拒绝");
    }

    @Test
    public void listsBenefitConfigsWithChineseRuleNames() {
        MemberService service = new MemberService();
        PpdSceneConfig apply = sceneConfig("APPLY_YPAT", 7, 3L);
        ReflectionTestUtils.setField(service, "ppdSceneConfigRepository", ppdSceneConfigRepository(apply));
        ReflectionTestUtils.setField(service, "memberBenefitRuleRepository",
                benefitRuleRepository(rule("APPLY_YPAT", 2, 0, "1", "1")));

        List<MemberBenefitConfigQo> configs = service.listBenefitConfigs();

        assertEquals(3, configs.size());
        MemberBenefitConfigQo applyConfig = configs.get(1);
        assertEquals("发起约拍申请", applyConfig.getSceneName());
        assertEquals(Integer.valueOf(7), applyConfig.getOriginalPpd());
        assertEquals("基础会员", applyConfig.getRules().get(0).getLevelName());
        assertEquals("拍豆减免", applyConfig.getRules().get(0).getBenefitTypeName());
    }

    @Test
    public void savesAggregatedBenefitConfigWithMatchingVersion() {
        MemberService service = new MemberService();
        SaveCounter operationLogSaves = new SaveCounter();
        PpdSceneConfig existing = sceneConfig("APPLY_YPAT", 3, 3L);
        MemberBenefitRule existingRule = rule("APPLY_YPAT", 0, 0, "1", "1");
        ReflectionTestUtils.setField(service, "ppdSceneConfigRepository", ppdSceneConfigRepository(existing));
        ReflectionTestUtils.setField(service, "memberBenefitRuleRepository", benefitRuleRepository(existingRule));
        ReflectionTestUtils.setField(service, "memberOperationLogRepository",
                operationLogRepository(operationLogSaves));
        MemberBenefitConfigQo input = new MemberBenefitConfigQo();
        input.setScene("APPLY_YPAT");
        input.setOriginalPpd(7);
        input.setDescription("申请定价");
        input.setVersion(3L);
        input.setOperatorId(9L);
        MemberBenefitRuleQo ruleInput = new MemberBenefitRuleQo();
        ruleInput.setLevelCode("BASIC");
        ruleInput.setScene("APPLY_YPAT");
        ruleInput.setBenefitType("PPD_DISCOUNT");
        ruleInput.setDiscountPpd(2);
        ruleInput.setMinActualPpd(1);
        ruleInput.setEffective("1");
        ruleInput.setStatus("1");
        input.setRules(Collections.singletonList(ruleInput));

        MemberBenefitConfigQo saved = service.saveBenefitConfig(input);

        assertEquals(Integer.valueOf(7), existing.getOriginalPpd());
        assertEquals(Integer.valueOf(2), existingRule.getDiscountPpd());
        assertEquals(Integer.valueOf(1), existingRule.getMinActualPpd());
        assertEquals("发起约拍申请", saved.getSceneName());
        assertEquals(1, operationLogSaves.count);
    }

    @Test
    public void markPaidDoesNotGrantTwiceWhenRepositoryUpdateReturnsZero() {
        MemberService service = new MemberService();
        SaveCounter userMemberSaves = new SaveCounter();
        SaveCounter recordSaves = new SaveCounter();
        SaveCounter logSaves = new SaveCounter();
        MemberOrderRepository orders = memberOrderRepository(0, null);
        UserMemberRepository userMembers = userMemberRepository(null, userMemberSaves);
        RecordRepository records = recordRepository(recordSaves);
        MemberOperationLogRepository logs = operationLogRepository(logSaves);
        ReflectionTestUtils.setField(service, "memberOrderRepository", orders);
        ReflectionTestUtils.setField(service, "userMemberRepository", userMembers);
        ReflectionTestUtils.setField(service, "recordRepository", records);
        ReflectionTestUtils.setField(service, "memberOperationLogRepository", logs);

        boolean result = service.markPaid("M202607050001", "wx-tx", new Date());

        assertFalse(result);
        assertEquals(0, userMemberSaves.count);
        assertEquals(0, recordSaves.count);
        assertEquals(0, logSaves.count);
    }

    @Test
    public void markPaidGrantsGiftPpdAndLogsWhenFirstPaid() {
        MemberService service = new MemberService();
        SaveCounter userMemberSaves = new SaveCounter();
        SaveCounter userSaves = new SaveCounter();
        SaveCounter recordSaves = new SaveCounter();
        SaveCounter logSaves = new SaveCounter();
        MemberOrder order = paidOrder(5);
        User user = new User();
        user.setId(1L);
        user.setPpd(10);
        MemberOrderRepository orders = memberOrderRepository(1, order);
        UserMemberRepository userMembers = userMemberRepository(null, userMemberSaves);
        UserRepository users = userRepository(user, userSaves);
        RecordRepository records = recordRepository(recordSaves);
        MemberOperationLogRepository logs = operationLogRepository(logSaves);
        ReflectionTestUtils.setField(service, "memberOrderRepository", orders);
        ReflectionTestUtils.setField(service, "userMemberRepository", userMembers);
        ReflectionTestUtils.setField(service, "userRepository", users);
        ReflectionTestUtils.setField(service, "recordRepository", records);
        ReflectionTestUtils.setField(service, "memberOperationLogRepository", logs);

        boolean result = service.markPaid("M202607050001", "wx-tx", new Date());

        assertTrue(result);
        assertEquals(Integer.valueOf(15), user.getPpd());
        assertEquals(1, userMemberSaves.count);
        assertEquals(1, userSaves.count);
        assertEquals(1, recordSaves.count);
        assertEquals(1, logSaves.count);
    }

    private static MemberBenefitRule submitRule(int discount, int minActualPpd, String effective, String status) {
        return rule("SUBMIT_YPAT", discount, minActualPpd, effective, status);
    }

    private static MemberBenefitRule rule(String scene, int discount, int minActualPpd, String effective, String status) {
        MemberBenefitRule rule = new MemberBenefitRule();
        rule.setLevelCode("BASIC");
        rule.setScene(scene);
        rule.setBenefitType("PPD_DISCOUNT");
        rule.setDiscountPpd(discount);
        rule.setMinActualPpd(minActualPpd);
        rule.setEffective(effective);
        rule.setStatus(status);
        return rule;
    }

    private static UserMember activeMember() {
        UserMember member = new UserMember();
        member.setUserId(1L);
        member.setLevel("BASIC");
        member.setExpireAt(daysFromNow(1));
        return member;
    }

    private static MemberOrder paidOrder(int giftPpd) {
        MemberOrder order = new MemberOrder();
        order.setOutTradeNo("M202607050001");
        order.setUserId(1L);
        order.setDurationDays(30);
        order.setGiftPpd(giftPpd);
        return order;
    }

    private static Date daysFromNow(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, days);
        return c.getTime();
    }

    private static MemberBenefitRuleRepository benefitRuleRepository(final MemberBenefitRule rule) {
        return benefitRuleRepository(rule, null);
    }

    private static MemberBenefitRuleRepository benefitRuleRepository(final MemberBenefitRule rule,
                                                                      final String[] queriedScene) {
        return proxy(MemberBenefitRuleRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findByLevelCodeAndSceneAndBenefitType".equals(method.getName())) {
                    if (queriedScene != null) queriedScene[0] = (String) args[1];
                    return rule;
                }
                if ("findBySceneAndBenefitTypeOrderByLevelCodeAsc".equals(method.getName())) {
                    return rule == null ? Collections.emptyList() : Collections.singletonList(rule);
                }
                if ("save".equals(method.getName())) return args[0];
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static PpdSceneConfigRepository ppdSceneConfigRepository(final PpdSceneConfig config) {
        return proxy(PpdSceneConfigRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findByScene".equals(method.getName())) {
                    return config != null && config.getScene().equals(args[0]) ? config : null;
                }
                if ("save".equals(method.getName())) return args[0];
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static PpdSceneConfig sceneConfig(String scene, int originalPpd, long version) {
        PpdSceneConfig config = new PpdSceneConfig();
        config.setScene(scene);
        config.setOriginalPpd(originalPpd);
        config.setVersion(version);
        return config;
    }

    private static MemberOrderRepository memberOrderRepository(final int markPaidRows, final MemberOrder order) {
        return proxy(MemberOrderRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("markPaidIfPending".equals(method.getName())) return markPaidRows;
                if ("findByOutTradeNo".equals(method.getName())) return order;
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static UserMemberRepository userMemberRepository(final UserMember member, final SaveCounter saves) {
        return proxy(UserMemberRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findOne".equals(method.getName())) return member;
                if ("save".equals(method.getName())) {
                    if (saves != null) saves.count++;
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static UserRepository userRepository(final User user, final SaveCounter saves) {
        return proxy(UserRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findById".equals(method.getName())) return user;
                if ("save".equals(method.getName())) {
                    saves.count++;
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static RecordRepository recordRepository(final SaveCounter saves) {
        return proxy(RecordRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("save".equals(method.getName())) {
                    saves.count++;
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static MemberOperationLogRepository operationLogRepository(final SaveCounter saves) {
        return proxy(MemberOperationLogRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("save".equals(method.getName())) {
                    saves.count++;
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        });
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
        if (double.class.equals(type)) return 0D;
        if (float.class.equals(type)) return 0F;
        if (short.class.equals(type)) return (short) 0;
        if (byte.class.equals(type)) return (byte) 0;
        if (char.class.equals(type)) return (char) 0;
        return null;
    }

    private static class SaveCounter {
        int count;
    }
}
