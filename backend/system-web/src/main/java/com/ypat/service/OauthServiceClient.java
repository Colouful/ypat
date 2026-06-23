package com.ypat.service;

import com.ypat.OauthQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("SYSTEM-API")
public interface OauthServiceClient {
    @GetMapping("/service/oauth/getAuth")
    String getAuth(@RequestParam("id") Long id);

    @PostMapping("/service/oauth/audit")
    String audit(@RequestParam("id") Long id, @RequestParam("flag") String flag);
}
