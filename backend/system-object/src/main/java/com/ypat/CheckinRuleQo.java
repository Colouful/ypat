package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class CheckinRuleQo implements Serializable {
    private Long id;
    private String enabled;
    private Integer rewardPpd;
    private String confirmTitle;
    private String confirmContent;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
