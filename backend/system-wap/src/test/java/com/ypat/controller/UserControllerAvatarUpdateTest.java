package com.ypat.controller;

import com.ypat.MessInfoQo;
import com.ypat.UserQo;
import com.ypat.model.SecurityUserDetails;
import com.ypat.service.UserServiceClient;
import com.ypat.storage.StorageBizPath;
import com.ypat.storage.StorageService;
import com.ypat.storage.StorageUrlPolicy;
import com.ypat.storage.StoredObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class UserControllerAvatarUpdateTest {
    private UserController controller;
    private FakeUserServiceClient userServiceClient;
    private FakeStorageUrlPolicy storageUrlPolicy;

    @Before
    public void setUp() {
        controller = new UserController();
        userServiceClient = new FakeUserServiceClient();
        storageUrlPolicy = new FakeStorageUrlPolicy();
        ReflectionTestUtils.setField(controller, "systemServiceClient", userServiceClient);
        ReflectionTestUtils.setField(controller, "storageUrlPolicy", storageUrlPolicy);
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

    @Test
    public void updUploadsBase64AvatarThroughStorageService() throws IOException {
        FakeStorageService storageService = new FakeStorageService();
        ReflectionTestUtils.setField(controller, "storageService", storageService);

        controller.upd(new UserQo(), "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=");

        assertEquals(StorageBizPath.AVATAR, storageService.bizPath);
        assertEquals("png", storageService.originalFilename);
        assertEquals("image/png", storageService.contentType);
        assertEquals("https://cdn.example.test/dev/avatar/a.jpg", userServiceClient.updatedUser.getImgpath());
    }

    @Test(expected = com.ypat.SysException.class)
    public void updRejectsExternalAvatarUrls() throws IOException {
        storageUrlPolicy.allow = false;

        controller.upd(new UserQo(), "https://example.com/avatar.jpg");
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

    private static class FakeStorageService implements StorageService {
        String originalFilename;
        String contentType;
        StorageBizPath bizPath;

        @Override
        public StoredObject upload(InputStream inputStream, String originalFilename, String contentType, StorageBizPath bizPath) {
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.bizPath = bizPath;
            return new StoredObject("dev/avatar/a.jpg", "https://cdn.example.test/dev/avatar/a.jpg");
        }

        @Override
        public boolean supportsUrl(String url) {
            return false;
        }

        @Override
        public String extractObjectKey(String url) {
            return null;
        }

        @Override
        public void deleteByUrl(String url) {
        }
    }

    private static class FakeStorageUrlPolicy extends StorageUrlPolicy {
        boolean allow = true;

        @Override
        public String requireSupported(String url) {
            if (allow) return url.trim();
            throw new com.ypat.SysException(com.ypat.ResponseCode.FAIL_PARA);
        }
    }
}
