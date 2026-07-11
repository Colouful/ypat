package com.ypat.service;

import com.ypat.OrderQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("SYSTEM-API")
public interface PpdOrderServiceClient {

    @PostMapping("/service/order/addPpdPayment")
    void addPpdPayment(@RequestBody OrderQo orderQo);
}
