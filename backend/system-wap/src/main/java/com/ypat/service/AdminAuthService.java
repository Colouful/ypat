package com.ypat.service;

import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.UserQo;
import com.ypat.model.SecurityUserDetails;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.util.JwtTokenUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理端登录服务。
 *
 * <p>复用旧后台 system-web 的 ManageLogin 逻辑（手机号 + MD5 大写密码比对），
 * 生成 JWT Token 使用 system-wap 的 {@link JwtTokenUtil}。</p>
 */
@Service
public class AdminAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthService.class);

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private AdminCaptchaService captchaService;

    /**
     * 管理端账号密码登录。
     *
     * @param mobile    手机号
     * @param password  明文密码
     * @param captchaId 验证码 ID
     * @param captchaCode 用户输入的验证码
     * @return ResponseApiBody 包含 token 和管理员信息
     */
    public ResponseApiBody login(String mobile, String password, String captchaId, String captchaCode) {
        // 参数校验
        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(password)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "手机号和密码不能为空");
        }

        // 验证码校验
        captchaService.verify(captchaId, captchaCode);

        // 通过微服务查询用户
        String userJson = userServiceClient.findByMobile(mobile);
        UserQo user = GsonUtils.fromJson(userJson, UserQo.class);

        if (user == null || user.getId() == null) {
            logger.warn("管理端登录失败：用户不存在，mobile={}", maskMobile(mobile));
            throw new SysException(ResponseCode.FAIL_NOT);
        }

        // MD5 大写密码比对（与旧后台 system-web UserService.manageLogin 一致）
        String encodedPassword = md5UpperCase(password, "UTF-8");
        if (user.getPassword() == null || !user.getPassword().equalsIgnoreCase(encodedPassword)) {
            logger.warn("管理端登录失败：密码错误，mobile={}", maskMobile(mobile));
            throw new SysException(ResponseCode.FAIL_PASSWORD);
        }

        // 生成 JWT Token
        SecurityUserDetails userDetails = new SecurityUserDetails();
        userDetails.setUserId(user.getId().toString());
        userDetails.setUsername(StringUtils.defaultIfBlank(user.getName(), user.getMobile()));
        userDetails.setMobile(user.getMobile());

        String token = jwtTokenUtil.generateToken(userDetails);

        Map<String, Object> res = new HashMap<>(8);
        res.put("token", token);
        res.put("id", user.getId());
        res.put("mobile", user.getMobile());
        res.put("name", user.getName());
        res.put("nickname", user.getNickname());

        logger.info("管理端登录成功：mobile={}", maskMobile(mobile));
        return ResponseApiBody.success(res);
    }

    /**
     * 获取当前登录管理员信息。
     *
     * @param token JWT Token
     * @return ResponseApiBody 包含管理员信息
     */
    public ResponseApiBody getUserInfo(String token) {
        if (StringUtils.isBlank(token)) {
            throw new SysException(ResponseCode.FAIL_NET);
        }

        if (!jwtTokenUtil.validateToken(token)) {
            throw new SysException(ResponseCode.FAIL_NET);
        }

        String userId = jwtTokenUtil.getUserFromToken(token);
        if (StringUtils.isBlank(userId)) {
            throw new SysException(ResponseCode.FAIL_NET);
        }

        String userJson = userServiceClient.get(Long.parseLong(userId));
        UserQo user = GsonUtils.fromJson(userJson, UserQo.class);

        if (user == null || user.getId() == null) {
            throw new SysException(ResponseCode.FAIL_NOT);
        }

        Map<String, Object> res = new HashMap<>(8);
        res.put("id", user.getId());
        res.put("mobile", user.getMobile());
        res.put("name", user.getName());
        res.put("nickname", user.getNickname());

        return ResponseApiBody.success(res);
    }

    /**
     * MD5 加密并转大写（与旧后台 system-web MD5Util.encode + toUpperCase 一致）。
     */
    private String md5UpperCase(String origin, String charsetName) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest;
            if (charsetName == null || charsetName.isEmpty()) {
                digest = md.digest(origin.getBytes());
            } else {
                digest = md.digest(origin.getBytes(charsetName));
            }
            return Hex.encodeHexString(digest).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding: " + charsetName, e);
        }
    }

    /**
     * 手机号脱敏（保留前3后4）。
     */
    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return "***";
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
