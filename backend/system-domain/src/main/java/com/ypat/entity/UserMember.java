package com.ypat.entity;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户会员状态 — 单行 = 当前生效的会员有效期。
 * 续费场景：markPaid 时在当前 expire_at 之上叠加 duration_days。
 * 取消会员：不删除行，level 改 NONE / expire_at 改 NOW。
 */
@Entity
@Table(name = "t_user_member")
public class UserMember implements Serializable {
    @Id
    @Column(name = "user_id")
    private Long userId;

    /** NONE / BASIC / PRO ... 当前切片 3 仅用 BASIC 表示"已开通过"。 */
    @Column(nullable = false, length = 16)
    private String level;

    @Column(name = "expire_at", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expireAt;

    /** 最近一次开通/续费的订单号（最近一份源订单）。 */
    @Column(name = "source_order_no", length = 64)
    private String sourceOrderNo;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Date getExpireAt() { return expireAt; }
    public void setExpireAt(Date expireAt) { this.expireAt = expireAt; }
    public String getSourceOrderNo() { return sourceOrderNo; }
    public void setSourceOrderNo(String sourceOrderNo) { this.sourceOrderNo = sourceOrderNo; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}