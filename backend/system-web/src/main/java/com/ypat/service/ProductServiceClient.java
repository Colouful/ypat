package com.ypat.service;

import com.ypat.ProductQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("SYSTEM-API")
public interface ProductServiceClient {

    @GetMapping("/service/product/get")
    String get(@RequestParam("id") Long id);

    @PostMapping("/service/product/findPage")
    String findPage(@RequestBody ProductQo productQo);

    @PostMapping("/service/product/add")
    String add(@RequestBody ProductQo productQo);

    @PostMapping("/service/product/upDown")
    String upDown(@RequestBody ProductQo productQo);

}
