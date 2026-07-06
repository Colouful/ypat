package com.ypat.controller;

import com.ypat.MessInfoQo;
import com.ypat.UserQo;
import com.ypat.model.SecurityUserDetails;
import com.ypat.service.UserServiceClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class UserControllerAvatarUpdateTest {
    private UserController controller;
    private FakeUserServiceClient userServiceClient;

    @Before
    public void setUp() {
        controller = new UserController();
        userServiceClient = new FakeUserServiceClient();
        ReflectionTestUtils.setField(controller, "systemServiceClient", userServiceClient);
        setAuthenticatedUser("2");
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void updPersistsUploadedAvatarUrlFromPics() throws IOException {
        controller.upd(new UserQo(), "https://fastdfs.panghu.work/group1/M00/00/00/avatar.jpg");

        assertEquals(Long.valueOf(2), userServiceClient.updatedUser.getId());
        assertEquals(
                "https://fastdfs.panghu.work/group1/M00/00/00/avatar.jpg",
                userServiceClient.updatedUser.getImgpath()
        );
    }

    private void setAuthenticatedUser(String userId) {
        SecurityUserDetails details = new SecurityUserDetails();
        details.setUserId(userId);
        details.setUsername("user-" + userId);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static class FakeUserServiceClient implements UserServiceClient {
        UserQo updatedUser;

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
            return null;
        }

        @Override
        public String upd(UserQo userQo) {
            updatedUser = userQo;
            return "{\"code\":\"200\"}";
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
