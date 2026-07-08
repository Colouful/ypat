package com.ypat.controller;

import com.ypat.DepositConfigQo;
import com.ypat.DepositOrderQo;
import com.ypat.service.DepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DepositController {

    @Autowired
    private DepositService depositService;

    @GetMapping("/service/deposit/config")
    public DepositConfigQo config() {
        return depositService.getConfig();
    }

    @PostMapping("/service/deposit/config/save")
    public DepositConfigQo saveConfig(@RequestBody DepositConfigQo qo) {
        return depositService.saveConfig(qo);
    }

    @PostMapping("/service/deposit/order/create")
    public DepositOrderQo createOrder(@RequestParam("userId") Long userId,
                                      @RequestParam("channel") String channel) {
        return depositService.createPendingOrder(userId, channel);
    }

    @GetMapping("/service/deposit/order/get")
    public DepositOrderQo getOrder(@RequestParam("outTradeNo") String outTradeNo,
                                   @RequestParam(value = "userId", required = false) Long userId) {
        return depositService.getOrder(outTradeNo, userId);
    }

    @PostMapping("/service/deposit/order/prepared")
    public DepositOrderQo prepared(@RequestParam("outTradeNo") String outTradeNo,
                                   @RequestParam("channel") String channel,
                                   @RequestParam(value = "prepayId", required = false) String prepayId) {
        return depositService.updatePaymentPrepared(outTradeNo, channel, prepayId);
    }

    @PostMapping("/service/deposit/admin/orders")
    public Map<String, Object> adminOrders(@RequestBody DepositOrderQo qo) {
        return depositService.findAdminOrders(qo);
    }
}
