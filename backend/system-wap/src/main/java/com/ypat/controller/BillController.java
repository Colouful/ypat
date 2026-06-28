package com.ypat.controller;

import com.ypat.BillQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.BillServiceClient;
import com.ypat.util.UserUtil;
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
        throw new SysException(ResponseCode.FAIL_VAL);
    }

    @PostMapping("/bill/add")
    public void add(BillQo billQo){
        throw new SysException(ResponseCode.FAIL_VAL);
    }

    @PostMapping("/bill/findPage")
    public String findPage(BillQo billQo) {
        billQo.setUserid(Long.parseLong(UserUtil.getUserId()));
        return billServiceClient.findPage(billQo);
    }
}
