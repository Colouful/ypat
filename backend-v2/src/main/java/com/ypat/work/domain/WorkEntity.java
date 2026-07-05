package com.ypat.work.domain;

import jakarta.persistence.*;
import java.util.Date;

/**
 * PR-11 follow-up: real JPA entity for t_work.
 *
 * Maps the production schema (PR-07b baseline) so v2 can serve
 * the real work list / detail once the controller is wired
 * (PR-11 follow-up).
 */
@Entity
@Table(name = "t_work")
public class WorkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userid")
    private Long userId;

    @Column(name = "description")
    private String description;

    @Column(name = "device")
    private String device;

    @Column(name = "shoot_location")
    private String shootLocation;

    @Column(name = "media_type")
    private String mediaType;

    @Column(name = "return_photo_flag")
    private Integer returnPhotoFlag;

    @Column(name = "is_nationwide")
    private Integer isNationwide;

    @Column(name = "status")
    private String status;

    @Column(name = "audit_reason")
    private String auditReason;

    @Column(name = "read_count")
    private Integer readCount;

    @Column(name = "like_count")
    private Integer likeCount;

    @Column(name = "favorite_count")
    private Integer favoriteCount;

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

    @Column(name = "city")
    private String city;

    @Column(name = "area")
    private String area;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getDescription() { return description; }
    public String getDevice() { return device; }
    public String getShootLocation() { return shootLocation; }
    public String getMediaType() { return mediaType; }
    public Integer getReturnPhotoFlag() { return returnPhotoFlag; }
    public Integer getIsNationwide() { return isNationwide; }
    public String getStatus() { return status; }
    public String getAuditReason() { return auditReason; }
    public Integer getReadCount() { return readCount; }
    public Integer getLikeCount() { return likeCount; }
    public Integer getFavoriteCount() { return favoriteCount; }
    public Date getPublishTime() { return publishTime; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public Integer getDeletedFlag() { return deletedFlag; }
    public String getCity() { return city; }
    public String getArea() { return area; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setDescription(String description) { this.description = description; }
    public void setDevice(String device) { this.device = device; }
    public void setShootLocation(String shootLocation) { this.shootLocation = shootLocation; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public void setReturnPhotoFlag(Integer returnPhotoFlag) { this.returnPhotoFlag = returnPhotoFlag; }
    public void setIsNationwide(Integer isNationwide) { this.isNationwide = isNationwide; }
    public void setStatus(String status) { this.status = status; }
    public void setAuditReason(String auditReason) { this.auditReason = auditReason; }
    public void setReadCount(Integer readCount) { this.readCount = readCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public void setDeletedFlag(Integer deletedFlag) { this.deletedFlag = deletedFlag; }
    public void setCity(String city) { this.city = city; }
    public void setArea(String area) { this.area = area; }
}