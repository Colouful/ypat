package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ypat.enums.YpatChargeWay;
import com.ypat.enums.YpatPatstyle;
import com.ypat.enums.YpatStatus;
import com.ypat.enums.YpatTarget;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class YpatInfoQo extends PageQo implements java.io.Serializable {
    private Long id;
    @NotEmpty(message = "describ不能为空")
    private String describ;
    @NotEmpty(message = "target不能为空")
    @Size(max = 1, message = "target最大一位")
    private String target;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date patdate;
    private String patarea;
    private String patslice;
    @NotEmpty(message = "chargeway不能为空")
    @Size(max = 1, message = "chargeway最大一位")
    private String chargeway;
    private BigDecimal chargeamt;
    @NotEmpty(message = "province不能为空")
    private String province;
    @NotEmpty(message = "city不能为空")
    private String city;
    private String area;
    @Size(max = 1, message = "creditflag最大一位")
    private String creditflag;
    @Size(max = 1, message = "realnameflag最大一位")
    private String realnameflag;
    @Size(max = 20, message = "patstyle最二十位")
    private String patstyle;
    private String status;
    private BigDecimal longitude;
    private BigDecimal latitude;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date pubdate;
    private Integer readtimes;
    private Integer pattimes;
    private Integer coltimes;
    private UserQo userQo;
    private Long userid;
    private String timeStr;;
    private List<String> pics;
    private String colflag;
    private String recomflag;
    private String reason;
    private String msgflag;

    private String chargewayTxt;
    private String targetTxt;
    private String patstyleTxt;
    private String statusTxt;

    /** 是否全国（0否/1是） */
    private String isNationwide;
    /** 来自作品发起的约拍（关联 t_work.id） */
    private String workId;

    private String profess;
    private String gender;
    private String mobile;
    private String nickname;
    private String imgpath;
    private String dataFlag;
    private String internalBatchNo;

    public String getImgpath() {
        return imgpath;
    }

    public void setImgpath(String imgpath) {
        this.imgpath = imgpath;
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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<String> getPics() {
        return pics;
    }

    public void setPics(List<String> pics) {
        this.pics = pics;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescrib() {
        return describ;
    }

    public void setDescrib(String describ) {
        this.describ = describ;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Date getPatdate() {
        return patdate;
    }

    public void setPatdate(Date patdate) {
        this.patdate = patdate;
    }

    public String getPatarea() {
        return patarea;
    }

    public void setPatarea(String patarea) {
        this.patarea = patarea;
    }

    public String getPatslice() {
        return patslice;
    }

    public void setPatslice(String patslice) {
        this.patslice = patslice;
    }

    public String getChargeway() {
        return chargeway;
    }

    public void setChargeway(String chargeway) {
        this.chargeway = chargeway;
    }

    public BigDecimal getChargeamt() {
        return chargeamt;
    }

    public void setChargeamt(BigDecimal chargeamt) {
        this.chargeamt = chargeamt;
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

    public String getCreditflag() {
        return creditflag;
    }

    public void setCreditflag(String creditflag) {
        this.creditflag = creditflag;
    }

    public String getRealnameflag() {
        return realnameflag;
    }

    public void setRealnameflag(String realnameflag) {
        this.realnameflag = realnameflag;
    }

    public String getPatstyle() {
        return patstyle;
    }

    public void setPatstyle(String patstyle) {
        this.patstyle = patstyle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public Date getPubdate() {
        return pubdate;
    }

    public void setPubdate(Date pubdate) {
        this.pubdate = pubdate;
    }

    public Integer getReadtimes() {
        return readtimes;
    }

    public void setReadtimes(Integer readtimes) {
        this.readtimes = readtimes;
    }

    public Integer getPattimes() {
        return pattimes;
    }

    public void setPattimes(Integer pattimes) {
        this.pattimes = pattimes;
    }

    public Integer getColtimes() {
        return coltimes;
    }

    public void setColtimes(Integer coltimes) {
        this.coltimes = coltimes;
    }

    public UserQo getUserQo() {
        return userQo;
    }

    public void setUserQo(UserQo userQo) {
        this.userQo = userQo;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public String getColflag() {
        return colflag;
    }

    public void setColflag(String colflag) {
        this.colflag = colflag;
    }

    public String getMsgflag() {
        return msgflag;
    }

    public void setMsgflag(String msgflag) {
        this.msgflag = msgflag;
    }

    public String getChargewayTxt() {
        return YpatChargeWay.getNameByCode(this.chargeway);
    }

    public void setChargewayTxt(String chargewayTxt) {
        this.chargewayTxt = chargewayTxt;
    }

    public String getTargetTxt() {
        return YpatTarget.getNameByCode(this.target);
    }

    public void setTargetTxt(String targetTxt) {
        this.targetTxt = targetTxt;
    }

    public String getPatstyleTxt() {
        if(!StringUtils.isEmpty(this.patstyle)) {
            StringBuilder styleName = new StringBuilder();
            String[] styles = this.patstyle.split(",");
            for (String style : styles) {
                styleName.append(YpatPatstyle.getNameByCode(style))
                         .append(",");
            }
            patstyleTxt = styleName.substring(0, styleName.length()-1);
        }
        return patstyleTxt;
    }

    public void setPatstyleTxt(String patstyleTxt) {
        this.patstyleTxt = patstyleTxt;
    }

    public String getStatusTxt() {
        return YpatStatus.getNameByCode(this.status);
    }

    public void setStatusTxt(String statusTxt) {
        this.statusTxt = statusTxt;
    }

    public String getRecomflag() {
        return recomflag;
    }

    public void setRecomflag(String recomflag) {
        this.recomflag = recomflag;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getIsNationwide() {
        return isNationwide;
    }

    public void setIsNationwide(String isNationwide) {
        this.isNationwide = isNationwide;
    }

    public String getWorkId() {
        return workId;
    }

    public void setWorkId(String workId) {
        this.workId = workId;
    }

    @Override
    public String toString() {
        return "YpatInfoQo{" +
                "id=" + id +
                ", describ='" + describ + '\'' +
                ", target='" + target + '\'' +
                ", patdate=" + patdate +
                ", patarea='" + patarea + '\'' +
                ", patslice='" + patslice + '\'' +
                ", chargeway='" + chargeway + '\'' +
                ", chargeamt=" + chargeamt +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", area='" + area + '\'' +
                ", creditflag='" + creditflag + '\'' +
                ", realnameflag='" + realnameflag + '\'' +
                ", patstyle='" + patstyle + '\'' +
                ", status='" + status + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", pubdate=" + pubdate +
                ", readtimes=" + readtimes +
                ", pattimes=" + pattimes +
                ", coltimes=" + coltimes +
                ", userQo=" + userQo +
                ", userid=" + userid +
                ", timeStr='" + timeStr + '\'' +
                '}';
    }
}
