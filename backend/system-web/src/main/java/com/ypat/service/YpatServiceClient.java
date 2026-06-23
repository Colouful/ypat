package com.ypat.service;

import com.ypat.YpatInfoQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("SYSTEM-API")
public interface YpatServiceClient {

    @GetMapping("/service/ypat/get")
    String get(@RequestParam("id") Long id, @RequestParam("userid") Long userid);

    @PostMapping("/service/ypat/findPage")
    String findPage(@RequestBody YpatInfoQo ypatInfoQo);

    @PostMapping("/service/ypat/audit")
    String audit(@RequestParam("id") Long id, @RequestParam("flag") String flag,
                 @RequestParam("recomflag") String recomflag,
                 @RequestParam("reason") String reason);

    @PostMapping("/service/ypat/upRecom")
    String upRecom(@RequestParam("id") Long id, @RequestParam("recomflag") String recomflag);

    @PostMapping("/service/ypat/submit")
    String submit(@RequestBody YpatInfoQo ypatInfoQo);
}
