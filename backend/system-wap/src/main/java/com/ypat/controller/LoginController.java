package com.ypat.controller;

import com.ypat.ResponseCode;
import com.ypat.ResponseApiBody;
import com.ypat.SysException;
import com.ypat.UserQo;
import com.ypat.enums.UserOrigType;
import com.ypat.service.UserService;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.third.bdlogin.BdClient;
import com.ypat.util.UserUtil;
import com.ypat.third.wxpay.sdk.WXPayClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LoginController {

    private static Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private WXPayClient wxPayClient;
    @Autowired
    private BdClient bdClient;

    /***************微信授权登录*****************/
    @GetMapping("/user/code")
    public ResponseApiBody code(String code) {
        if (StringUtils.isEmpty(code)) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        String wxResult = wxPayClient.code2Session(code);
        if (StringUtils.isEmpty(wxResult)) {
            throw new SysException(ResponseCode.FAIL_WX);
        }
        Map<String, Object> wxResultMap = GsonUtils.fromJson(wxResult, Map.class);
        if (wxResultMap != null && wxResultMap.get("errcode") != null) {
            String errMsg = String.valueOf(wxResultMap.get("errmsg"));
            return new ResponseApiBody(ResponseCode.FAIL_WX.getCode(), "微信登录失败：" + errMsg, null);
        }
        return ResponseApiBody.success(wxResult);
    }

    @PostMapping("/user/sms/code")
    public String smsCode(String mobile) {
        return GsonUtils.toJson(userService.sendH5LoginCode(mobile));
    }

    @PostMapping("/user/login")
    public String login(UserQo userQo) {
        logger.info("登录输入："+userQo);
        if(StringUtils.isEmpty(userQo.getChannel())) {
            userQo.setChannel(UserOrigType.wx.value);
        }
        Map<String, String> mapRes = userService.login(userQo);
        logger.info("登录输出："+userQo);
        return GsonUtils.toJson(mapRes);
    }

    @GetMapping("/user/token")
    public String code(UserQo userQo) {
        return GsonUtils.toJson(userService.getToken(userQo, UserUtil.getUserId()));
    }

    /***************百度授权登录*****************/
    @PostMapping("/bd/code")
    public String bdCode(String code) {
        return GsonUtils.toJson(bdClient.code2Session(code));
    }

    @PostMapping("/bd/login")
    public String bdLogin(UserQo userQo) {
        userQo.setChannel(UserOrigType.bd.value);
        logger.info("登录输入："+userQo);
        Map<String, String> mapRes = userService.login(userQo);
        logger.info("登录输出："+userQo);
        return GsonUtils.toJson(mapRes);
    }
}
