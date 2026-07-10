package com.ypat.controller;

import com.ypat.OauthQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.UserQo;
import com.ypat.comm.Const;
import com.ypat.enums.MessType;
import com.ypat.enums.UserStatus;
import com.ypat.service.MessagePushLogRecorder;
import com.ypat.service.OauthServiceClient;
import com.ypat.service.UserServiceClient;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.third.wxmess.WxMessClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端用户管理 Controller。
 *
 * <p>提供实名认证用户列表、详情、审核接口。
 * 通过 Feign Client 调用 system-restapi 微服务获取数据，包装为 ResponseApiBody 返回。</p>
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    /** 默认页码（0-based） */
    private static final int DEFAULT_PAGE = 0;

    /** 默认每页条数 */
    private static final int DEFAULT_SIZE = 10;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private OauthServiceClient oauthServiceClient;
    @Autowired(required = false)
    private WxMessClient wxMessClient;
    @Autowired(required = false)
    private MessagePushLogRecorder messagePushLogRecorder;

    /**
     * 实名认证用户列表（分页）。
     *
     * <p>对应旧后台接口：GET /manage/user/list</p>
     *
     * @param status 用户状态（1待审核/2审核通过/3审核未通过）
     * @param page   页码（0-based，默认 0）
     * @param size   每页条数（默认 10）
     * @return ResponseApiBody 包含 Spring Data Page（content + totalElements）
     */
    @GetMapping("/list")
    public ResponseApiBody list(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "regisdate", required = false) String regisdate,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        if (page == null || page < 0) {
            page = DEFAULT_PAGE;
        }
        if (size == null || size <= 0) {
            size = DEFAULT_SIZE;
        }

        UserQo userQo = new UserQo();
        userQo.setPage(page);
        userQo.setSize(size);
        if (StringUtils.isNotBlank(status)) {
            userQo.setStatus(status);
        }
        if (StringUtils.isNotBlank(nickname)) {
            userQo.setNickname(nickname);
        }
        if (StringUtils.isNotBlank(mobile)) {
            userQo.setMobile(mobile);
        }
        if (StringUtils.isNotBlank(regisdate)) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                userQo.setRegisdate(sdf.parse(regisdate));
            } catch (java.text.ParseException e) {
                logger.warn("注册日期格式错误：{}", regisdate);
            }
        }
        if (StringUtils.isNotBlank(gender)) {
            userQo.setGender(gender);
        }
        if (id != null) {
            userQo.setId(id);
        }

        String json = userServiceClient.findPage(userQo);
        JsonElement pageData = JsonParser.parseString(json);

        return ResponseApiBody.success(pageData);
    }

    /**
     * 实名认证详情。
     *
     * <p>对应旧后台接口：GET /manage/user/detail</p>
     *
     * @param id 用户 ID
     * @return ResponseApiBody 包含 OauthQo（实名认证信息）
     */
    @GetMapping("/detail")
    public ResponseApiBody detail(@RequestParam("id") Long id) {
        if (id == null) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }

        String json = oauthServiceClient.getAuth(id);
        OauthQo oauthQo = GsonUtils.fromJson(json, OauthQo.class);

        if (oauthQo == null) {
            throw new SysException(ResponseCode.FAIL_NOT);
        }

        return ResponseApiBody.success(oauthQo);
    }

    /**
     * 实名审核。
     *
     * <p>对应旧后台接口：GET /manage/user/audit</p>
     *
     * @param id   用户 ID
     * @param flag 审核标志（2审核通过/3审核未通过，对应 UserStatus.shtg/shbtg）
     * @return ResponseApiBody 审核结果
     */
    @PostMapping("/audit")
    public ResponseApiBody audit(
            @RequestParam("id") Long id,
            @RequestParam("flag") String flag) {

        if (id == null) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (StringUtils.isBlank(flag)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "审核标志不能为空");
        }

        logger.info("管理端实名审核：id={}, flag={}", id, flag);

        String result = oauthServiceClient.audit(id, flag);
        JsonElement resData = parseAuditResponse(result);
        pushOauthAuditMessage(id, flag);

        Map<String, Object> res = new HashMap<>(4);
        res.put("success", true);
        res.put("data", resData);

        return ResponseApiBody.success(res);
    }

    private JsonElement parseAuditResponse(String result) {
        if (StringUtils.isBlank(result)) {
            return JsonParser.parseString("{}");
        }
        return JsonParser.parseString(result);
    }

    private void pushOauthAuditMessage(Long id, String flag) {
        String page = "";
        String touserOpenid = null;
        String pushResponse = null;
        try {
            if (wxMessClient == null) {
                return;
            }
            String accessToken = wxMessClient.getAccessToken();
            if (accessToken == null) {
                return;
            }
            String userJson = userServiceClient.get(id);
            UserQo userQo = GsonUtils.fromJson(userJson, UserQo.class);
            if (userQo == null) {
                return;
            }
            touserOpenid = userQo.getOpenid();
            Map<String, String> contentMap = new HashMap<>();
            contentMap.put("type", "实名认证");
            if (UserStatus.shtg.value.equals(flag)) {
                page = Const.PAGE_REALNAME_TG;
                contentMap.put("result", UserStatus.shtg.name);
                contentMap.put("note", "赶紧找到心仪的小伙伴拍起来吧~");
            } else {
                page = Const.PAGE_REALNAME_BTG;
                contentMap.put("result", UserStatus.shbtg.name);
                contentMap.put("note", "填写信息有误，请重新认证哦~");
            }
            pushResponse = wxMessClient.sendMsg(accessToken, touserOpenid, MessType.oauth, page, contentMap);
            recordWechatPush(id, touserOpenid, page, pushResponse, null);
        } catch (Exception e) {
            logger.error("实名审核消息推送失败：", e);
            recordWechatPush(id, touserOpenid, page, pushResponse, e);
        }
    }

    private void recordWechatPush(Long recperid, String touserOpenid, String page, String responseBody, Exception error) {
        if (messagePushLogRecorder != null) {
            messagePushLogRecorder.recordWechat(MessType.oauth, null, null, recperid, touserOpenid, page, responseBody, error);
        }
    }
}
