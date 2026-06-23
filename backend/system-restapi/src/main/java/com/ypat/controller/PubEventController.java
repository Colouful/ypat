package com.ypat.controller;

import com.ypat.PubEventQo;
import com.ypat.entity.PubEvent;
import com.ypat.service.PubEventService;
import com.ypat.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PubEventController {
    @Autowired
    private PubEventService pubEventService;

    @PostMapping("/service/pubEvent/add")
    public void add(@RequestBody PubEventQo pubEventQo){
        PubEvent pubEvent = CopyUtil.copy(pubEventQo, PubEvent.class);
        pubEventService.save(pubEvent);
    }

    @PostMapping("/service/pubEvent/findPage")
    public Map<String, Object> findPage(@RequestBody PubEventQo pubEventQo) {
        return pubEventService.findPage(pubEventQo);
    }
}
