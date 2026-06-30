package com.ypat.controller;

import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.AdminAuthService;
import com.ypat.service.AdminCaptchaService;
import com.ypat.util.JwtTokenUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理端认证 Controller。
 *
 * <p>提供管理端登录、登出、验证码、用户信息接口。
 * 复用 system-wap 现有 JWT 认证体系（Header: Token, HS512 签名）。</p>
 */
@RestController
@RequestMapping("/admin")
public class AdminAuthController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthController.class);

    @Autowired
    private AdminAuthService adminAuthService;

    @Autowired
    private AdminCaptchaService captchaService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 获取图片验证码。
     *
     * @return ResponseApiBody 包含 captchaId 和 base64 图片
     */
    @GetMapping("/captcha")
    public ResponseApiBody captcha() {
        return captchaService.generate();
    }

    /**
     * 管理端登录。
     *
     * @param body 包含 mobile, password, captchaId, captchaCode
     * @return ResponseApiBody 包含 token 和管理员信息
     */
    @PostMapping("/login")
    public ResponseApiBody login(@RequestBody Map<String, String> body) {
        String mobile = body.get("mobile");
        String password = body.get("password");
        String captchaId = body.get("captchaId");
        String captchaCode = body.get("captchaCode");

        return adminAuthService.login(mobile, password, captchaId, captchaCode);
    }

    /**
     * 获取当前登录管理员信息。
     *
     * @param request HTTP 请求（从 Header 获取 Token）
     * @return ResponseApiBody 包含管理员信息
     */
    @GetMapping("/user/info")
    public ResponseApiBody userInfo(HttpServletRequest request) {
        String token = request.getHeader("Token");
        return adminAuthService.getUserInfo(token);
    }

    /**
     * 管理端退出登录。
     *
     * <p>JWT 无状态，前端清除 Token 即可。后端记录日志。</p>
     *
     * @param request HTTP 请求
     * @return ResponseApiBody 成功
     */
    @PostMapping("/logout")
    public ResponseApiBody logout(HttpServletRequest request) {
        String token = request.getHeader("Token");
        if (StringUtils.isNotBlank(token)) {
            String userId = jwtTokenUtil.getUserFromToken(token);
            logger.info("管理端退出登录：userId={}", userId);
        }
        Map<String, Object> res = new HashMap<>(2);
        res.put("success", true);
        return ResponseApiBody.success(res);
    }
}
