package com.ypat.controller;

import com.ypat.MessInfoQo;
import com.ypat.enums.MessType;
import com.ypat.service.MessServiceClient;
import com.ypat.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessInfoController {
    @Autowired
    private MessServiceClient messServiceClient;

    @GetMapping("/mess/get")
    public String get(Long id) {
        long userid = Long.parseLong(UserUtil.getUserId());
        return messServiceClient.get(id, userid);
    }

    @PutMapping("/mess/yta")
    public void add(MessInfoQo messInfoQo){
        messInfoQo.setSendperid(Long.parseLong(UserUtil.getUserId()));
        messServiceClient.add(messInfoQo);
    }

}
