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
@Table(name = "t_member_benefit_rule",
        uniqueConstraints = @UniqueConstraint(name = "uk_level_scene_type", columnNames = {"level_code", "scene", "benefit_type"}))
public class MemberBenefitRule implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "level_code", nullable = false, length = 16)
    private String levelCode;
    @Column(nullable = false, length = 32)
    private String scene;
    @Column(name = "benefit_type", nullable = false, length = 32)
    private String benefitType;
    @Column(name = "discount_ppd", nullable = false)
    private Integer discountPpd;
    @Column(name = "min_actual_ppd", nullable = false)
    private Integer minActualPpd;
    @Column(nullable = false, length = 1)
    private String effective;
    @Column(nullable = false, length = 1)
    private String status;
    @Column(length = 256)
    private String description;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getBenefitType() { return benefitType; }
    public void setBenefitType(String benefitType) { this.benefitType = benefitType; }
    public Integer getDiscountPpd() { return discountPpd; }
    public void setDiscountPpd(Integer discountPpd) { this.discountPpd = discountPpd; }
    public Integer getMinActualPpd() { return minActualPpd; }
    public void setMinActualPpd(Integer minActualPpd) { this.minActualPpd = minActualPpd; }
    public String getEffective() { return effective; }
    public void setEffective(String effective) { this.effective = effective; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
