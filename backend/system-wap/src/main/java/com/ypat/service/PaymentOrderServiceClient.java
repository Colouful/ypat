package com.ypat.service;

import com.ypat.PaymentOrderQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("SYSTEM-API")
public interface PaymentOrderServiceClient {
    @PostMapping("/service/payment/order/createPending")
    PaymentOrderQo createPending(@RequestParam("businessType") String businessType,
                                 @RequestParam("businessOrderNo") String businessOrderNo,
                                 @RequestParam("outTradeNo") String outTradeNo,
                                 @RequestParam("userId") Long userId,
                                 @RequestParam("channel") String channel,
                                 @RequestParam("amountFen") Integer amountFen);

    @GetMapping("/service/payment/order/get")
    PaymentOrderQo get(@RequestParam("outTradeNo") String outTradeNo);

    @PostMapping("/service/payment/order/prepared")
    PaymentOrderQo prepared(@RequestParam("outTradeNo") String outTradeNo,
                            @RequestParam(value = "channel", required = false) String channel,
                            @RequestParam(value = "prepayId", required = false) String prepayId,
                            @RequestParam(value = "h5Url", required = false) String h5Url);

    @PostMapping("/service/payment/markPaid")
    Boolean markPaid(@RequestParam("outTradeNo") String outTradeNo,
                     @RequestParam("txId") String txId,
                     @RequestParam("amountFen") Integer amountFen,
                     @RequestParam(value = "paidAtMs", required = false) Long paidAtMs,
                     @RequestParam(value = "eventId", required = false) String eventId,
                     @RequestParam(value = "digest", required = false) String digest);

    @PostMapping("/service/payment/admin/orders")
    Map<String, Object> adminOrders(@RequestBody PaymentOrderQo qo);
}
