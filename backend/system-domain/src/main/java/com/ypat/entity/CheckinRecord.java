package com.ypat.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_checkin_record",
        uniqueConstraints = @UniqueConstraint(name = "uniq_user_checkin_date", columnNames = {"userid", "checkin_date"}))
public class CheckinRecord implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userid;
    @Column(name = "checkin_date", nullable = false, length = 10)
    private String checkinDate;
    @Column(name = "reward_ppd", nullable = false)
    private Integer rewardPpd;
    @Column(name = "record_id")
    private Long recordId;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserid() { return userid; }
    public void setUserid(Long userid) { this.userid = userid; }
    public String getCheckinDate() { return checkinDate; }
    public void setCheckinDate(String checkinDate) { this.checkinDate = checkinDate; }
    public Integer getRewardPpd() { return rewardPpd; }
    public void setRewardPpd(Integer rewardPpd) { this.rewardPpd = rewardPpd; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
