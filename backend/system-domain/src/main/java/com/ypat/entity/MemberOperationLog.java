package com.ypat.entity;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(
        name = "t_member_operation_log",
        indexes = {
                @Index(name = "idx_user_created_at", columnList = "user_id, created_at"),
                @Index(name = "idx_operator_created_at", columnList = "operator_id, created_at")
        }
)
public class MemberOperationLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "action_type", nullable = false, length = 32)
    private String actionType;

    @Column(length = 256)
    private String reason;

    @Column(name = "before_value", length = 1024)
    private String beforeValue;

    @Column(name = "after_value", length = 1024)
    private String afterValue;

    @Column(name = "source_order_no", length = 64)
    private String sourceOrderNo;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getBeforeValue() { return beforeValue; }
    public void setBeforeValue(String beforeValue) { this.beforeValue = beforeValue; }
    public String getAfterValue() { return afterValue; }
    public void setAfterValue(String afterValue) { this.afterValue = afterValue; }
    public String getSourceOrderNo() { return sourceOrderNo; }
    public void setSourceOrderNo(String sourceOrderNo) { this.sourceOrderNo = sourceOrderNo; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
