package com.ypat.service;

import com.ypat.OauthQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.User;
import com.ypat.entity.UserImg;
import com.ypat.enums.UserImgType;
import com.ypat.enums.UserStatus;
import com.ypat.enums.YesNo;
import com.ypat.repository.UserImgRepository;
import com.ypat.repository.UserRepository;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UserServiceRealnameSecuritySourceTest {

    @Test
    public void realnameSubmitRequiresThreePhotosAndPaidOrRejectedStatus() throws Exception {
        String userService = read("backend/system-domain/src/main/java/com/ypat/service/UserService.java");
        String userRepository = read("backend/system-domain/src/main/java/com/ypat/repository/UserRepository.java");

        assertTrue(userService.contains("REALNAME_PHOTO_COUNT = 3"));
        assertTrue(userService.contains("userRepository.findByIdForUpdate(oauthQo.getUserid())"));
        assertTrue(userService.contains("pics == null || pics.size() != REALNAME_PHOTO_COUNT"));
        assertTrue(userService.contains("UserStatus.zfcg.value.equals(old.getStatus())"));
        assertTrue(userService.contains("UserStatus.shbtg.value.equals(old.getStatus())"));
        assertTrue(userService.contains("throw new SysException(ResponseCode.FAIL_NOREAL)"));
        assertTrue(userService.contains("old.setStatus(UserStatus.ytj.value)"));
        assertTrue(userRepository.contains("import javax.persistence.LockModeType;"));
        assertTrue(userRepository.contains("import org.springframework.data.jpa.repository.Lock;"));
        assertTrue(userRepository.contains("@Lock(LockModeType.PESSIMISTIC_WRITE)"));
        assertTrue(userRepository.contains("User findByIdForUpdate(@Param(\"id\") Long id)"));
    }

    @Test
    public void realnamedUserCannotSubmitAgainOrMutateImages() {
        Capture capture = new Capture();
        UserService service = service(user(UserStatus.shtg.value, YesNo.yes.value, oldImage(1L, UserImgType.front.value, "old-front")), capture);

        try {
            service.oauth(oauthQo("new-front", "new-back", "new-hand"));
            fail("expected already realname exception");
        } catch (SysException e) {
            assertEquals(1007, e.getCode());
        }

        assertTrue(capture.lockedLookupCalled);
        assertNull(capture.savedUser);
        assertEquals(0, capture.deletedImages.size());
        assertEquals(0, capture.savedImages.size());
    }

    @Test
    public void unpaidUserCannotSubmitEvenWithThreePhotos() {
        Capture capture = new Capture();
        UserService service = service(user(UserStatus.zc.value, YesNo.no.value), capture);

        try {
            service.oauth(oauthQo("front", "back", "hand"));
            fail("expected unpaid realname gate exception");
        } catch (SysException e) {
            assertEquals(ResponseCode.FAIL_NOREAL.getCode(), e.getCode());
        }

        assertTrue(capture.lockedLookupCalled);
        assertNull(capture.savedUser);
        assertEquals(0, capture.deletedImages.size());
        assertEquals(0, capture.savedImages.size());
    }

    @Test
    public void paidUserSubmitsThreePhotosAndReplacesOnlyDocumentImages() {
        UserImg head = oldImage(1L, UserImgType.head.value, "head");
        UserImg oldFront = oldImage(2L, UserImgType.front.value, "old-front");
        UserImg oldBack = oldImage(3L, UserImgType.back.value, "old-back");
        Capture capture = new Capture();
        UserService service = service(user(UserStatus.zfcg.value, YesNo.no.value, head, oldFront, oldBack), capture);

        service.oauth(oauthQo("new-front", "new-back", "new-hand"));

        assertTrue(capture.lockedLookupCalled);
        assertEquals("张三", capture.savedUser.getName());
        assertEquals("110101199001011234", capture.savedUser.getCertcode());
        assertEquals(UserStatus.ytj.value, capture.savedUser.getStatus());
        assertEquals(YesNo.no.value, capture.savedUser.getRealnameflag());
        assertEquals(2, capture.deletedImages.size());
        assertEquals(oldFront, capture.deletedImages.get(0));
        assertEquals(oldBack, capture.deletedImages.get(1));
        assertEquals(3, capture.savedImages.size());
        assertSavedImage(capture.savedImages.get(0), UserImgType.front.value, "new-front");
        assertSavedImage(capture.savedImages.get(1), UserImgType.back.value, "new-back");
        assertSavedImage(capture.savedImages.get(2), UserImgType.hand.value, "new-hand");
    }

    @Test
    public void rejectedUserCanResubmitAndReturnsToPendingAudit() {
        Capture capture = new Capture();
        UserService service = service(user(UserStatus.shbtg.value, YesNo.no.value), capture);

        service.oauth(oauthQo("retry-front", "retry-back", "retry-hand"));

        assertTrue(capture.lockedLookupCalled);
        assertEquals(UserStatus.ytj.value, capture.savedUser.getStatus());
        assertEquals(3, capture.savedImages.size());
    }

    @Test
    public void wrongPhotoCountCannotMutateUserOrImages() {
        Capture capture = new Capture();
        UserService service = service(user(UserStatus.zfcg.value, YesNo.no.value), capture);

        try {
            service.oauth(oauthQo("front", "back"));
            fail("expected realname photo count exception");
        } catch (SysException e) {
            assertEquals(ResponseCode.FAIL_REALNAME.getCode(), e.getCode());
        }

        assertTrue(capture.lockedLookupCalled);
        assertNull(capture.savedUser);
        assertEquals(0, capture.deletedImages.size());
        assertEquals(0, capture.savedImages.size());
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) {
            path = Paths.get(file.replace("backend/system-domain/", ""));
        }
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private UserService service(final User user, final Capture capture) {
        UserService service = new UserService();
        ReflectionTestUtils.setField(service, "userRepository", proxy(UserRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findByIdForUpdate".equals(method.getName())) {
                    capture.lockedLookupCalled = true;
                    return user;
                }
                if ("findById".equals(method.getName())) return user;
                if ("save".equals(method.getName())) {
                    capture.savedUser = (User) args[0];
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        }));
        ReflectionTestUtils.setField(service, "userImgRepository", proxy(UserImgRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("delete".equals(method.getName()) && args != null && args.length == 1 && args[0] instanceof UserImg) {
                    capture.deletedImages.add((UserImg) args[0]);
                    return null;
                }
                if ("save".equals(method.getName()) && args != null && args.length == 1 && args[0] instanceof Iterable) {
                    for (Object item : (Iterable<?>) args[0]) {
                        capture.savedImages.add((UserImg) item);
                    }
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        }));
        return service;
    }

    private User user(String status, String realnameflag, UserImg... userImgs) {
        User user = new User();
        user.setId(99L);
        user.setStatus(status);
        user.setRealnameflag(realnameflag);
        user.setUserImgs(Arrays.asList(userImgs));
        return user;
    }

    private UserImg oldImage(Long id, String type, String imgpath) {
        UserImg userImg = new UserImg();
        userImg.setId(id);
        userImg.setUserid(99L);
        userImg.setType(type);
        userImg.setImgpath(imgpath);
        return userImg;
    }

    private OauthQo oauthQo(String... pics) {
        OauthQo oauthQo = new OauthQo();
        oauthQo.setUserid(99L);
        oauthQo.setName("张三");
        oauthQo.setCertcode("110101199001011234");
        oauthQo.setPics(Arrays.asList(pics));
        return oauthQo;
    }

    private void assertSavedImage(UserImg userImg, String type, String imgpath) {
        assertEquals(Long.valueOf(99L), userImg.getUserid());
        assertEquals(type, userImg.getType());
        assertEquals(imgpath, userImg.getImgpath());
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
        return null;
    }

    private static class Capture {
        boolean lockedLookupCalled;
        User savedUser;
        List<UserImg> deletedImages = new ArrayList<>();
        List<UserImg> savedImages = new ArrayList<>();
    }
}
