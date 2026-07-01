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

    /** Service 层从 Token 注入 */
    private String viewerUserId;

    public String getWorkId() { return workId; }
    public void setWorkId(String workId) { this.workId = workId; }
    public String getViewerUserId() { return viewerUserId; }
    public void setViewerUserId(String viewerUserId) { this.viewerUserId = viewerUserId; }
}
