package com.ypat.controller;

import com.ypat.MessInfoQo;
import com.ypat.YpatInfoQo;
import com.ypat.service.MessInfoService;
import com.ypat.service.UserService;
import com.ypat.service.UserYpatService;
import com.ypat.service.YpatInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class MypatInfoController {

    @Autowired
    private UserService userService;
    @Autowired
    private MessInfoService messInfoService;
    @Autowired
    private YpatInfoService ypatInfoService;
    @Autowired
    private UserYpatService userYpatService;

    @PutMapping("/service/my/ypat/rec/add")
    public void myRecAdd(@RequestBody MessInfoQo messInfoQo){
        messInfoService.myRecAdd(messInfoQo);
    }

    @PutMapping("/service/my/ypat/sc/add")
    public void myScAdd(Long userid, Long ypatid){
        userService.myColAdd(userid, ypatid);
    }

    @PutMapping("/service/my/ypat/sc/cancel")
    public void myScCancel(Long userid, Long ypatid){
        userService.myColCancel(userid, ypatid);
    }

    @GetMapping("/service/my/ypat/rec/count")
    public Long myRecCount(String type, Long userid) {
        return messInfoService.count(type, userid);
    }

    @GetMapping("/service/my/ypat/unread/count")
    public Long myUnreadCount(Long userid) {
        return messInfoService.countUnread(userid);
    }

    @GetMapping("/service/my/ypat/rec/unread/count")
    public Long myRecUnreadCount(String type, Long userid) {
        return messInfoService.countRecUnread(type, userid);
    }

    @GetMapping("/service/my/ypat/send/unread/count")
    public Long mySendUnreadCount(String type, Long userid) {
        return messInfoService.countSendUnread(type, userid);
    }

    @PostMapping("/service/my/ypat/rec/list")
    public Map<String, Object> myRecList(@RequestBody MessInfoQo messInfoQo) {
        return messInfoService.findPage(messInfoQo);
    }

    @PostMapping("/service/my/ypat/send/list")
    public Map<String, Object> mySendList(@RequestBody MessInfoQo messInfoQo) {
        return messInfoService.findPage(messInfoQo);
    }

    @PostMapping("/service/my/ypat/pub/list")
    public Map<String, Object> myPubList(@RequestBody YpatInfoQo ypatInfoQo) {
        return ypatInfoService.findPage(ypatInfoQo);
    }

    @PostMapping("/service/my/ypat/app/list")
    public Map<String, Object> myAppList(@RequestBody MessInfoQo messInfoQo) {
        return messInfoService.myAppList(messInfoQo);
    }

    @PostMapping("/service/my/ypat/sc/list")
    public Map<String, Object> myScList(@RequestBody YpatInfoQo ypatInfoQo) {
        return userYpatService.myScList(ypatInfoQo);
    }
}
