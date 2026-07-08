package com.ypat.controller;

import com.ypat.PaymentOrderQo;
import com.ypat.entity.PaymentOrder;
import com.ypat.service.PaymentCallbackService;
import com.ypat.service.PaymentOrderService;
import com.ypat.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
public class PaymentOrderController {

    @Autowired
    private PaymentOrderService paymentOrderService;
    @Autowired
    private PaymentCallbackService paymentCallbackService;

    @PostMapping("/service/payment/order/createPending")
    public PaymentOrderQo createPending(@RequestParam("businessType") String businessType,
                                        @RequestParam("businessOrderNo") String businessOrderNo,
                                        @RequestParam("outTradeNo") String outTradeNo,
                                        @RequestParam("userId") Long userId,
                                        @RequestParam("channel") String channel,
                                        @RequestParam("amountFen") Integer amountFen) {
        PaymentOrder created = paymentOrderService.createPending(businessType, businessOrderNo, outTradeNo,
                userId, channel, amountFen);
        return CopyUtil.copy(created, PaymentOrderQo.class);
    }

    @GetMapping("/service/payment/order/get")
    public PaymentOrderQo get(@RequestParam("outTradeNo") String outTradeNo) {
        return paymentOrderService.findByOutTradeNo(outTradeNo);
    }

    @PostMapping("/service/payment/order/prepared")
    public PaymentOrderQo prepared(@RequestParam("outTradeNo") String outTradeNo,
                                   @RequestParam(value = "channel", required = false) String channel,
                                   @RequestParam(value = "prepayId", required = false) String prepayId,
                                   @RequestParam(value = "h5Url", required = false) String h5Url) {
        return paymentOrderService.updatePrepared(outTradeNo, channel, prepayId, h5Url);
    }

    @PostMapping("/service/payment/markPaid")
    public Boolean markPaid(@RequestParam("outTradeNo") String outTradeNo,
                            @RequestParam("txId") String txId,
                            @RequestParam("amountFen") Integer amountFen,
                            @RequestParam(value = "paidAtMs", required = false) Long paidAtMs,
                            @RequestParam(value = "eventId", required = false) String eventId,
                            @RequestParam(value = "digest", required = false) String digest) {
        Date paidAt = paidAtMs == null ? new Date() : new Date(paidAtMs);
        return paymentCallbackService.markPaid(outTradeNo, txId, amountFen, paidAt, eventId, digest);
    }

    @PostMapping("/service/payment/admin/orders")
    public Map<String, Object> adminOrders(@RequestBody PaymentOrderQo qo) {
        return paymentOrderService.findAdminPage(qo);
    }
}
