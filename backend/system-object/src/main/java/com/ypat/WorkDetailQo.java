package com.ypat;

import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

/**
 * 作品详情查询参数
 */
public class WorkDetailQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "id不能为空")
    private String id;

    /** Service 层从 Token 注入 viewerUserId */
    private String viewerUserId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getViewerUserId() { return viewerUserId; }
    public void setViewerUserId(String viewerUserId) { this.viewerUserId = viewerUserId; }
}
