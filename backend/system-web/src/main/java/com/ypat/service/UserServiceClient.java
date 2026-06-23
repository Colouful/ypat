package com.ypat.service;

import com.ypat.UserQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("SYSTEM-API")
public interface UserServiceClient {
    @PostMapping(value="/service/user/add")
    String add(@RequestBody UserQo userQo);

    @RequestMapping("/service/user/get")
    String get(@RequestParam("id") Long id);

    @GetMapping("/service/user/findByMobile")
    String findByMobile(@RequestParam("mobile") String mobile);

    @PostMapping("/service/user/findPage")
    String findPage(@RequestBody UserQo userQo);

    @GetMapping("/service/user/findByCityAndProfess")
    String findByCityAndProfess(@RequestParam("userid") Long userid, @RequestParam("city") String city);
}
