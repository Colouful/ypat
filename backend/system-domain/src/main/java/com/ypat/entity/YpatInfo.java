package com.ypat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "t_ypat_info")
@DynamicInsert
@DynamicUpdate
@NamedEntityGraph(
        name="YpatInfo.all",
        attributeNodes = {
                @NamedAttributeNode(value = "user")
        })
public class YpatInfo implements java.io.Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String describ;
    private String target;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "patdate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date patdate;
    private String patarea;
    private String patslice;
    private String chargeway;
    private BigDecimal chargeamt;
    private String province;
    private String city;
    private String area;
    private String creditflag;
    private String realnameflag;
    private String patstyle;
    private String status;
    private BigDecimal longitude;
    private BigDecimal latitude;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "pubdate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date pubdate;
    private Integer readtimes;
    private Integer pattimes;
    private Integer coltimes;
    @ManyToOne
    @JoinColumn(name = "userid")
    @JsonIgnore
    private User user;
    @OneToMany
    @JoinColumn(name = "ypatid")
    @JsonIgnore
    private List<YpatImg> ypatImgs;
    private String recomflag;
    private String reason;
    private Integer isNationwide;
    private Long workId;

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

    public List<YpatImg> getYpatImgs() {
        return ypatImgs;
    }

    public void setYpatImgs(List<YpatImg> ypatImgs) {
        this.ypatImgs = ypatImgs;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Integer getIsNationwide() {
        return isNationwide;
    }

    public void setIsNationwide(Integer isNationwide) {
        this.isNationwide = isNationwide;
    }

    public Long getWorkId() {
        return workId;
    }

    public void setWorkId(Long workId) {
        this.workId = workId;
    }
}
