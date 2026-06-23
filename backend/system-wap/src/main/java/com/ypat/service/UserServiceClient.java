package com.ypat.service;

import com.ypat.MessInfoQo;
import com.ypat.UserQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("SYSTEM-API")
public interface UserServiceClient {

    @GetMapping("/service/user/get")
    String get(@RequestParam("id") Long id);

    @GetMapping("/service/user/findByMobile")
    String findByMobile(@RequestParam("mobile") String mobile);

    @GetMapping("/service/user/linkway/get")
    String linkway(@RequestParam("id") Long id, @RequestParam("userid") Long userid, @RequestParam("messid") Long messid);

    @PostMapping(value="/service/user/add")
    String add(@RequestBody UserQo userQo);

    @PutMapping(value="/service/user/upd")
    String upd(@RequestBody UserQo userQo);

    @PostMapping("/service/user/findPage")
    String findPage(@RequestBody UserQo userQo);

    @PutMapping("/service/my/ypat/rec/add")
    String myRecAdd(@RequestBody MessInfoQo messInfoQo);

    @PutMapping("/service/my/ypat/sc/add")
    String myScAdd(@RequestParam("userid") Long userid, @RequestParam("ypatid") Long ypatid);

    @GetMapping("/service/user/findByCityAndProfess")
    String findByCityAndProfess(@RequestParam("userid") Long userid, @RequestParam("city") String city);
}
