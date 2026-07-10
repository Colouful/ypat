package com.ypat.service;

import com.ypat.MessInfoQo;
import com.ypat.MessagePushLogQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("SYSTEM-API")
public interface MessServiceClient {

    @GetMapping("/service/mess/get")
    String get(@RequestParam("id") Long id, @RequestParam("userid") Long userid);

    @PostMapping("/service/mess/findPage")
    String findPage(@RequestBody MessInfoQo messInfoQo);

    @PostMapping("/service/message-push-log/record")
    void recordPushLog(@RequestBody MessagePushLogQo qo);

}
