package com.ypat;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

/**
 * 作品提交参数
 */
public class WorkSubmitQo implements Serializable {
    @NotEmpty(message = "description不能为空")
    @Length(min = 5, max = 500, message = "description长度必须在5-500字符之间")
    private String description;

    @Length(max = 100, message = "device长度不能超过100字符")
    private String device;

    @Length(max = 100, message = "shootLocation长度不能超过100字符")
    private String shootLocation;

    @NotEmpty(message = "returnPhotoFlag不能为空")
    private String returnPhotoFlag;

    @NotEmpty(message = "mediaType不能为空")
    private String mediaType;

    private String isNationwide;

    @NotEmpty(message = "mediaIds不能为空")
    private String mediaIds; // 逗号分隔

    @NotEmpty(message = "tagIds不能为空")
    private String tagIds;   // 逗号分隔

    /** 来自作品发起的约拍，透传到 ypat */
    private String workId;

    /** 当前登录用户 ID（Service 层从 Token 注入） */
    private String userid;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
    public String getShootLocation() { return shootLocation; }
    public void setShootLocation(String shootLocation) { this.shootLocation = shootLocation; }
    public String getReturnPhotoFlag() { return returnPhotoFlag; }
    public void setReturnPhotoFlag(String returnPhotoFlag) { this.returnPhotoFlag = returnPhotoFlag; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getIsNationwide() { return isNationwide; }
    public void setIsNationwide(String isNationwide) { this.isNationwide = isNationwide; }
    public String getMediaIds() { return mediaIds; }
    public void setMediaIds(String mediaIds) { this.mediaIds = mediaIds; }
    public String getTagIds() { return tagIds; }
    public void setTagIds(String tagIds) { this.tagIds = tagIds; }
    public String getWorkId() { return workId; }
    public void setWorkId(String workId) { this.workId = workId; }
    public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }

    /** 解析 mediaIds 为 List<Long> */
    public List<Long> getMediaIdList() {
        if (mediaIds == null || mediaIds.trim().isEmpty()) return java.util.Collections.emptyList();
        List<Long> out = new java.util.ArrayList<>();
        for (String s : mediaIds.split(",")) {
            try { out.add(Long.parseLong(s.trim())); } catch (NumberFormatException ignored) {}
        }
        return out;
    }

    /** 解析 tagIds 为 List<Long> */
    public List<Long> getTagIdList() {
        if (tagIds == null || tagIds.trim().isEmpty()) return java.util.Collections.emptyList();
        List<Long> out = new java.util.ArrayList<>();
        for (String s : tagIds.split(",")) {
            try { out.add(Long.parseLong(s.trim())); } catch (NumberFormatException ignored) {}
        }
        return out;
    }
}
