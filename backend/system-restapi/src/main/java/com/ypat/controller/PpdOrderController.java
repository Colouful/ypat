package com.ypat.controller;

import com.ypat.OrderQo;
import com.ypat.entity.Order;
import com.ypat.service.OrderService;
import com.ypat.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PpdOrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/service/order/addPpdPayment")
    public void addPpdPayment(@RequestBody OrderQo orderQo) {
        Order order = CopyUtil.copy(orderQo, Order.class);
        orderService.savePpdPaymentOrder(order);
    }
}
