package com.ypat;

import java.io.Serializable;

/**
 * 作品列表查询参数
 */
public class WorkListQo extends PageQo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 推荐|同城|模特|摄影|化妆|修图 */
    private String category;

    /** 城市（同城筛选） */
    private String city;

    /** 性别：1=男 2=女 */
    private String gender;

    /** 身份（profess 枚举值） */
    private String profession;

    /** 标签 ID（多选用 , 分隔） */
    private String tagIds;

    /** 当前登录用户（Service 注入，用于判断 colflag/likeFlag） */
    private String viewerUserId;

    private String status;
    private String nickname;
    private String mobile;
    private String mediaType;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getTagIds() { return tagIds; }
    public void setTagIds(String tagIds) { this.tagIds = tagIds; }
    public String getViewerUserId() { return viewerUserId; }
    public void setViewerUserId(String viewerUserId) { this.viewerUserId = viewerUserId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
}
