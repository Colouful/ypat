package com.ypat.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_internal_test_resource")
@DynamicInsert
@DynamicUpdate
public class InternalTestResource implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "media_type")
    private String mediaType;
    @Column(name = "usage_type")
    private String usageType;
    @Column(name = "style_code")
    private String styleCode;
    private String url;
    @Transient
    private java.util.List<String> urls;
    private String title;
    private String description;
    private String profession;
    private String province;
    private String city;
    private String area;
    @Column(name = "group_no")
    private String groupNo;
    @Column(name = "group_title")
    private String groupTitle;
    @Transient
    private Integer groupSize;
    @Column(name = "group_sort_no")
    private Integer groupSortNo;
    @Column(name = "used_flag")
    private Integer usedFlag;
    @Column(name = "used_batch_no")
    private String usedBatchNo;
    @Column(name = "used_target_type")
    private String usedTargetType;
    @Column(name = "used_target_id")
    private Long usedTargetId;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "used_at")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date usedAt;
    private String status;
    @Column(name = "sort_no")
    private Integer sortNo;
    private String remark;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getUsageType() { return usageType; }
    public void setUsageType(String usageType) { this.usageType = usageType; }
    public String getStyleCode() { return styleCode; }
    public void setStyleCode(String styleCode) { this.styleCode = styleCode; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public java.util.List<String> getUrls() { return urls; }
    public void setUrls(java.util.List<String> urls) { this.urls = urls; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getGroupNo() { return groupNo; }
    public void setGroupNo(String groupNo) { this.groupNo = groupNo; }
    public String getGroupTitle() { return groupTitle; }
    public void setGroupTitle(String groupTitle) { this.groupTitle = groupTitle; }
    public Integer getGroupSize() { return groupSize; }
    public void setGroupSize(Integer groupSize) { this.groupSize = groupSize; }
    public Integer getGroupSortNo() { return groupSortNo; }
    public void setGroupSortNo(Integer groupSortNo) { this.groupSortNo = groupSortNo; }
    public Integer getUsedFlag() { return usedFlag; }
    public void setUsedFlag(Integer usedFlag) { this.usedFlag = usedFlag; }
    public String getUsedBatchNo() { return usedBatchNo; }
    public void setUsedBatchNo(String usedBatchNo) { this.usedBatchNo = usedBatchNo; }
    public String getUsedTargetType() { return usedTargetType; }
    public void setUsedTargetType(String usedTargetType) { this.usedTargetType = usedTargetType; }
    public Long getUsedTargetId() { return usedTargetId; }
    public void setUsedTargetId(Long usedTargetId) { this.usedTargetId = usedTargetId; }
    public java.util.Date getUsedAt() { return usedAt; }
    public void setUsedAt(java.util.Date usedAt) { this.usedAt = usedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
