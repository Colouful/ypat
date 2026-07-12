package com.ypat.service;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PpdBenefitChargeSourceTest {
    @Test
    public void paidBusinessActionsUseConfiguredQuotesAndDescribedRecords() throws Exception {
        String ypatSource = read("YpatInfoService.java");
        String messSource = read("MessInfoService.java");
        String workSource = read("WorkService.java");
        String userSource = read("UserService.java");

        assertTrue(ypatSource.contains("quoteBenefit(ypatInfo.getUserid(), PpdBenefitScene.SUBMIT_YPAT.getCode())"));
        assertTrue(messSource.contains("quoteBenefit(userid, PpdBenefitScene.APPLY_YPAT.getCode())"));
        assertTrue(workSource.contains("quoteBenefit(viewerId, PpdBenefitScene.APPLY_YPAT.getCode())"));
        assertTrue(userSource.contains("quoteBenefit(id, PpdBenefitScene.VIEW_CONTACT.getCode())"));

        assertFalse(ypatSource.contains("Constant.PUB_NEED_PPD"));
        assertFalse(messSource.contains("Constant.APPLY_NEED_PPD"));
        assertFalse(workSource.contains("Constant.APPLY_NEED_PPD"));
        assertFalse(userSource.contains("Constant.VIEW_NEED_PPD"));

        assertRecordMetadata(ypatSource, "PpdBenefitScene.SUBMIT_YPAT.getCode()", "发布约拍扣除拍豆");
        assertRecordMetadata(messSource, "PpdBenefitScene.APPLY_YPAT.getCode()", "发起约拍申请扣除拍豆");
        assertRecordMetadata(workSource, "PpdBenefitScene.APPLY_YPAT.getCode()", "发起约拍申请扣除拍豆");
        assertRecordMetadata(userSource, "PpdBenefitScene.VIEW_CONTACT.getCode()", "查看联系方式扣除拍豆");
    }

    private static void assertRecordMetadata(String source, String scene, String description) {
        assertTrue(source.contains("setScene(" + scene + ")"));
        assertTrue(source.contains("setDescription(\"" + description + "\")"));
    }

    private static String read(String fileName) throws Exception {
        return new String(Files.readAllBytes(Paths.get("src/main/java/com/ypat/service", fileName)),
                StandardCharsets.UTF_8);
    }
}
