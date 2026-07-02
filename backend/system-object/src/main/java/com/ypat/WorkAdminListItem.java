package com.ypat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class WorkAdminListItem implements Serializable {
    private Long id;
    private String description;
    private String coverUrl;
    private String mediaType;
    private String mediaTypeTxt;
    private String status;
    private String statusTxt;
    private String auditReason;
    private Integer readCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Date publishTime;
    private Long userId;
    private String nickname;
    private String mobile;
    private String gender;
    private String profession;
    private String city;
    private String area;
    private List<String> tags;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getMediaTypeTxt() { return mediaTypeTxt; }
    public void setMediaTypeTxt(String mediaTypeTxt) { this.mediaTypeTxt = mediaTypeTxt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStatusTxt() { return statusTxt; }
    public void setStatusTxt(String statusTxt) { this.statusTxt = statusTxt; }
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
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
