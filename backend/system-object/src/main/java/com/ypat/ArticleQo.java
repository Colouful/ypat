package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ypat.enums.ArticlePlat;
import com.ypat.enums.ArticleStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class ArticleQo extends PageQo implements java.io.Serializable {
    private Long id;
    private String title;
    private String describ;
    private String content;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date credate;
    private Long userid;
    private String status;
    private String flag;
    private String plat;
    private Integer readtimes;
    private String editorValue;
    private String statusTxt;
    private String platTxt;
    private String imgpath;
    private String timeStr;;

    public ArticleQo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescrib() {
        return describ;
    }

    public void setDescrib(String describ) {
        this.describ = describ;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCredate() {
        return credate;
    }

    public void setCredate(Date credate) {
        this.credate = credate;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getPlat() {
        return plat;
    }

    public void setPlat(String plat) {
        this.plat = plat;
    }

    public Integer getReadtimes() {
        return readtimes;
    }

    public void setReadtimes(Integer readtimes) {
        this.readtimes = readtimes;
    }

    public String getEditorValue() {
        return editorValue;
    }

    public void setEditorValue(String editorValue) {
        this.editorValue = editorValue;
    }

    public String getStatusTxt() {
        return ArticleStatus.getNameByCode(status);
    }

    public void setStatusTxt(String statusTxt) {
        this.statusTxt = statusTxt;
    }

    public String getPlatTxt() {
        return ArticlePlat.getNameByCode(plat);
    }

    public void setPlatTxt(String platTxt) {
        this.platTxt = platTxt;
    }

    public String getImgpath() {
        return imgpath;
    }

    public void setImgpath(String imgpath) {
        this.imgpath = imgpath;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }
}
