package com.ypat.controller;

import com.ypat.RecordQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.RecordServiceClient;
import com.ypat.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecordController {
    @Autowired
    private RecordServiceClient recordServiceClient;

    @GetMapping("/record/get")
    public String get(Long id) {
        throw new SysException(ResponseCode.FAIL_VAL);
    }

    @PostMapping("/record/add")
    public void add(RecordQo recordQo){
        throw new SysException(ResponseCode.FAIL_VAL);
    }

    @GetMapping("/record/findPage")
    public String findPage(RecordQo recordQo) {
        recordQo.setUserid(Long.parseLong(UserUtil.getUserId()));
        return recordServiceClient.findPage(recordQo);
    }
}
