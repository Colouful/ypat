package com.ypat.service;

import com.ypat.DepositConfigQo;
import com.ypat.DepositOrderQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("SYSTEM-API")
public interface DepositServiceClient {
    @GetMapping("/service/deposit/config")
    DepositConfigQo config();

    @PostMapping("/service/deposit/config/save")
    DepositConfigQo saveConfig(@RequestBody DepositConfigQo qo);

    @PostMapping("/service/deposit/order/create")
    DepositOrderQo createOrder(@RequestParam("userId") Long userId,
                               @RequestParam("channel") String channel);

    @GetMapping("/service/deposit/order/get")
    DepositOrderQo getOrder(@RequestParam("outTradeNo") String outTradeNo,
                            @RequestParam(value = "userId", required = false) Long userId);

    @PostMapping("/service/deposit/order/prepared")
    DepositOrderQo prepared(@RequestParam("outTradeNo") String outTradeNo,
                            @RequestParam("channel") String channel,
                            @RequestParam(value = "prepayId", required = false) String prepayId);

    @PostMapping("/service/deposit/admin/orders")
    Map<String, Object> adminOrders(@RequestBody DepositOrderQo qo);
}
