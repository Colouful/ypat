package com.ypat.user.domain;

import jakarta.persistence.*;
import java.util.Date;

/**
 * PR-15 follow-up: real JPA entity for t_user.
 *
 * Maps the existing production schema (PR-07b baseline). All
 * columns are nullable in the baseline because the legacy
 * data is messy; we keep the entity permissive and let the
 * service layer do validation.
 */
@Entity
@Table(name = "t_user")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "avatarurl")
    private String avatarUrl;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "openid")
    private String openid;

    @Column(name = "city")
    private String city;

    @Column(name = "area")
    private String area;

    @Column(name = "province")
    private String province;

    @Column(name = "gender")
    private String gender;

    @Column(name = "profess")
    private String profess;

    @Column(name = "realnameflag")
    private String realnameFlag;

    @Column(name = "pubtimes")
    private Integer pubTimes;

    @Column(name = "coltimes")
    private Integer colTimes;

    @Column(name = "ppd")
    private Integer ppd;

    @Column(name = "creditflag")
    private String creditFlag;

    @Column(name = "channel")
    private String channel;

    @Column(name = "qq")
    private String qq;

    @Column(name = "birthday")
    @Temporal(TemporalType.TIMESTAMP)
    private Date birthday;

    @Column(name = "certcode")
    private String certCode;

    /**
     * The legacy t_user table has a single timestamp column
     * (regisdate). v2 maps BOTH createdAt and updatedAt to it
     * because there is no separate "last update" column in the
     * legacy schema. Once the table gets a proper updated_at
     * column (PR-15 follow-up via Flyway), split the mapping.
     */
    @Column(name = "regisdate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Transient
    private Date updatedAt;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getNickname() { return nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getMobile() { return mobile; }
    public String getOpenid() { return openid; }
    public String getCity() { return city; }
    public String getArea() { return area; }
    public String getProvince() { return province; }
    public String getGender() { return gender; }
    public String getProfess() { return profess; }
    public String getRealnameFlag() { return realnameFlag; }
    public Integer getPubTimes() { return pubTimes; }
    public Integer getColTimes() { return colTimes; }
    public Integer getPpd() { return ppd; }
    public String getCreditFlag() { return creditFlag; }
    public String getChannel() { return channel; }
    public String getQq() { return qq; }
    public Date getBirthday() { return birthday; }
    public String getCertCode() { return certCode; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public void setOpenid(String openid) { this.openid = openid; }
    public void setCity(String city) { this.city = city; }
    public void setArea(String area) { this.area = area; }
    public void setProvince(String province) { this.province = province; }
    public void setGender(String gender) { this.gender = gender; }
    public void setProfess(String profess) { this.profess = profess; }
    public void setRealnameFlag(String realnameFlag) { this.realnameFlag = realnameFlag; }
    public void setPubTimes(Integer pubTimes) { this.pubTimes = pubTimes; }
    public void setColTimes(Integer colTimes) { this.colTimes = colTimes; }
    public void setPpd(Integer ppd) { this.ppd = ppd; }
    public void setCreditFlag(String creditFlag) { this.creditFlag = creditFlag; }
    public void setChannel(String channel) { this.channel = channel; }
    public void setQq(String qq) { this.qq = qq; }
    public void setBirthday(Date birthday) { this.birthday = birthday; }
    public void setCertCode(String certCode) { this.certCode = certCode; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}