package com.ypat.controller;

import com.ypat.FeedbackQo;
import com.ypat.SysException;
import com.ypat.model.SecurityUserDetails;
import com.ypat.service.FeedbackServiceClient;
import com.ypat.util.RedisClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FeedbackControllerTest {
    private FeedbackController controller;
    private FakeFeedbackServiceClient feedbackServiceClient;
    private FakeRedisClient redisClient;

    @Before
    public void setUp() {
        controller = new FeedbackController();
        feedbackServiceClient = new FakeFeedbackServiceClient();
        redisClient = new FakeRedisClient();
        ReflectionTestUtils.setField(controller, "feedbackServiceClient", feedbackServiceClient);
        ReflectionTestUtils.setField(controller, "redisClient", redisClient);
        setAuthenticatedUser("42");
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void addUsesAuthenticatedUserAndSanitizesInput() {
        String result = controller.add("  这里是一段合法反馈<script>  ", "  13800138000  ");

        assertEquals("{\"code\":\"200\"}", result);
        assertEquals(Long.valueOf(42), feedbackServiceClient.feedbackQo.getUserid());
        assertEquals("这里是一段合法反馈＜script＞", feedbackServiceClient.feedbackQo.getContent());
        assertEquals("13800138000", feedbackServiceClient.feedbackQo.getContact());
        assertEquals("1", redisClient.values.get("feedback:add:42"));
        assertEquals(Long.valueOf(60), redisClient.ttls.get("feedback:add:42"));
    }

    @Test(expected = SysException.class)
    public void addRejectsAnonymousUser() {
        SecurityContextHolder.clearContext();
        controller.add("这里是一段合法反馈内容", "");
    }

    @Test(expected = SysException.class)
    public void addRejectsShortContent() {
        controller.add("太短", "");
    }

    @Test(expected = SysException.class)
    public void addRejectsLongContact() {
        controller.add("这里是一段合法反馈内容", repeat("1", 101));
    }

    @Test(expected = SysException.class)
    public void addRejectsFrequentSubmission() {
        redisClient.values.put("feedback:add:42", "1");
        controller.add("这里是一段合法反馈内容", "");
    }

    @Test
    public void addAllowsSubmissionWhenRedisIsUnavailable() {
        redisClient.fail = true;

        String result = controller.add("这里是一段合法反馈内容", "");

        assertEquals("{\"code\":\"200\"}", result);
        assertEquals(Long.valueOf(42), feedbackServiceClient.feedbackQo.getUserid());
        assertEquals("这里是一段合法反馈内容", feedbackServiceClient.feedbackQo.getContent());
    }

    private void setAuthenticatedUser(String userId) {
        SecurityUserDetails details = new SecurityUserDetails();
        details.setUserId(userId);
        details.setUsername("user-" + userId);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }

    private static class FakeFeedbackServiceClient implements FeedbackServiceClient {
        FeedbackQo feedbackQo;

        @Override
        public String add(FeedbackQo feedbackQo) {
            this.feedbackQo = feedbackQo;
            return "{\"code\":\"200\"}";
        }
    }

    private static class FakeRedisClient extends RedisClient {
        final Map<String, Object> values = new HashMap<String, Object>();
        final Map<String, Long> ttls = new HashMap<String, Long>();
        boolean fail;

        @Override
        public Object get(String key) {
            if (fail) {
                throw new IllegalStateException("redis down");
            }
            return values.get(key);
        }

        @Override
        public void put(String key, Object object, long timeout) {
            if (fail) {
                throw new IllegalStateException("redis down");
            }
            values.put(key, object);
            ttls.put(key, timeout);
        }
    }
}
