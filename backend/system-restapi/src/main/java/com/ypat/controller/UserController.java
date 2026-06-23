package com.ypat.controller;

import com.ypat.UserLinkWayQo;
import com.ypat.UserQo;
import com.ypat.entity.User;
import com.ypat.service.UserService;
import com.ypat.util.CopyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/service/user/get")
    public UserQo get(Long id) {
        return userService.findById(id);
    }

    @GetMapping("/service/user/linkway/get")
    public UserLinkWayQo linkway(Long id, Long userid, Long messid) {
        return userService.getLinkWay(id, userid, messid);
    }

    @RequestMapping("/service/user/findByMobile")
    public UserQo findByMobile(String mobile) {
        return userService.findByMobile(mobile);
    }

    @PostMapping("/service/user/add")
    public UserQo add(@RequestBody UserQo userQo){
        return userService.save(userQo);
    }

    @PutMapping("/service/user/upd")
    public void upd(@RequestBody UserQo userQo){
        userService.save(userQo);
    }

    @PostMapping("/service/user/findPage")
    public Map<String, Object> findPage(@RequestBody UserQo userQo) {
        return userService.findPage(userQo);
    }

    @RequestMapping("/service/user/findByCityAndProfess")
    public Map<String, Object> findByCityAndProfess(Long userid, String city) {
        return userService.findByCityAndProfess(userid, city);
    }
}

