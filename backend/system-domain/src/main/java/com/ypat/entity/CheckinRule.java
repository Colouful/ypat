package com.ypat.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_checkin_rule")
public class CheckinRule implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 1)
    private String enabled;
    @Column(name = "reward_ppd", nullable = false)
    private Integer rewardPpd;
    @Column(name = "confirm_title", nullable = false, length = 64)
    private String confirmTitle;
    @Column(name = "confirm_content", nullable = false, length = 256)
    private String confirmContent;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public Integer getRewardPpd() { return rewardPpd; }
    public void setRewardPpd(Integer rewardPpd) { this.rewardPpd = rewardPpd; }
    public String getConfirmTitle() { return confirmTitle; }
    public void setConfirmTitle(String confirmTitle) { this.confirmTitle = confirmTitle; }
    public String getConfirmContent() { return confirmContent; }
    public void setConfirmContent(String confirmContent) { this.confirmContent = confirmContent; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
