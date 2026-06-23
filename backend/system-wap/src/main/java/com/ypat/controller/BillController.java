package com.ypat.controller;

import com.ypat.BillQo;
import com.ypat.service.BillServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BillController {
    @Autowired
    private BillServiceClient billServiceClient;

    @GetMapping("/bill/get")
    public String get(Long id) {
        return billServiceClient.get(id);
    }

    @PostMapping("/bill/add")
    public void add(BillQo billQo){
        billServiceClient.add(billQo);
    }

    @PostMapping("/bill/findPage")
    public String findPage(BillQo billQo) {
        return billServiceClient.findPage(billQo);
    }
}
