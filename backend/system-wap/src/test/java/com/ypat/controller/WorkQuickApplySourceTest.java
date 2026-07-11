package com.ypat.controller;

import com.ypat.WorkQuickApplyQo;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WorkQuickApplySourceTest {

    @Test
    public void workQuickApplyQoAcceptsReasonAndContactFields() throws Exception {
        Field reasonField = WorkQuickApplyQo.class.getDeclaredField("reason");
        Field mobileField = WorkQuickApplyQo.class.getDeclaredField("mobile");
        Field wxField = WorkQuickApplyQo.class.getDeclaredField("wx");

        Method getReason = WorkQuickApplyQo.class.getDeclaredMethod("getReason");
        Method setReason = WorkQuickApplyQo.class.getDeclaredMethod("setReason", String.class);
        Method getMobile = WorkQuickApplyQo.class.getDeclaredMethod("getMobile");
        Method setMobile = WorkQuickApplyQo.class.getDeclaredMethod("setMobile", String.class);
        Method getWx = WorkQuickApplyQo.class.getDeclaredMethod("getWx");
        Method setWx = WorkQuickApplyQo.class.getDeclaredMethod("setWx", String.class);

        assertNotNull(reasonField);
        assertNotNull(mobileField);
        assertNotNull(wxField);
        assertNotNull(getReason);
        assertNotNull(setReason);
        assertNotNull(getMobile);
        assertNotNull(setMobile);
        assertNotNull(getWx);
        assertNotNull(setWx);
    }

    @Test
    public void workServiceQuickApplyCreatesYpatAndMessageRecords() throws IOException {
        String source = readSource(
                "../system-domain/src/main/java/com/ypat/service/WorkService.java",
                "backend/system-domain/src/main/java/com/ypat/service/WorkService.java",
                "WorkService.java should exist");

        assertTrue(source.contains("createQuickApplyYpat(work, author, target)"));
        assertTrue(source.contains("messInfoRepository.save(messInfo)"));
        assertTrue(source.contains("messInfoRepository.countSendByWorkId(MessType.send.value, viewerId, workId)"));
        assertTrue(source.contains("recordRepository.save(record)"));
        assertTrue(source.contains("viewer.setPpd(userPpd - Constant.APPLY_NEED_PPD)"));
        assertTrue(source.contains("res.put(\"ypatid\", ypatInfo.getId())"));
        assertTrue(source.contains("res.put(\"messId\", messInfo.getId())"));
    }

    @Test
    public void workDetailReturnsMiniappCompatibleStateFields() throws IOException {
        String source = readSource(
                "../system-domain/src/main/java/com/ypat/service/WorkService.java",
                "backend/system-domain/src/main/java/com/ypat/service/WorkService.java",
                "WorkService.java should exist");

        assertTrue(source.contains("userMap.put(\"professTxt\", UserProfess.getNameByCode(user.getProfess()))"));
        assertTrue(source.contains("res.put(\"likeflag\", liked ? \"1\" : \"0\")"));
        assertTrue(source.contains("res.put(\"colflag\", favorited ? \"1\" : \"0\")"));
        assertTrue(source.contains("res.put(\"favoriteflag\", favorited ? \"1\" : \"0\")"));
        assertTrue(source.contains("messInfoRepository.countSendByWorkId(MessType.send.value, viewerId, workId)"));
        assertTrue(source.contains("res.put(\"isApplied\", applied)"));
    }

    private String readSource(String modulePath, String repoPath, String message) throws IOException {
        Path path = Paths.get(modulePath);
        if (!Files.exists(path)) {
            path = Paths.get(repoPath);
        }
        assertTrue(message, Files.exists(path));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
