package com.ypat.controller;

import com.ypat.DepositConfigQo;
import com.ypat.DepositOrderQo;
import com.ypat.ResponseApiBody;
import com.ypat.service.DepositServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/deposit")
public class AdminDepositController {

    @Autowired
    private DepositServiceClient depositServiceClient;

    @GetMapping("/config")
    public ResponseApiBody config() {
        return ResponseApiBody.success(depositServiceClient.config());
    }

    @PutMapping("/config")
    public ResponseApiBody saveConfig(@RequestBody DepositConfigQo qo) {
        return ResponseApiBody.success(depositServiceClient.saveConfig(qo));
    }

    @GetMapping("/orders")
    public ResponseApiBody orders(DepositOrderQo qo) {
        return ResponseApiBody.success(depositServiceClient.adminOrders(qo));
    }
}
