package com.ypat;

import java.io.Serializable;

/**
 * `/invite/my-info` 响应：当前用户的邀请概览。
 * - inviteCode：可分享的邀请码（base36 编码 user.id，无 mobile 泄露）
 * - totalInvited：成功绑定为下线的人数
 * - totalReward：累计奖励拍拍豆
 * - rewardPpd：单次邀请奖励数（取自后端常量，前端不要硬编码）
 */
public class InviteSummaryQo implements Serializable {
    private String inviteCode;
    private Long totalInvited;
    private Integer totalReward;
    private Integer rewardPpd;

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public Long getTotalInvited() { return totalInvited; }
    public void setTotalInvited(Long totalInvited) { this.totalInvited = totalInvited; }
    public Integer getTotalReward() { return totalReward; }
    public void setTotalReward(Integer totalReward) { this.totalReward = totalReward; }
    public Integer getRewardPpd() { return rewardPpd; }
    public void setRewardPpd(Integer rewardPpd) { this.rewardPpd = rewardPpd; }
}
