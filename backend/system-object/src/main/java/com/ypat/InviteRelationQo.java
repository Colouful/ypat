package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class InviteRelationQo extends PageQo implements Serializable {
    private Long id;
    private Long inviterUserid;
    private Long inviteeUserid;
    private String inviteCode;
    private String source;
    private Integer rewardPpd;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date credate;

    /** 列表场景下展示给前端的被邀请人脱敏快照。 */
    private String inviteeNickname;
    private String inviteeImgpath;
    private String inviteeMobileMask;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInviterUserid() { return inviterUserid; }
    public void setInviterUserid(Long inviterUserid) { this.inviterUserid = inviterUserid; }
    public Long getInviteeUserid() { return inviteeUserid; }
    public void setInviteeUserid(Long inviteeUserid) { this.inviteeUserid = inviteeUserid; }
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Integer getRewardPpd() { return rewardPpd; }
    public void setRewardPpd(Integer rewardPpd) { this.rewardPpd = rewardPpd; }
    public Date getCredate() { return credate; }
    public void setCredate(Date credate) { this.credate = credate; }
    public String getInviteeNickname() { return inviteeNickname; }
    public void setInviteeNickname(String inviteeNickname) { this.inviteeNickname = inviteeNickname; }
    public String getInviteeImgpath() { return inviteeImgpath; }
    public void setInviteeImgpath(String inviteeImgpath) { this.inviteeImgpath = inviteeImgpath; }
    public String getInviteeMobileMask() { return inviteeMobileMask; }
    public void setInviteeMobileMask(String inviteeMobileMask) { this.inviteeMobileMask = inviteeMobileMask; }
}
