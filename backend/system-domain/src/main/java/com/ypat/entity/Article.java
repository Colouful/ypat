package com.ypat.entity;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.support.SimpleTriggerContext;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author dingyinxin
 */
@Entity
@Table(name = "t_article")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String describ;
    private String content;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    private Date credate;
    private Long userid;
    private String status;//状态（0.暂存、1.发布、2.撤回）
    private String flag;//是否热门文章
    private String plat;//平台
    private Integer readtimes;
    private String imgpath;

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

    public String getImgpath() {
        return imgpath;
    }

    public void setImgpath(String imgpath) {
        this.imgpath = imgpath;
    }
}
