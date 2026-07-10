package com.ypat.controller;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class RealnameOrderSecuritySourceTest {

    @Test
    public void realnameOrderAmountIsForcedByServerInFen() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/controller/OrderController.java");

        assertTrue(source.contains("REALNAME_AUDIT_FEE_FEN = 2900"));
        assertTrue(source.contains("OrderType.REAL.value.equals(orderQo.getType())"));
        assertTrue(source.contains("orderQo.setTotal_fee(REALNAME_AUDIT_FEE_FEN)"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) {
            path = Paths.get(file.replace("backend/system-wap/", ""));
        }
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
