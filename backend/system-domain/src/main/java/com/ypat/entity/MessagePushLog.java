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
@Table(name = "t_message_push_log")
public class MessagePushLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;
    @Column(name = "business_type", length = 32)
    private String businessType;
    @Column(name = "message_id")
    private Long messageId;
    private Long ypatid;
    private Long sendperid;
    private Long recperid;
    @Column(name = "touser_openid", length = 128)
    private String touserOpenid;
    @Column(name = "template_id", length = 128)
    private String templateId;
    @Column(name = "page_url", length = 255)
    private String pageUrl;
    @Column(length = 8)
    private String success;
    @Column(name = "wechat_errcode", length = 32)
    private String wechatErrcode;
    @Column(name = "wechat_errmsg", length = 255)
    private String wechatErrmsg;
    @Column(name = "response_body", length = 1024)
    private String responseBody;
    @Column(length = 255)
    private String remark;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getYpatid() { return ypatid; }
    public void setYpatid(Long ypatid) { this.ypatid = ypatid; }
    public Long getSendperid() { return sendperid; }
    public void setSendperid(Long sendperid) { this.sendperid = sendperid; }
    public Long getRecperid() { return recperid; }
    public void setRecperid(Long recperid) { this.recperid = recperid; }
    public String getTouserOpenid() { return touserOpenid; }
    public void setTouserOpenid(String touserOpenid) { this.touserOpenid = touserOpenid; }
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }
    public String getSuccess() { return success; }
    public void setSuccess(String success) { this.success = success; }
    public String getWechatErrcode() { return wechatErrcode; }
    public void setWechatErrcode(String wechatErrcode) { this.wechatErrcode = wechatErrcode; }
    public String getWechatErrmsg() { return wechatErrmsg; }
    public void setWechatErrmsg(String wechatErrmsg) { this.wechatErrmsg = wechatErrmsg; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
