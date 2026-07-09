package com.ypat.service;

import com.ypat.CheckinResultQo;
import com.ypat.CheckinTodayQo;
import com.ypat.entity.CheckinRecord;
import com.ypat.entity.CheckinRule;
import com.ypat.entity.Record;
import com.ypat.entity.User;
import com.ypat.enums.RecordType;
import com.ypat.enums.YesNo;
import com.ypat.repository.CheckinRecordRepository;
import com.ypat.repository.CheckinRuleRepository;
import com.ypat.repository.RecordRepository;
import com.ypat.repository.UserRepository;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CheckinServiceTest {

    @Test
    public void todayReturnsUncheckedWhenNoRecord() {
        CheckinService service = service(rule(YesNo.yes.value, 1), checkinRecords(null, null), records(null), users(null, null));

        CheckinTodayQo today = service.today(1L);

        assertTrue(today.getEnabled());
        assertFalse(today.getCheckedIn());
        assertEquals(Integer.valueOf(1), today.getRewardPpd());
        assertEquals("每日签到", today.getConfirmTitle());
        assertNotNull(today.getCheckinDate());
    }

    @Test
    public void doCheckinAddsPpdAndRecordOnce() {
        SaveCounter checkinSaves = new SaveCounter();
        SaveCounter recordSaves = new SaveCounter();
        SaveCounter userSaves = new SaveCounter();
        Captured<Record> savedRecord = new Captured<Record>();
        User user = user(1L, 3);
        CheckinService service = service(
                rule(YesNo.yes.value, 1),
                checkinRecords(null, checkinSaves),
                records(recordSaves, savedRecord, 200L),
                users(user, userSaves));

        CheckinResultQo result = service.doCheckin(1L);

        assertTrue(result.getCheckedIn());
        assertEquals(Integer.valueOf(1), result.getRewardPpd());
        assertEquals(Integer.valueOf(4), result.getCurrentPpd());
        assertEquals(Long.valueOf(200L), result.getRecordId());
        assertEquals(Integer.valueOf(4), user.getPpd());
        assertEquals(2, checkinSaves.count);
        assertEquals(1, recordSaves.count);
        assertEquals(1, userSaves.count);
        assertNotNull(savedRecord.value);
        assertEquals(RecordType.CHECKIN.value, savedRecord.value.getType());
        assertEquals(Integer.valueOf(1), savedRecord.value.getPpd());
        assertEquals(Long.valueOf(1L), savedRecord.value.getUserid());
    }

    @Test
    public void doCheckinReturnsAlreadyCheckedWithoutAddingPpd() {
        SaveCounter recordSaves = new SaveCounter();
        SaveCounter userSaves = new SaveCounter();
        CheckinRecord existing = new CheckinRecord();
        existing.setUserid(1L);
        existing.setRewardPpd(1);
        CheckinService service = service(
                rule(YesNo.yes.value, 1),
                checkinRecords(existing, null),
                records(recordSaves),
                users(user(1L, 3), userSaves));

        CheckinResultQo result = service.doCheckin(1L);

        assertTrue(result.getCheckedIn());
        assertEquals(Integer.valueOf(0), result.getRewardPpd());
        assertEquals("今日已签到", result.getMessage());
        assertEquals(0, recordSaves.count);
        assertEquals(0, userSaves.count);
    }

    @Test
    public void doCheckinReturnsClosedWhenRuleDisabled() {
        SaveCounter checkinSaves = new SaveCounter();
        CheckinService service = service(
                rule(YesNo.no.value, 1),
                checkinRecords(null, checkinSaves),
                records(null),
                users(user(1L, 3), null));

        CheckinResultQo result = service.doCheckin(1L);

        assertFalse(result.getCheckedIn());
        assertEquals(Integer.valueOf(0), result.getRewardPpd());
        assertEquals("签到活动暂未开启", result.getMessage());
        assertEquals(0, checkinSaves.count);
    }

    private static CheckinService service(CheckinRuleRepository rules, CheckinRecordRepository checkins,
                                          RecordRepository records, UserRepository users) {
        return new CheckinService(rules, checkins, records, users);
    }

    private static CheckinRuleRepository rule(final String enabled, final int rewardPpd) {
        return proxy(CheckinRuleRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findTopByOrderByIdAsc".equals(method.getName())) {
                    CheckinRule rule = new CheckinRule();
                    rule.setEnabled(enabled);
                    rule.setRewardPpd(rewardPpd);
                    rule.setConfirmTitle("每日签到");
                    rule.setConfirmContent("签到成功可获得 1 拍豆");
                    return rule;
                }
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static CheckinRecordRepository checkinRecords(final CheckinRecord existing, final SaveCounter saves) {
        return proxy(CheckinRecordRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findByUseridAndCheckinDate".equals(method.getName())) return existing;
                if ("save".equals(method.getName())) {
                    if (saves != null) saves.count++;
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static RecordRepository records(final SaveCounter saves) {
        return records(saves, null, null);
    }

    private static RecordRepository records(final SaveCounter saves, final Captured<Record> captured, final Long savedId) {
        return proxy(RecordRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("save".equals(method.getName())) {
                    if (saves != null) saves.count++;
                    Record record = (Record) args[0];
                    if (savedId != null) record.setId(savedId);
                    if (captured != null) captured.value = record;
                    return record;
                }
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static UserRepository users(final User user, final SaveCounter saves) {
        return proxy(UserRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findById".equals(method.getName())) return user;
                if ("save".equals(method.getName())) {
                    if (saves != null) saves.count++;
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static User user(Long id, int ppd) {
        User user = new User();
        user.setId(id);
        user.setPpd(ppd);
        return user;
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

    private static class Captured<T> {
        T value;
    }
}
