package com.ypat.service;

import com.ypat.OrderQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("SYSTEM-API")
public interface OrderServiceClient {

    @GetMapping("/service/order/get")
    String get(@RequestParam("id") Long id);

    @GetMapping("/service/order/count")
    String count(@RequestParam("userid") Long userid, @RequestParam("type") String type);

    @PostMapping("/service/order/findPage")
    String findPage(@RequestBody OrderQo orderQo);

    @PostMapping("/service/order/add")
    String add(@RequestBody OrderQo orderQo);

}
