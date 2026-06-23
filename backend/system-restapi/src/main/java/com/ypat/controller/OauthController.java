package com.ypat.controller;

import com.ypat.OauthQo;
import com.ypat.entity.User;
import com.ypat.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OauthController {

    private static Logger logger = LoggerFactory.getLogger(OauthController.class);

    @Autowired
    private UserService userService;

    @PutMapping("/service/oauth/add")
    public void add(@RequestBody OauthQo oauthQo) {
        userService.oauth(oauthQo);
    }

    @GetMapping("/service/oauth/get")
    public Map<String, Object> getStatus(Long id) {
        User user = userService.get(id);
        Map<String, Object> map = new HashMap<>();
        map.put("status", user.getStatus());
        return map;
    }

    @GetMapping("/service/oauth/getAuth")
    public OauthQo getAuth(Long id) {
        return userService.getAuth(id);
    }

    @PostMapping("/service/oauth/audit")
    public void audit(Long id, String flag){
        userService.audit(id, flag);
    }
}
