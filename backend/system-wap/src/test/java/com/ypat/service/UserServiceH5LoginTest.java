package com.ypat.service;

import com.ypat.UserQo;
import com.ypat.MessInfoQo;
import com.ypat.enums.UserOrigType;
import com.ypat.model.SecurityUserDetails;
import com.ypat.util.JwtTokenUtil;
import com.ypat.util.RedisClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UserServiceH5LoginTest {
    private UserService userService;
    private FakeUserServiceClient userServiceClient;
    private FakeRedisClient redisClient;

    @Before
    public void setUp() {
        userService = new UserService();
        userServiceClient = new FakeUserServiceClient();
        redisClient = new FakeRedisClient();

        ReflectionTestUtils.setField(userService, "userServiceClient", userServiceClient);
        ReflectionTestUtils.setField(userService, "redisClient", redisClient);
        ReflectionTestUtils.setField(userService, "jwtTokenUtil", new FixedJwtTokenUtil());
    }

    @Test
    public void h5LoginUsesMobileAndSmsCodeWithoutWechatEncryptedData() {
        redisClient.values.put("h5:login:sms:13800138000", "123456");
        userServiceClient.addResponse = "{\"id\":88,\"mobile\":\"13800138000\",\"nickname\":\"H5用户\"}";

        UserQo input = new UserQo();
        input.setChannel(UserOrigType.pc.value);
        input.setMobile("13800138000");
        input.setSmsCode("123456");

        Map<String, String> result = userService.login(input);

        assertEquals("token-h5", result.get("token"));
        assertEquals("88", result.get("id"));
        assertEquals("13800138000", result.get("mobile"));
        assertEquals(UserOrigType.pc.value, userServiceClient.addedUser.getChannel());
        assertEquals("13800138000", userServiceClient.addedUser.getMobile());
        assertTrue(redisClient.removedKey.equals("h5:login:sms:13800138000"));
    }

    @Test
    public void sendH5LoginCodeStoresSixDigitCodeForMobile() {
        Map<String, String> result = userService.sendH5LoginCode("13800138000");

        assertEquals("13800138000", result.get("mobile"));
        assertEquals("300", result.get("expiresIn"));
        String debugCode = result.get("debugCode");
        assertTrue(debugCode.matches("\\d{6}"));
        assertFalse(debugCode.equals("000000"));
        assertEquals(debugCode, redisClient.values.get("h5:login:sms:13800138000"));
        assertEquals(Long.valueOf(300L), redisClient.ttls.get("h5:login:sms:13800138000"));
    }

    @Test
    public void sendH5LoginCodeReturnsWhitelistCodeWithoutRedis() {
        Map<String, String> result = userService.sendH5LoginCode("18888888888");

        assertEquals("18888888888", result.get("mobile"));
        assertEquals("300", result.get("expiresIn"));
        assertEquals("888888", result.get("debugCode"));
        assertFalse(redisClient.values.containsKey("h5:login:sms:18888888888"));
    }

    @Test
    public void h5WhitelistLoginAcceptsFixedCodeWithoutCachedSmsCode() {
        userServiceClient.addResponse = "{\"id\":188,\"mobile\":\"18888888888\",\"nickname\":\"测试账号\"}";

        UserQo input = new UserQo();
        input.setChannel(UserOrigType.pc.value);
        input.setMobile("18888888888");
        input.setSmsCode("888888");

        Map<String, String> result = userService.login(input);

        assertEquals("token-h5", result.get("token"));
        assertEquals("188", result.get("id"));
        assertEquals("18888888888", result.get("mobile"));
        assertEquals(UserOrigType.pc.value, userServiceClient.addedUser.getChannel());
        assertNull(redisClient.removedKey);
    }

    private static class FixedJwtTokenUtil extends JwtTokenUtil {
        @Override
        public String generateToken(SecurityUserDetails userDetails) {
            return "token-h5";
        }
    }

    private static class FakeRedisClient extends RedisClient {
        final Map<String, Object> values = new HashMap<String, Object>();
        final Map<String, Long> ttls = new HashMap<String, Long>();
        String removedKey;

        @Override
        public void put(String key, Object object, long timeout) {
            values.put(key, object);
            ttls.put(key, timeout);
        }

        @Override
        public Object get(String key) {
            return values.get(key);
        }

        @Override
        public void removeForExpire(String key) {
            removedKey = key;
            values.remove(key);
        }
    }

    private static class FakeUserServiceClient implements UserServiceClient {
        UserQo addedUser;
        String addResponse;

        @Override
        public String get(Long id) {
            return null;
        }

        @Override
        public String findByMobile(String mobile) {
            return null;
        }

        @Override
        public String linkway(Long id, Long userid, Long messid) {
            return null;
        }

        @Override
        public String add(UserQo userQo) {
            addedUser = userQo;
            return addResponse;
        }

        @Override
        public String upd(UserQo userQo) {
            return null;
        }

        @Override
        public String findPage(UserQo userQo) {
            return null;
        }

        @Override
        public String myRecAdd(MessInfoQo messInfoQo) {
            return null;
        }

        @Override
        public String myScAdd(Long userid, Long ypatid) {
            return null;
        }

        @Override
        public String myScCancel(Long userid, Long ypatid) {
            return null;
        }

        @Override
        public String findByCityAndProfess(Long userid, String city) {
            return null;
        }
    }
}
