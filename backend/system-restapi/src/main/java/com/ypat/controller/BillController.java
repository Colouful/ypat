package com.ypat.controller;

import com.ypat.BillQo;
import com.ypat.entity.Bill;
import com.ypat.service.BillService;
import com.ypat.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class BillController {
    @Autowired
    private BillService billService;

    @GetMapping("/service/bill/get")
    public BillQo get(Long id) {
        return billService.findById(id);
    }

    @PostMapping("/service/bill/add")
    public void add(@RequestBody BillQo billQo){
        Bill bill = CopyUtil.copy(billQo, Bill.class);
        billService.save(bill);
    }

    @PostMapping("/service/bill/findPage")
    public Map<String, Object> findPage(@RequestBody BillQo billQo) {
        return billService.findPage(billQo);
    }
}
