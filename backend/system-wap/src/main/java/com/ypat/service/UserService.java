package com.ypat.service;

import com.ypat.UserQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.enums.UserOrigType;
import com.ypat.model.SecurityUserDetails;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.third.bdlogin.BdUtils;
import com.ypat.third.wxlogin.WxUtils;
import com.ypat.util.JwtTokenUtil;
import com.ypat.util.RedisClient;
import com.ypat.util.SmsUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class UserService implements UserDetailsService {

    private static Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final String H5_SMS_KEY_PREFIX = "h5:login:sms:";
    private static final long H5_SMS_EXPIRES_SECONDS = 300L;
    private static final String H5_TEST_MOBILE = "18888888888";
    private static final String H5_TEST_SMS_CODE = "888888";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private RedisClient redisClient;
    @Autowired(required = false)
    private Environment environment;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SecurityUserDetails us = new SecurityUserDetails();
        us.setUsername(username);
        return us;
    }

    public Map<String, String> login(UserQo userQo) {
        if (isH5PhoneLogin(userQo)) {
            return loginByMobileCode(userQo);
        }

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

        return loginOrCreateUser(userQo, mobile);
    }

    public Map<String, String> sendH5LoginCode(String mobile) {
        validateMobile(mobile);
        if (isH5TestLoginCode(mobile, H5_TEST_SMS_CODE)) {
            Map<String, String> res = new HashMap();
            res.put("mobile", mobile);
            res.put("expiresIn", String.valueOf(H5_SMS_EXPIRES_SECONDS));
            res.put("debugCode", H5_TEST_SMS_CODE);
            return res;
        }

        String code = createSmsCode();
        boolean sent = SmsUtils.sendLoginCode(mobile, code);
        boolean debugMode = isDebugLoginCodeEnabled();
        if (!sent && !debugMode) {
            throw new RuntimeException("短信服务暂不可用，请稍后再试");
        }
        redisClient.put(buildH5SmsKey(mobile), code, H5_SMS_EXPIRES_SECONDS);

        Map<String, String> res = new HashMap();
        res.put("mobile", mobile);
        res.put("expiresIn", String.valueOf(H5_SMS_EXPIRES_SECONDS));
        if (debugMode) {
            res.put("debugCode", code);
        }
        return res;
    }

    private Map<String, String> loginByMobileCode(UserQo userQo) {
        String mobile = userQo.getMobile();
        validateMobile(mobile);
        String smsCode = userQo.getSmsCode();
        if (StringUtils.isBlank(smsCode)) {
            throw new RuntimeException("请输入短信验证码");
        }

        boolean testLoginCode = isH5TestLoginCode(mobile, smsCode);
        if (testLoginCode) {
            userQo.setChannel(UserOrigType.pc.value);
            userQo.setMobile(mobile);
            return loginOrCreateUser(userQo, mobile);
        }

        String smsKey = buildH5SmsKey(mobile);
        Object cachedCode = redisClient.get(smsKey);
        if (cachedCode == null || !smsCode.equals(String.valueOf(cachedCode))) {
            throw new RuntimeException("验证码错误或已过期");
        }
        redisClient.removeForExpire(smsKey);
        userQo.setChannel(UserOrigType.pc.value);
        userQo.setMobile(mobile);
        return loginOrCreateUser(userQo, mobile);
    }

    private boolean isH5PhoneLogin(UserQo userQo) {
        return userQo != null
                && UserOrigType.pc.value.equals(userQo.getChannel())
                && StringUtils.isNotBlank(userQo.getMobile())
                && StringUtils.isNotBlank(userQo.getSmsCode())
                && StringUtils.isBlank(userQo.getEncryptedData())
                && StringUtils.isBlank(userQo.getSessionKey())
                && StringUtils.isBlank(userQo.getIv());
    }

    private void validateMobile(String mobile) {
        if (StringUtils.isBlank(mobile) || !MOBILE_PATTERN.matcher(mobile).matches()) {
            throw new RuntimeException("请输入正确的手机号");
        }
    }

    private String buildH5SmsKey(String mobile) {
        return H5_SMS_KEY_PREFIX + mobile;
    }

    private String createSmsCode() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            builder.append(SECURE_RANDOM.nextInt(10));
        }
        return builder.toString();
    }

    private boolean isDebugLoginCodeEnabled() {
        if ("true".equalsIgnoreCase(System.getenv("YPAT_H5_LOGIN_DEBUG_CODE"))) {
            return true;
        }
        if (environment == null) {
            return true;
        }
        return environment.acceptsProfiles("dev");
    }

    private boolean isH5TestLoginCode(String mobile, String smsCode) {
        return H5_TEST_MOBILE.equals(mobile) && H5_TEST_SMS_CODE.equals(smsCode);
    }

    private Map<String, String> loginOrCreateUser(UserQo userQo, String mobile) {
        SecurityUserDetails userDetails = new SecurityUserDetails();
        //查询数据库
        String userJson = userServiceClient.findByMobile(mobile);
        UserQo user = GsonUtils.fromJson(userJson, UserQo.class);
        if (user != null) {
            userDetails.setUserId(user.getId()+"");
            userDetails.setUsername(StringUtils.defaultIfBlank(user.getName(), user.getMobile()));
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
            userDetails.setUsername(StringUtils.defaultIfBlank(user.getName(), user.getMobile()));
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
        return getToken(userQo, null);
    }

    public Map<String, String> getToken(UserQo userQo, String authenticatedUserId) {
        if (StringUtils.isBlank(authenticatedUserId)) {
            throw new SysException(ResponseCode.FAIL_AUTH);
        }
        SecurityUserDetails userDetails = new SecurityUserDetails();
        String userJson = userServiceClient.get(Long.parseLong(authenticatedUserId));
        UserQo user = GsonUtils.fromJson(userJson, UserQo.class);
        if (user == null || user.getId() == null) {
            throw new SysException(ResponseCode.FAIL_AUTH);
        }
        userDetails.setUserId(user.getId()+"");
        userDetails.setUsername(StringUtils.defaultIfBlank(user.getName(), user.getMobile()));
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
