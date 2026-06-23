package com.ypat.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_mess_info")
@DynamicInsert
@DynamicUpdate
@NamedEntityGraph(
        name="MessInfo.all",
        attributeNodes = {
                @NamedAttributeNode(value = "sendper",  subgraph = ""),
                @NamedAttributeNode(value = "recper", subgraph = ""),
                @NamedAttributeNode(value = "ypatInfo", subgraph = "subUser")
        },
        subgraphs = {
                @NamedSubgraph(name="subUser",attributeNodes = {
                        @NamedAttributeNode(value = "user")
                })
        })
public class MessInfo implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private String content;
    private String status;
    private String messviewflag;
    private String linkwayflag;
    @ManyToOne
    @JoinColumn(name = "sendperid")
    private User sendper;

    @ManyToOne
    @JoinColumn(name = "recperid")
    private User recper;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "credate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date credate;

    @ManyToOne
    @JoinColumn(name = "ypatid")
    private YpatInfo ypatInfo;

    public MessInfo() {
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
        return content;
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

    public User getSendper() {
        return sendper;
    }

    public void setSendper(User sendper) {
        this.sendper = sendper;
    }

    public User getRecper() {
        return recper;
    }

    public void setRecper(User recper) {
        this.recper = recper;
    }

    public String getLinkwayflag() {
        return linkwayflag;
    }

    public void setLinkwayflag(String linkwayflag) {
        this.linkwayflag = linkwayflag;
    }

    public Date getCredate() {
        return credate;
    }

    public void setCredate(Date credate) {
        this.credate = credate;
    }

    public String getMessviewflag() {
        return messviewflag;
    }

    public void setMessviewflag(String messviewflag) {
        this.messviewflag = messviewflag;
    }

    public YpatInfo getYpatInfo() {
        return ypatInfo;
    }

    public void setYpatInfo(YpatInfo ypatInfo) {
        this.ypatInfo = ypatInfo;
    }
}
