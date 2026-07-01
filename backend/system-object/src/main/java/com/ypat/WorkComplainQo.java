package com.ypat;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

/**
 * 作品投诉参数
 */
public class WorkComplainQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "workId不能为空")
    private String workId;

    @NotEmpty(message = "reason不能为空")
    @Length(min = 10, max = 500, message = "reason长度必须在10-500字符之间")
    private String reason;

    @Length(max = 100, message = "contact长度不能超过100字符")
    private String contact;

    /** Service 层从 Token 注入 */
    private String userId;

    public String getWorkId() { return workId; }
    public void setWorkId(String workId) { this.workId = workId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
