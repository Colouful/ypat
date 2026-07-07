package com.ypat.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_work_complain")
@DynamicInsert
@DynamicUpdate
public class WorkComplain implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "work_id")
    private Long workId;
    @Column(name = "user_id")
    private Long userId;
    private String reason;
    private String contact;
    private String pics;
    private String status;
    @Column(name = "handle_reason")
    private String handleReason;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkId() { return workId; }
    public void setWorkId(Long workId) { this.workId = workId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getPics() { return pics; }
    public void setPics(String pics) { this.pics = pics; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getHandleReason() { return handleReason; }
    public void setHandleReason(String handleReason) { this.handleReason = handleReason; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
