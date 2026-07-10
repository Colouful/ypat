package com.ypat;

public class InternalTestBatchQo implements java.io.Serializable {
    private String batchNo;
    private Integer userCount;
    private Integer ypatCount;
    private Integer workCount;
    private Integer ignoredRealCount;
    private Integer releasedResourceCount;
    private String status;
    private java.util.List<String> errors;
    private java.util.Date createdAt;

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Integer getUserCount() { return userCount; }
    public void setUserCount(Integer userCount) { this.userCount = userCount; }
    public Integer getYpatCount() { return ypatCount; }
    public void setYpatCount(Integer ypatCount) { this.ypatCount = ypatCount; }
    public Integer getWorkCount() { return workCount; }
    public void setWorkCount(Integer workCount) { this.workCount = workCount; }
    public Integer getIgnoredRealCount() { return ignoredRealCount; }
    public void setIgnoredRealCount(Integer ignoredRealCount) { this.ignoredRealCount = ignoredRealCount; }
    public Integer getReleasedResourceCount() { return releasedResourceCount; }
    public void setReleasedResourceCount(Integer releasedResourceCount) { this.releasedResourceCount = releasedResourceCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.util.List<String> getErrors() { return errors; }
    public void setErrors(java.util.List<String> errors) { this.errors = errors; }
    public java.util.Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.util.Date createdAt) { this.createdAt = createdAt; }
}
