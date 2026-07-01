package com.ypat.service;

import com.ypat.WorkComplainQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("SYSTEM-API")
public interface WorkComplainServiceClient {
    @PostMapping("/service/work/complain")
    String complain(@RequestBody WorkComplainQo qo);
}
