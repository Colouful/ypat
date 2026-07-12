package com.ypat.controller;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class RealnamePaymentV3SourceTest {

    @Test
    public void realnameUsesUnifiedPaymentWithServerControlledAmount() throws Exception {
        String controller = read("src/main/java/com/ypat/controller/RealnamePaymentController.java");

        assertTrue(controller.contains("@PostMapping(\"/realname/order/create\")"));
        assertTrue(controller.contains("@GetMapping(\"/realname/order/status\")"));
        assertTrue(controller.contains("DepositServiceClient depositServiceClient"));
        assertTrue(controller.contains("getRealnameAuditFeeFen()"));
        assertTrue(controller.contains("realnameAuditFeeFen"));
        assertTrue(!controller.contains("REALNAME_AUDIT_FEE_FEN = 2900"));
        assertTrue(controller.contains("PaymentBusinessType.REALNAME.value"));
        assertTrue(controller.contains("wechatPaymentService.create(command)"));
        assertTrue(controller.contains("wechatPaymentReconcileService.syncPaidIfWechatSuccess(outTradeNo)"));
        assertTrue(controller.contains("realnameOrderServiceClient.addRealnamePayment(order)"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get("backend/system-wap", file);
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
