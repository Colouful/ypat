package com.ypat.controller;

import com.ypat.PaymentOrderQo;
import com.ypat.ResponseApiBody;
import com.ypat.service.PaymentOrderServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/payment")
public class AdminPaymentController {

    @Autowired
    private PaymentOrderServiceClient paymentOrderServiceClient;

    @GetMapping("/orders")
    public ResponseApiBody orders(PaymentOrderQo qo) {
        return ResponseApiBody.success(paymentOrderServiceClient.adminOrders(qo));
    }
}
