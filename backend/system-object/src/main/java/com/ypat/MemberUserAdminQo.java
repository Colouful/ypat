package com.ypat;

import java.io.Serializable;
import java.util.Date;

public class MemberUserAdminQo extends PageQo implements Serializable {
    private Long userId;
    private String mobile;
    private String nickname;
    private String levelCode;
    private Date expireAt;
    private String memberStatus;
    private Integer days;
    private String reason;
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    public Date getExpireAt() { return expireAt; }
    public void setExpireAt(Date expireAt) { this.expireAt = expireAt; }
    public String getMemberStatus() { return memberStatus; }
    public void setMemberStatus(String memberStatus) { this.memberStatus = memberStatus; }
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
