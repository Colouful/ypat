package com.ypat.service;

import com.ypat.OauthQo;
import com.ypat.UserQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("SYSTEM-API")
public interface OauthServiceClient {

    @GetMapping("/service/oauth/get")
    String get(@RequestParam("id") Long id);

    @PutMapping("/service/oauth/add")
    String add(@RequestBody OauthQo oauthQo);

    @PostMapping("/service/oauth/audit")
    String audit(@RequestParam("id") Long id, @RequestParam("flag") String flag);

    @GetMapping("/service/oauth/getAuth")
    String getAuth(@RequestParam("id") Long id);
}
