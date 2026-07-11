package com.ypat.controller;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class PpdRechargeProductSourceTest {

    @Test
    public void productModelCarriesRecommendedFlagForRechargePackages() throws Exception {
        String product = read("../system-domain/src/main/java/com/ypat/entity/Product.java");
        String productQo = read("../system-object/src/main/java/com/ypat/ProductQo.java");
        String productService = read("../system-domain/src/main/java/com/ypat/service/ProductService.java");

        assertTrue(product.contains("private String recommended"));
        assertTrue(product.contains("getRecommended()"));
        assertTrue(product.contains("setRecommended(String recommended)"));
        assertTrue(productQo.contains("private String recommended"));
        assertTrue(productQo.contains("getRecommended()"));
        assertTrue(productQo.contains("setRecommended(String recommended)"));
        assertTrue(productService.contains("\"recommended\""));
    }

    @Test
    public void ppdOrderCreationAcceptsNewProductUpStatus() throws Exception {
        String orderController = read("src/main/java/com/ypat/controller/OrderController.java");

        assertTrue(orderController.contains("isProductUpStatus"));
        assertTrue(orderController.contains("\"0\".equals(status)"));
        assertTrue(orderController.contains("\"up\".equalsIgnoreCase(status)"));
    }

    @Test
    public void ppdRechargeUsesUnifiedWechatPaymentFlow() throws Exception {
        String controller = read("src/main/java/com/ypat/controller/PpdController.java");

        assertTrue(controller.contains("@PostMapping(\"/ppd/order/create\")"));
        assertTrue(controller.contains("@GetMapping(\"/ppd/order/status\")"));
        assertTrue(controller.contains("PaymentBusinessType.PPD.value"));
        assertTrue(controller.contains("wechatPaymentService.create(command)"));
        assertTrue(controller.contains("wechatPaymentReconcileService.syncPaidIfWechatSuccess(outTradeNo)"));
        assertTrue(controller.contains("product.getOldval()"));
        assertTrue(controller.contains("ppdOrderServiceClient.addPpdPayment(order)"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
