package com.ypat.service;

import com.ypat.MessInfoQo;
import com.ypat.UserQo;
import com.ypat.SysException;
import com.ypat.model.SecurityUserDetails;
import com.ypat.util.JwtTokenUtil;
import com.ypat.util.RedisClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class UserServiceTokenRefreshTest {
    private UserService userService;
    private FakeUserServiceClient userServiceClient;

    @Before
    public void setUp() {
        userService = new UserService();
        userServiceClient = new FakeUserServiceClient();
        ReflectionTestUtils.setField(userService, "userServiceClient", userServiceClient);
        ReflectionTestUtils.setField(userService, "redisClient", new RedisClient());
        ReflectionTestUtils.setField(userService, "jwtTokenUtil", new FixedJwtTokenUtil());
    }

    @Test
    public void refreshTokenUsesAuthenticatedUserIdAndIgnoresMobileParameter() {
        userServiceClient.getResponse = "{\"id\":7,\"mobile\":\"13800138000\",\"nickname\":\"当前用户\",\"status\":\"0\"}";

        UserQo request = new UserQo();
        request.setMobile("13900139000");
        Map<String, String> result = userService.getToken(request, "7");

        assertEquals(Long.valueOf(7), userServiceClient.requestedId);
        assertEquals("token-7", result.get("token"));
        assertEquals("13800138000", result.get("mobile"));
    }

    @Test(expected = SysException.class)
    public void refreshTokenRejectsMissingAuthenticatedUser() {
        userService.getToken(new UserQo(), null);
    }

    @Test(expected = SysException.class)
    public void refreshTokenRejectsUnknownAuthenticatedUser() {
        userServiceClient.getResponse = null;
        userService.getToken(new UserQo(), "404");
    }

    private static class FixedJwtTokenUtil extends JwtTokenUtil {
        @Override
        public String generateToken(SecurityUserDetails userDetails) {
            return "token-" + userDetails.getUserId();
        }
    }

    private static class FakeUserServiceClient implements UserServiceClient {
        Long requestedId;
        String getResponse;

        @Override
        public String get(Long id) {
            requestedId = id;
            return getResponse;
        }

        @Override
        public String findByMobile(String mobile) {
            throw new AssertionError("Token refresh must not query by mobile");
        }

        @Override
        public String linkway(Long id, Long userid, Long messid) {
            return null;
        }

        @Override
        public String add(UserQo userQo) {
            return null;
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
        public String findByCityAndProfess(Long userid, String city) {
            return null;
        }
    }
}

