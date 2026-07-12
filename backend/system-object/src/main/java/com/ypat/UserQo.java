package com.ypat;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.ypat.enums.UserGender;
import com.ypat.enums.UserOrigType;
import com.ypat.enums.UserProfess;
import com.ypat.enums.UserStatus;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Size;
import java.util.Date;

public class UserQo extends PageQo implements java.io.Serializable{

    private Long id;
    private String gender;
    private String nickname;
    @Size(max = 2, message = "profess最大两位")
    private String profess;
    private String mobile;
    private String wx;
    private String qq;
    private String wb;
    private String name;
    private String certcode;
    private Integer ppd;
    private String avatarurl;
    @Size(max = 1, message = "realnameflag最大一位")
    private String realnameflag;
    @Size(max = 1, message = "creditflag最大一位")
    private String creditflag;
    private Boolean memberActive;
    private String memberLevel;
    private Integer pubtimes;
    private Integer rectimes;
    private Integer coltimes;
    private String recmobile;
    /** 邀请码（base36 编码的邀请人 user.id），优先级高于 recmobile。 */
    private String inviteCode;
    /** 邀请入口来源：share / qr / manual / recmobile，供运营回溯。 */
    private String inviteSource;
    private String status;
    private String province;
    private String city;
    private String area;
    private String openid;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date regisdate;
    private String password;
    private String imgpath;

    private String encryptedData;
    private String sessionKey;
    private String iv;
    private String smsCode;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;
    private String statusTxt;
    private String genderTxt;
    private String professTxt;
    private String channel;
    private String channelTxt;
    private String dataFlag;
    private String internalBatchNo;

    public String getChannelTxt() {
        return UserOrigType.getNameByCode(this.channel);
    }

    public void setChannelTxt(String channelTxt) {
        this.channelTxt = channelTxt;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDataFlag() {
        return dataFlag;
    }

    public void setDataFlag(String dataFlag) {
        this.dataFlag = dataFlag;
    }

    public String getInternalBatchNo() {
        return internalBatchNo;
    }

    public void setInternalBatchNo(String internalBatchNo) {
        this.internalBatchNo = internalBatchNo;
    }

    public String getProfessTxt() {
        return UserProfess.getNameByCode(this.profess);
    }

    public void setProfessTxt(String professTxt) {
        this.professTxt = professTxt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public UserQo() {
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

    public Boolean getMemberActive() {
        return memberActive;
    }

    public void setMemberActive(Boolean memberActive) {
        this.memberActive = memberActive;
    }

    public String getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(String memberLevel) {
        this.memberLevel = memberLevel;
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

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getInviteSource() {
        return inviteSource;
    }

    public void setInviteSource(String inviteSource) {
        this.inviteSource = inviteSource;
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

    public String getImgpath() {
        return imgpath;
    }

    public void setImgpath(String imgpath) {
        this.imgpath = imgpath;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getStatusTxt() {
        return UserStatus.getNameByCode(this.status);
    }

    public void setStatusTxt(String statusTxt) {
        this.statusTxt = statusTxt;
    }

    public String getGenderTxt() {
        return UserGender.getNameByCode(this.gender);
    }

    public void setGenderTxt(String genderTxt) {
        this.genderTxt = genderTxt;
    }

    @Override
    public String toString() {
        return "UserQo{" +
                "id=" + id +
                ", gender='" + gender + '\'' +
                ", nickname='" + nickname + '\'' +
                ", profess='" + profess + '\'' +
                ", mobile='" + mobile + '\'' +
                ", wx='" + wx + '\'' +
                ", qq='" + qq + '\'' +
                ", wb='" + wb + '\'' +
                ", name='" + name + '\'' +
                ", certcode='" + certcode + '\'' +
                ", ppd=" + ppd +
                ", avatarurl='" + avatarurl + '\'' +
                ", realnameflag='" + realnameflag + '\'' +
                ", creditflag='" + creditflag + '\'' +
                ", memberActive=" + memberActive +
                ", memberLevel='" + memberLevel + '\'' +
                ", pubtimes=" + pubtimes +
                ", rectimes=" + rectimes +
                ", coltimes=" + coltimes +
                ", recmobile='" + recmobile + '\'' +
                ", status='" + status + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", area='" + area + '\'' +
                ", openid='" + openid + '\'' +
                ", regisdate=" + regisdate +
                ", password='" + password + '\'' +
                ", encryptedData='" + encryptedData + '\'' +
                ", sessionKey='" + sessionKey + '\'' +
                ", iv='" + iv + '\'' +
                ", birthday='" + birthday + '\'' +
                ", channel='" + channel + '\'' +
                '}';
    }
}
