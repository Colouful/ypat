package com.ypat.controller;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class YpatApplyFlowSourceTest {

    @Test
    public void ypatDetailReturnsPublisherAndAppliedState() throws IOException {
        String qoSource = readSource(
                "../system-object/src/main/java/com/ypat/YpatInfoQo.java",
                "backend/system-object/src/main/java/com/ypat/YpatInfoQo.java");
        String serviceSource = readSource(
                "../system-domain/src/main/java/com/ypat/service/YpatInfoService.java",
                "backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java");

        assertTrue(qoSource.contains("public String getMsgflag()"));
        assertTrue(qoSource.contains("public void setMsgflag(String msgflag)"));
        assertTrue(serviceSource.contains("ypatInfoQo.setUserid(user.getId())"));
        assertTrue(serviceSource.contains("messInfoRepository.countSend(MessType.send.value, userid, ypatInfo.getId())"));
        assertTrue(serviceSource.contains("ypatInfoQo.setMsgflag(hasSent != null && hasSent > 0 ? YesNo.yes.value : YesNo.no.value)"));
    }

    @Test
    public void ypatApplySerializesDuplicateChecksAndValidatesIdentity() throws IOException {
        String messageServiceSource = readSource(
                "../system-domain/src/main/java/com/ypat/service/MessInfoService.java",
                "backend/system-domain/src/main/java/com/ypat/service/MessInfoService.java");
        String workServiceSource = readSource(
                "../system-domain/src/main/java/com/ypat/service/WorkService.java",
                "backend/system-domain/src/main/java/com/ypat/service/WorkService.java");

        assertTrue(messageServiceSource.contains("userRepository.findByIdForUpdate(userid)"));
        assertTrue(messageServiceSource.contains("if(ypatInfo == null)"));
        assertTrue(messageServiceSource.contains("if(userid.equals(recper.getId()))"));
        assertTrue(workServiceSource.contains("userRepository.findByIdForUpdate(viewerId)"));
    }

    private String readSource(String modulePath, String repoPath) throws IOException {
        Path path = Paths.get(modulePath);
        if (!Files.exists(path)) {
            path = Paths.get(repoPath);
        }
        assertTrue("source file should exist: " + repoPath, Files.exists(path));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
