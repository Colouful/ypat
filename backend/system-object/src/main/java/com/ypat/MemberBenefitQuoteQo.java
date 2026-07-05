package com.ypat;

import java.io.Serializable;

public class MemberBenefitQuoteQo implements Serializable {
    private String scene;
    private Boolean memberActive;
    private String levelCode;
    private Integer originalPpd;
    private Integer discountPpd;
    private Integer actualPpd;
    private Boolean ruleEffective;

    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public Boolean getMemberActive() { return memberActive; }
    public void setMemberActive(Boolean memberActive) { this.memberActive = memberActive; }
    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    public Integer getOriginalPpd() { return originalPpd; }
    public void setOriginalPpd(Integer originalPpd) { this.originalPpd = originalPpd; }
    public Integer getDiscountPpd() { return discountPpd; }
    public void setDiscountPpd(Integer discountPpd) { this.discountPpd = discountPpd; }
    public Integer getActualPpd() { return actualPpd; }
    public void setActualPpd(Integer actualPpd) { this.actualPpd = actualPpd; }
    public Boolean getRuleEffective() { return ruleEffective; }
    public void setRuleEffective(Boolean ruleEffective) { this.ruleEffective = ruleEffective; }
}
