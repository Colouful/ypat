package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class MessagePushLogQo extends PageQo implements Serializable {
    private Long id;
    private String eventType;
    private String businessType;
    private Long messageId;
    private Long ypatid;
    private Long sendperid;
    private Long recperid;
    private String touserOpenid;
    private String templateId;
    private String pageUrl;
    private String success;
    private String wechatErrcode;
    private String wechatErrmsg;
    private String responseBody;
    private String remark;
    private String dateStart;
    private String dateEnd;
    private Long total;
    private Long successCount;
    private Long failedCount;
    private Long wechatTotal;
    private Long inAppTotal;
    private String failedRate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    public String getDateStart() { return dateStart; }
    public void setDateStart(String dateStart) { this.dateStart = dateStart; }
    public String getDateEnd() { return dateEnd; }
    public void setDateEnd(String dateEnd) { this.dateEnd = dateEnd; }
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
    public Long getSuccessCount() { return successCount; }
    public void setSuccessCount(Long successCount) { this.successCount = successCount; }
    public Long getFailedCount() { return failedCount; }
    public void setFailedCount(Long failedCount) { this.failedCount = failedCount; }
    public Long getWechatTotal() { return wechatTotal; }
    public void setWechatTotal(Long wechatTotal) { this.wechatTotal = wechatTotal; }
    public Long getInAppTotal() { return inAppTotal; }
    public void setInAppTotal(Long inAppTotal) { this.inAppTotal = inAppTotal; }
    public String getFailedRate() { return failedRate; }
    public void setFailedRate(String failedRate) { this.failedRate = failedRate; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
