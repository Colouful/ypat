package com.ypat;

import java.io.Serializable;

public class MemberBenefitRuleQo extends PageQo implements Serializable {
    private Long id;
    private String levelCode;
    private String scene;
    private String benefitType;
    private Integer discountPpd;
    private Integer minActualPpd;
    private String effective;
    private String status;
    private String description;

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
}
