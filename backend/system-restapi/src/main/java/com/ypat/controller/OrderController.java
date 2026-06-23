package com.ypat.controller;

import com.ypat.OrderQo;
import com.ypat.entity.Order;
import com.ypat.service.OrderService;
import com.ypat.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/service/order/get")
    public OrderQo get(Long id) {
        return orderService.findById(id);
    }

    @GetMapping("/service/order/count")
    public int count(Long userid, String type) {
        return orderService.countByUseridAndType(userid, type);
    }

    @PostMapping("/service/order/add")
    public void add(@RequestBody OrderQo orderQo){
        Order order = CopyUtil.copy(orderQo, Order.class);
        orderService.save(order);
    }

    @PostMapping("/service/order/findPage")
    public Map<String, Object> findPage(@RequestBody OrderQo orderQo) {
        return orderService.findPage(orderQo);
    }
}
