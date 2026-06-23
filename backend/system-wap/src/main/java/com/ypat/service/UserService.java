package com.ypat.service;

import com.ypat.UserQo;
import com.ypat.enums.UserOrigType;
import com.ypat.model.SecurityUserDetails;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.third.bdlogin.BdUtils;
import com.ypat.third.wxlogin.WxUtils;
import com.ypat.util.JwtTokenUtil;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService implements UserDetailsService {

    private static Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SecurityUserDetails us = new SecurityUserDetails();
        us.setUsername(username);
        return us;
    }

    public Map<String, String> login(UserQo userQo) {
        JSONObject userInfo = null;
        String mobile = null;
        if(UserOrigType.wx.value.equals(userQo.getChannel())
            ||UserOrigType.pc.value.equals(userQo.getChannel())) {
            userInfo = WxUtils.getUserInfo(userQo.getEncryptedData(), userQo.getSessionKey(), userQo.getIv());
            try {
                mobile = userInfo.getString("phoneNumber");
                userQo.setMobile(mobile);
            } catch (JSONException e) {
                logger.error("获取微信加密信息异常", e);
                throw new RuntimeException("获取加密信息异常");
            }
        } else if (UserOrigType.bd.value.equals(userQo.getChannel())) {
            userInfo = BdUtils.getUserInfo(userQo.getEncryptedData(), userQo.getSessionKey());
            try {
                mobile = userInfo.getString("mobile");
                userQo.setMobile(mobile);
            } catch (JSONException e) {
                logger.error("获取百度加密信息异常", e);
                throw new RuntimeException("获取加密信息异常");
            }
        } else {
            //
        }

        SecurityUserDetails userDetails = new SecurityUserDetails();
        //查询数据库
        String userJson = userServiceClient.findByMobile(mobile);
        UserQo user = GsonUtils.fromJson(userJson, UserQo.class);
        if (user != null) {
            userDetails.setUserId(user.getId()+"");
            userDetails.setUsername(user.getName());
            userDetails.setMobile(user.getMobile());
            //更新为微信openid
            //更新为微信openid
            if(StringUtils.isNotEmpty(userQo.getOpenid())){
                UserQo newUserQo = new UserQo();
                newUserQo.setId(user.getId());
                newUserQo.setOpenid(userQo.getOpenid());
                userServiceClient.upd(newUserQo);
            }
        }else{
            userJson = userServiceClient.add(userQo);
            user = GsonUtils.fromJson(userJson, UserQo.class);
            userDetails.setUserId(user.getId()+"");
            userDetails.setUsername(user.getName());
            userDetails.setMobile(user.getMobile());
        }
        final String token = jwtTokenUtil.generateToken(userDetails);
        Map<String, String> res = new HashMap();
        res.put("token", token);
        res.put("id", user.getId()+"");
        res.put("mobile", user.getMobile());
        res.put("nickname", user.getNickname());
        res.put("gender", user.getGender());
        res.put("profess", user.getProfess());
        return res;
    }

    public Map<String, String> getToken(UserQo userQo) {
        SecurityUserDetails userDetails = new SecurityUserDetails();
        //查询数据库
        String userJson = userServiceClient.findByMobile(userQo.getMobile());
        UserQo user = GsonUtils.fromJson(userJson, UserQo.class);
        userDetails.setUserId(user.getId()+"");
        userDetails.setUsername(user.getName());
        userDetails.setMobile(user.getMobile());
        final String token = jwtTokenUtil.generateToken(userDetails);
        Map<String, String> res = new HashMap();
        res.put("token", token);
        res.put("id", user.getId()+"");
        res.put("mobile", user.getMobile());
        res.put("nickname", user.getNickname());
        res.put("gender", user.getGender());
        res.put("profess", user.getProfess());
        return res;
    }

}
