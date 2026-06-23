package com.ypat;


public class UserLinkWayQo extends PageQo implements java.io.Serializable{

    private String nickname;
    private String profess;
    private String mobile;
    private String wx;
    private String qq;
    private String wb;
    private String name;

    public UserLinkWayQo() {
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
}
