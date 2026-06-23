package com.ypat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "t_user")
@DynamicInsert
@DynamicUpdate
@NamedEntityGraph(
        name="User.all",
        attributeNodes = {
                @NamedAttributeNode(value = "userImgs", subgraph = "")
        })
public class User implements java.io.Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String gender;
    private String nickname;
    private String profess;
    private String mobile;
    private String wx;
    private String qq;
    private String wb;
    private String name;
    private String certcode;
    private Integer ppd;
    private String avatarurl;
    private String realnameflag;
    private String creditflag;
    private Integer pubtimes;
    private Integer rectimes;
    private Integer coltimes;
    private String recmobile;
    private String status;
    private String province;
    private String city;
    private String area;
    private String openid;
    private String password;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "regisdate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date regisdate;
    @ManyToMany(cascade = {},fetch = FetchType.LAZY)
    @JoinTable(name = "t_user_ypat",
            joinColumns = {@JoinColumn(name = "userid")},
            inverseJoinColumns = {@JoinColumn(name = "ypatid")})
    private List<YpatInfo> ypatInfos;
    @OneToMany
    @JoinColumn(name = "userid")
    @JsonIgnore
    private List<UserImg> userImgs;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "birthday")
    @Temporal(TemporalType.TIMESTAMP)
    private Date birthday;
    private String channel;

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfess() {
        return profess;
    }

    public void setProfess(String profess) {
        this.profess = profess;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getWx() {
        return wx;
    }

    public void setWx(String wx) {
        this.wx = wx;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getWb() {
        return wb;
    }

    public void setWb(String wb) {
        this.wb = wb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCertcode() {
        return certcode;
    }

    public void setCertcode(String certcode) {
        this.certcode = certcode;
    }

    public Integer getPpd() {
        return ppd;
    }

    public void setPpd(Integer ppd) {
        this.ppd = ppd;
    }

    public String getAvatarurl() {
        return avatarurl;
    }

    public void setAvatarurl(String avatarurl) {
        this.avatarurl = avatarurl;
    }

    public String getRealnameflag() {
        return realnameflag;
    }

    public void setRealnameflag(String realnameflag) {
        this.realnameflag = realnameflag;
    }

    public String getCreditflag() {
        return creditflag;
    }

    public void setCreditflag(String creditflag) {
        this.creditflag = creditflag;
    }

    public Integer getPubtimes() {
        return pubtimes;
    }

    public void setPubtimes(Integer pubtimes) {
        this.pubtimes = pubtimes;
    }

    public Integer getRectimes() {
        return rectimes;
    }

    public void setRectimes(Integer rectimes) {
        this.rectimes = rectimes;
    }

    public Integer getColtimes() {
        return coltimes;
    }

    public void setColtimes(Integer coltimes) {
        this.coltimes = coltimes;
    }

    public String getRecmobile() {
        return recmobile;
    }

    public void setRecmobile(String recmobile) {
        this.recmobile = recmobile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public Date getRegisdate() {
        return regisdate;
    }

    public void setRegisdate(Date regisdate) {
        this.regisdate = regisdate;
    }

    public List<YpatInfo> getYpatInfos() {
        return ypatInfos;
    }

    public void setYpatInfos(List<YpatInfo> ypatInfos) {
        this.ypatInfos = ypatInfos;
    }

    public List<UserImg> getUserImgs() {
        return userImgs;
    }

    public void setUserImgs(List<UserImg> userImgs) {
        this.userImgs = userImgs;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
