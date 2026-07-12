package com.ypat.service;

import com.ypat.OrderQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("SYSTEM-API")
public interface RealnameOrderServiceClient {

    @PostMapping("/service/order/addRealnamePayment")
    void addRealnamePayment(@RequestBody OrderQo orderQo);
}
