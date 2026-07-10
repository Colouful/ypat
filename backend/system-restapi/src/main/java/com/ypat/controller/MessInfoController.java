package com.ypat.controller;

import com.ypat.MessInfoQo;
import com.ypat.MessagePushLogQo;
import com.ypat.service.MessInfoService;
import com.ypat.service.MessagePushLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class MessInfoController {
    @Autowired
    private MessInfoService messInfoService;
    @Autowired
    private MessagePushLogService messagePushLogService;

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

    @PostMapping("/service/message-push-log/record")
    public void recordPushLog(@RequestBody MessagePushLogQo qo) {
        messagePushLogService.record(qo);
    }

    @PostMapping("/service/message-push-log/findPage")
    public Map<String, Object> findPushLogPage(@RequestBody MessagePushLogQo qo) {
        return messagePushLogService.findPage(qo);
    }

    @PostMapping("/service/message-push-log/stats")
    public MessagePushLogQo pushLogStats(@RequestBody MessagePushLogQo qo) {
        return messagePushLogService.stats(qo);
    }
}
