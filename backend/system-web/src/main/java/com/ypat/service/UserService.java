package com.ypat.service;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.UserQo;
import com.ypat.model.SecurityUserDetails;
import com.ypat.util.GsonUtils;
import com.ypat.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserServiceClient userServiceClient;

    public SecurityUserDetails manageLogin(UserQo userQo) {
        String userJson = userServiceClient.findByMobile(userQo.getMobile());
        UserQo user = GsonUtils.fromJson(userJson, UserQo.class);
        SecurityUserDetails userDetails = new SecurityUserDetails();
        if (user == null) {
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        String password = MD5Util.encode(userQo.getPassword(),"UTF-8").toUpperCase();
        if (!password.equals(user.getPassword())) {
            throw new SysException(ResponseCode.FAIL_PASSWORD);
        }
        userDetails.setUserId(userQo.getId()+"");
        userDetails.setUsername(userQo.getName());
        userDetails.setMobile(userQo.getMobile());
        return userDetails;
    }
}
