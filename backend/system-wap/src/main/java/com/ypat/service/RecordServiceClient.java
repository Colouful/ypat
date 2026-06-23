package com.ypat.service;

import com.ypat.RecordQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("SYSTEM-API")
public interface RecordServiceClient {

    @GetMapping("/service/record/get")
    String get(@RequestParam("id") Long id);

    @PostMapping("/service/record/findPage")
    String findPage(@RequestBody RecordQo recordQo);

    @PostMapping("/service/record/add")
    String add(@RequestBody RecordQo recordQo);

}
