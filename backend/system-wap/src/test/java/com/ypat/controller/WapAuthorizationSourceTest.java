package com.ypat.controller;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WapAuthorizationSourceTest {

    @Test
    public void privateControllersBindQueriesToAuthenticatedUser() throws IOException {
        String userController = readSource("UserController.java");
        String mypatController = readSource("MypatInfoController.java");
        String billController = readSource("BillController.java");
        String recordController = readSource("RecordController.java");
        String oauthController = readSource("OauthController.java");

        assertTrue(userController.contains("userQo.setId(id);"));
        assertTrue(mypatController.contains("public String myAppList(MessInfoQo messInfoQo, Long userid)"));
        assertTrue(mypatController.contains("messInfoQo.setSendperid(Long.parseLong(UserUtil.getUserId()))"));
        assertFalse(mypatController.contains("else {\n            messInfoQo.setSendperid(userid);"));
        assertTrue(mypatController.contains("ypatInfoQo.setUserid(Long.parseLong(UserUtil.getUserId()))"));
        assertTrue(mypatController.contains("userid = Long.parseLong(UserUtil.getUserId())"));
        assertTrue(billController.contains("billQo.setUserid(Long.parseLong(UserUtil.getUserId()))"));
        assertTrue(recordController.contains("recordQo.setUserid(Long.parseLong(UserUtil.getUserId()))"));
        assertTrue(oauthController.contains("throw new SysException(ResponseCode.FAIL_VAL);"));
    }

    @Test
    public void wapManagementActionsAreRejected() throws IOException {
        String ypatController = readSource("YpatInfoController.java");
        String oauthController = readSource("OauthController.java");

        assertTrue(ypatController.contains("public String auditList"));
        assertTrue(ypatController.contains("public String audit(Long id"));
        assertTrue(ypatController.contains("public String upRecom"));
        assertFalse(ypatController.contains("return ypatServiceClient.upRecom"));
        assertFalse(ypatController.contains("return ypatServiceClient.audit"));
        assertTrue(oauthController.contains("public String userAudit"));
    }

    private String readSource(String fileName) throws IOException {
        Path path = Paths.get("src/main/java/com/ypat/controller/" + fileName);
        if (!Files.exists(path)) {
            path = Paths.get("backend/system-wap/src/main/java/com/ypat/controller/" + fileName);
        }
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
