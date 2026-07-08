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

    @PostMapping("/service/ypat/add")
    String add(@RequestBody YpatInfoQo ypatInfoQo);

    @PostMapping("/service/ypat/submit")
    void submit(@RequestBody YpatInfoQo ypatInfoQo);

    @PostMapping("/service/ypat/audit")
    String audit(@RequestParam("id") Long id, @RequestParam("flag") String flag,
                 @RequestParam("recomflag") String recomflag,
                 @RequestParam("reason") String reason);

    @PostMapping("/service/ypat/upRecom")
    String upRecom(@RequestParam("id") Long id, @RequestParam("recomflag") String recomflag);

    @DeleteMapping("/service/ypat/del")
    String del(@RequestParam("id") Long id);

    @PutMapping("/service/ypat/yd/add")
    String readAdd(@RequestParam("id") Long id);

    @PutMapping("/service/ypat/yp/add")
    String ypatAdd(@RequestParam("id") Long id);

    @PutMapping("/service/ypat/sc/add")
    String collAdd(@RequestParam("id") Long id);

}
