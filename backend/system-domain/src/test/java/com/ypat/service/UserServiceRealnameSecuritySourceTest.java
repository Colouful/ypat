package com.ypat.service;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class UserServiceRealnameSecuritySourceTest {

    @Test
    public void realnameSubmitRequiresThreePhotosAndPaidOrRejectedStatus() throws Exception {
        String source = read("backend/system-domain/src/main/java/com/ypat/service/UserService.java");

        assertTrue(source.contains("REALNAME_PHOTO_COUNT = 3"));
        assertTrue(source.contains("pics == null || pics.size() != REALNAME_PHOTO_COUNT"));
        assertTrue(source.contains("UserStatus.zfcg.value.equals(old.getStatus())"));
        assertTrue(source.contains("UserStatus.shbtg.value.equals(old.getStatus())"));
        assertTrue(source.contains("throw new SysException(ResponseCode.FAIL_NOREAL)"));
        assertTrue(source.contains("old.setStatus(UserStatus.ytj.value)"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) {
            path = Paths.get(file.replace("backend/system-domain/", ""));
        }
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
