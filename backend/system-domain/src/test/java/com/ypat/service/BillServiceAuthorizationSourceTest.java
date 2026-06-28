package com.ypat.service;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class BillServiceAuthorizationSourceTest {

    @Test
    public void billPageQueryCanBeRestrictedByAuthenticatedUserOrders() throws IOException {
        String source = readSource();

        assertTrue(source.contains("queryQo.getUserid()"));
        assertTrue(source.contains("orderSubquery.from(Order.class)"));
        assertTrue(source.contains("orderRoot.get(\"userid\")"));
        assertTrue(source.contains("root.get(\"out_trade_no\").in(orderSubquery)"));
    }

    private String readSource() throws IOException {
        Path path = Paths.get("src/main/java/com/ypat/service/BillService.java");
        if (!Files.exists(path)) {
            path = Paths.get("backend/system-domain/src/main/java/com/ypat/service/BillService.java");
        }
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
