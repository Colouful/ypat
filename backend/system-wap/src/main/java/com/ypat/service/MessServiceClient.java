package com.ypat.service;

import com.ypat.MessInfoQo;
import com.ypat.YpatInfoQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("SYSTEM-API")
public interface MessServiceClient {

    @GetMapping("/service/mess/get")
    String get(@RequestParam("id") Long id, @RequestParam("userid") Long userid);

    @PostMapping("/service/my/ypat/rec/list")
    String myRecList(@RequestBody MessInfoQo messInfoQo);

    @GetMapping("/service/my/ypat/unread/count")
    String myUnreadCount(@RequestParam("userid") Long userid);

    @GetMapping("/service/my/ypat/rec/unread/count")
    String myRecUnreadCount(@RequestParam("type") String type, @RequestParam("userid") Long userid);

    @GetMapping("/service/my/ypat/send/unread/count")
    String mySendUnreadCount(@RequestParam("type") String type, @RequestParam("userid") Long userid);

    @PostMapping("/service/my/ypat/sc/list")
    String myScList(@RequestBody YpatInfoQo ypatInfoQo);

    @PostMapping("/service/my/ypat/pub/list")
    String myPubList(@RequestBody YpatInfoQo ypatInfoQo);

    @PostMapping("/service/my/ypat/app/list")
    String myAppList(@RequestBody MessInfoQo messInfoQo);

    @PostMapping("/service/my/ypat/send/list")
    String mySendList(@RequestBody MessInfoQo messInfoQo);

    @PostMapping("/service/mess/add")
    String add(@RequestBody MessInfoQo messInfoQo);

}
