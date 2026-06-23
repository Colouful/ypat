package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ypat.utils.FormatQo;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class MessInfoQo extends PageQo implements java.io.Serializable {

    private Long id;
    private String type;
    private String content;
    private String status;
    private Long sendperid;
    private Long recperid;
    private String messviewflag;
    private String linkwayflag;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date credate;

    private String nickname;
    private String imgpath;
    private Long ypatid;
    private String timeStr;
    private String city;

    public MessInfoQo() {
    }

    public MessInfoQo(Long id, String content, String nickname, String imgpath) {
        this.id = id;
        this.content = content;
        this.nickname = nickname;
        this.imgpath = imgpath;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImgpath() {
        return imgpath;
    }

    public void setImgpath(String imgpath) {
        this.imgpath = imgpath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return FormatQo.replaceNum(content);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSendperid() {
        return sendperid;
    }

    public void setSendperid(Long sendperid) {
        this.sendperid = sendperid;
    }

    public Long getRecperid() {
        return recperid;
    }

    public void setRecperid(Long recperid) {
        this.recperid = recperid;
    }


    public Date getCredate() {
        return credate;
    }

    public void setCredate(Date credate) {
        this.credate = credate;
    }

    public Long getYpatid() {
        return ypatid;
    }

    public void setYpatid(Long ypatid) {
        this.ypatid = ypatid;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getMessviewflag() {
        return messviewflag;
    }

    public void setMessviewflag(String messviewflag) {
        this.messviewflag = messviewflag;
    }

    public String getLinkwayflag() {
        return linkwayflag;
    }

    public void setLinkwayflag(String linkwayflag) {
        this.linkwayflag = linkwayflag;
    }

    @Override
    public String toString() {
        return "MessInfoQo{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", status='" + status + '\'' +
                ", sendperid=" + sendperid +
                ", recperid=" + recperid +
                ", messviewflag='" + messviewflag + '\'' +
                ", linkwayflag='" + linkwayflag + '\'' +
                ", credate=" + credate +
                ", nickname='" + nickname + '\'' +
                ", imgpath='" + imgpath + '\'' +
                ", ypatid=" + ypatid +
                ", timeStr='" + timeStr + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
