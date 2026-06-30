package com.ypat.entity;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import java.util.Date;

/**
 * 邀请关系。一行 = 一个被邀请人在注册时被绑定到一个邀请人。
 *
 * 幂等保证：
 * - `uk_invitee`(invitee_userid) 唯一约束 → 同一个新用户只能被绑定一次
 * - 注册流程（{@link com.ypat.service.UserService#save}）在事务内写入此关系，
 *   重复注册请求会因 mobile 唯一约束在更上层失败，不会重复触发本表写入
 *
 * 奖励仍走 {@link Record}（type = FRI），本表只记录"谁邀请了谁"的关系。
 */
@Entity
@Table(
        name = "t_invite_relation",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_invitee_userid", columnNames = "invitee_userid")
        },
        indexes = {
                @Index(name = "idx_inviter_userid", columnList = "inviter_userid")
        }
)
public class InviteRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inviter_userid", nullable = false)
    private Long inviterUserid;

    @Column(name = "invitee_userid", nullable = false)
    private Long inviteeUserid;

    /** 命中的邀请码（来源 InviteCodeCodec.encode）。recmobile 兜底场景可为空。 */
    @Column(name = "invite_code", length = 32)
    private String inviteCode;

    /** 入口来源：share / qr / manual / recmobile，便于运营回溯。 */
    @Column(name = "source", length = 16)
    private String source;

    @Column(name = "reward_ppd")
    private Integer rewardPpd;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    private Date credate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInviterUserid() {
        return inviterUserid;
    }

    public void setInviterUserid(Long inviterUserid) {
        this.inviterUserid = inviterUserid;
    }

    public Long getInviteeUserid() {
        return inviteeUserid;
    }

    public void setInviteeUserid(Long inviteeUserid) {
        this.inviteeUserid = inviteeUserid;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getRewardPpd() {
        return rewardPpd;
    }

    public void setRewardPpd(Integer rewardPpd) {
        this.rewardPpd = rewardPpd;
    }

    public Date getCredate() {
        return credate;
    }

    public void setCredate(Date credate) {
        this.credate = credate;
    }
}
