package com.ypat.identity.domain;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_user_identity")
public class UserIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Lob
    @Column(name = "encrypted_blob", nullable = false)
    private byte[] encryptedBlob;

    @Column(name = "key_id", nullable = false, length = 64)
    private String keyId;

    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "submitted_at")
    private Date submittedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "reviewed_at")
    private Date reviewedAt;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public byte[] getEncryptedBlob() { return encryptedBlob; }
    public String getKeyId() { return keyId; }
    public String getStatus() { return status; }
    public Date getSubmittedAt() { return submittedAt; }
    public Date getReviewedAt() { return reviewedAt; }
    public Long getReviewerId() { return reviewerId; }
    public String getRejectReason() { return rejectReason; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setEncryptedBlob(byte[] b) { this.encryptedBlob = b; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
    public void setStatus(String status) { this.status = status; }
    public void setSubmittedAt(Date submittedAt) { this.submittedAt = submittedAt; }
    public void setReviewedAt(Date reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}