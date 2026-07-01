package com.ypat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "t_work")
@DynamicInsert
@DynamicUpdate
public class Work implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userid;
    private String description;
    private String device;
    @Column(name = "shoot_location")
    private String shootLocation;
    @Column(name = "return_photo_flag")
    private Integer returnPhotoFlag;
    @Column(name = "media_type")
    private String mediaType;
    @Column(name = "is_nationwide")
    private Integer isNationwide;
    private String status;
    @Column(name = "audit_reason")
    private String auditReason;
    @Column(name = "read_count")
    private Integer readCount;
    @Column(name = "like_count")
    private Integer likeCount;
    @Column(name = "favorite_count")
    private Integer favoriteCount;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "publish_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date publishTime;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(name = "deleted_flag")
    private Integer deletedFlag;
    private String city;
    private String area;

    // 关联（非持久化字段，用于查询时填充）
    @Transient
    @JsonIgnore
    private User user;
    @Transient
    @JsonIgnore
    private List<WorkMedia> medias;
    @Transient
    @JsonIgnore
    private List<WorkTag> tags;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserid() { return userid; }
    public void setUserid(Long userid) { this.userid = userid; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
    public String getShootLocation() { return shootLocation; }
    public void setShootLocation(String shootLocation) { this.shootLocation = shootLocation; }
    public Integer getReturnPhotoFlag() { return returnPhotoFlag; }
    public void setReturnPhotoFlag(Integer returnPhotoFlag) { this.returnPhotoFlag = returnPhotoFlag; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public Integer getIsNationwide() { return isNationwide; }
    public void setIsNationwide(Integer isNationwide) { this.isNationwide = isNationwide; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAuditReason() { return auditReason; }
    public void setAuditReason(String auditReason) { this.auditReason = auditReason; }
    public Integer getReadCount() { return readCount; }
    public void setReadCount(Integer readCount) { this.readCount = readCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public Integer getDeletedFlag() { return deletedFlag; }
    public void setDeletedFlag(Integer deletedFlag) { this.deletedFlag = deletedFlag; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<WorkMedia> getMedias() { return medias; }
    public void setMedias(List<WorkMedia> medias) { this.medias = medias; }
    public List<WorkTag> getTags() { return tags; }
    public void setTags(List<WorkTag> tags) { this.tags = tags; }
}
