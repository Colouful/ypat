package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ypat.enums.ArticleStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class BannerQo extends PageQo implements java.io.Serializable {
    private Long id;
    private String title;
    private String imgpath;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date credate;
    private Long userid;
    private String status;
    private String statusTxt;

    public BannerQo() {
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

    public String getImgpath() {
        return imgpath;
    }

    public void setImgpath(String imgpath) {
        this.imgpath = imgpath;
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

    public String getStatusTxt() {
        return ArticleStatus.getNameByCode(status);
    }

    public void setStatusTxt(String statusTxt) {
        this.statusTxt = statusTxt;
    }
}
