package com.ypat.service;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("SYSTEM-API")
public interface WorkDictServiceClient {
    @GetMapping("/service/dict/work-tag")
    String listWorkTags();
}
