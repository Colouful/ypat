package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ypat.enums.RecordType;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class RecordQo extends PageQo implements java.io.Serializable{
    private Long id;
    private String type;//类型		（0.充值、1.好友邀请、2.系统赠送、3.发布约拍、4.申请约拍、5.查看联系方式）
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date credate;
    private Integer ppd;
    private Long userid;
    private String typeTxt;//类型

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

    public Date getCredate() {
        return credate;
    }

    public void setCredate(Date credate) {
        this.credate = credate;
    }

    public Integer getPpd() {
        return ppd;
    }

    public void setPpd(Integer ppd) {
        this.ppd = ppd;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getTypeTxt() {
        return RecordType.getNameByCode(this.type);
    }

    public void setTypeTxt(String typeTxt) {
        this.typeTxt = typeTxt;
    }
}
