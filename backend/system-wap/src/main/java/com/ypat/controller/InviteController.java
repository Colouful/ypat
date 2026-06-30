package com.ypat.controller;

import com.ypat.InviteRelationQo;
import com.ypat.InviteSummaryQo;
import com.ypat.service.InviteServiceClient;
import com.ypat.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 外部邀请接口 — 暴露给 H5 / 小程序客户端。
 * 所有接口都从 SecurityContext 取当前用户 ID，不接受前端传入的 inviter id，
 * 避免越权查询他人邀请记录。
 */
@RestController
@Validated
public class InviteController {

    private static final Logger logger = LoggerFactory.getLogger(InviteController.class);

    @Autowired
    private InviteServiceClient inviteServiceClient;

    @GetMapping(value = {"/invite/my-info"})
    public InviteSummaryQo myInfo() {
        Long userId = currentUserIdOrNull();
        return inviteServiceClient.summary(userId);
    }

    @GetMapping(value = {"/invite/rule"})
    public Map<String, Object> rule() {
        // 单独的规则接口供分享落地页 / 未登录浏览，不依赖 userId。
        // 真实奖励数从 InviteService.getSummary(null) 透传 Constant.INVITE_NEED_PPD。
        InviteSummaryQo summary = inviteServiceClient.summary(null);
        Map<String, Object> body = new HashMap<>();
        body.put("rewardPpd", summary.getRewardPpd());
        body.put("rewardUnit", "拍拍豆");
        body.put("ruleText", "好友通过你的邀请码注册后，自动到账 " + summary.getRewardPpd() + " 拍拍豆。");
        return body;
    }

    @GetMapping(value = {"/invite/records"})
    public Map<String, Object> records(InviteRelationQo qo) {
        Long userId = currentUserIdOrNull();
        if (userId == null) {
            // 未登录时返回空分页而不是 500，便于前端统一渲染空态。
            qo.setInviterUserid(-1L);
        } else {
            qo.setInviterUserid(userId);
        }
        return inviteServiceClient.findPage(qo);
    }

    private Long currentUserIdOrNull() {
        String raw = UserUtil.getUserId();
        if (raw == null || raw.isEmpty()) return null;
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            logger.warn("invite.user_id_parse_fail raw={}", raw);
            return null;
        }
    }
}
