package com.ypat.service;

import com.ypat.PubEventQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("SYSTEM-API")
public interface PubEventServiceClient {

    @PostMapping("/service/pubEvent/findPage")
    String findPage(@RequestBody PubEventQo pubEventQo);

    @PostMapping("/service/pubEvent/add")
    String add(@RequestBody PubEventQo pubEventQo);

}
