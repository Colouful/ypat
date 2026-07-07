package com.ypat;

import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

/**
 * 从作品发起约拍的参数（用于快速跳转约拍表单）
 */
public class WorkQuickApplyQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "workId不能为空")
    private String workId;
    private String reason;
    private String mobile;
    private String wx;

    /** Service 层从 Token 注入 */
    private String viewerUserId;

    public String getWorkId() { return workId; }
    public void setWorkId(String workId) { this.workId = workId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getWx() { return wx; }
    public void setWx(String wx) { this.wx = wx; }
    public String getViewerUserId() { return viewerUserId; }
    public void setViewerUserId(String viewerUserId) { this.viewerUserId = viewerUserId; }
}
