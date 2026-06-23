package com.ypat.service;

import com.ypat.BillQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("SYSTEM-API")
public interface BillServiceClient {

    @GetMapping("/service/bill/get")
    String get(@RequestParam("id") Long id);

    @PostMapping("/service/bill/findPage")
    String findPage(@RequestBody BillQo billQo);

    @PostMapping("/service/bill/add")
    String add(@RequestBody BillQo billQo);

}
