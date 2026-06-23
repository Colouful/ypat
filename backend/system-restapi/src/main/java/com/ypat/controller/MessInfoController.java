package com.ypat.controller;

import com.ypat.MessInfoQo;
import com.ypat.entity.MessInfo;
import com.ypat.service.MessInfoService;
import com.ypat.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MessInfoController {
    @Autowired
    private MessInfoService messInfoService;

    @GetMapping("/service/mess/get")
    public MessInfoQo get(Long id, Long userid) {
        return messInfoService.findById(id, userid);
    }

    @PutMapping("/service/mess/add")
    public void add(@RequestBody MessInfoQo messInfoQo){
        //messInfoService.save(messInfoQo);
    }

    @PostMapping("/service/mess/findPage")
    public Map<String, Object> findPage(@RequestBody MessInfoQo messInfoQo) {
        return messInfoService.findPage(messInfoQo);
    }
}
