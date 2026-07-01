package com.ypat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 作品列表项（轻量级）
 */
public class WorkListItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String description;
    private String coverUrl;       // 第一张图
    private String mediaType;      // 1图 2视频
    private String isVideo;        // "1" 视频标识
    private Long userId;
    private String nickname;
    private String avatar;
    private String gender;
    private String profession;     // 作者身份
    private String city;
    private String area;
    private String activeTime;     // "5分钟前" 格式
    private Integer readCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private List<String> tags;     // 标签名称列表
    private Date publishTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getIsVideo() { return isVideo; }
    public void setIsVideo(String isVideo) { this.isVideo = isVideo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getActiveTime() { return activeTime; }
    public void setActiveTime(String activeTime) { this.activeTime = activeTime; }
    public Integer getReadCount() { return readCount; }
    public void setReadCount(Integer readCount) { this.readCount = readCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
}
