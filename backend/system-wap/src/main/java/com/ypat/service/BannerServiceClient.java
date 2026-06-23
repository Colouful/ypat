package com.ypat.service;

import com.ypat.BannerQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("SYSTEM-API")
public interface BannerServiceClient {

    @GetMapping("/service/banner/get")
    String get(@RequestParam("id") Long id);

    @PostMapping("/service/banner/findPage")
    String findPage(@RequestBody BannerQo bannerQo);

}
