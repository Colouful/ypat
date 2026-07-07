package com.ypat;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

/**
 * 作品投诉参数
 */
public class WorkComplainQo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotEmpty(message = "workId不能为空")
    private String workId;

    @NotEmpty(message = "reason不能为空")
    @Length(min = 10, max = 500, message = "reason长度必须在10-500字符之间")
    private String reason;

    @Length(max = 100, message = "contact长度不能超过100字符")
    private String contact;
    private String content;
    private String pics;

    /** Service 层从 Token 注入 */
    private String userId;

    /** 管理端处理状态 */
    private String status;
    private String handleReason;

    /** 管理端是否关联下架作品 */
    private Boolean offlineWork;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWorkId() { return workId; }
    public void setWorkId(String workId) { this.workId = workId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getPics() { return pics; }
    public void setPics(String pics) { this.pics = pics; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getHandleReason() { return handleReason; }
    public void setHandleReason(String handleReason) { this.handleReason = handleReason; }
    public Boolean getOfflineWork() { return offlineWork; }
    public void setOfflineWork(Boolean offlineWork) { this.offlineWork = offlineWork; }
}
