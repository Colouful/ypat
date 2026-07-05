package com.ypat.service;

import com.ypat.MemberBenefitQuoteQo;
import com.ypat.entity.MemberBenefitRule;
import com.ypat.entity.UserMember;

import java.util.Date;
import java.util.Objects;

public class MemberBenefitCalculator {
    public static final String LEVEL_BASIC = "BASIC";
    public static final String BENEFIT_TYPE_PPD_DISCOUNT = "PPD_DISCOUNT";

    public MemberBenefitQuoteQo calculate(String scene, int originalPpd, UserMember member, MemberBenefitRule rule) {
        MemberBenefitQuoteQo q = new MemberBenefitQuoteQo();
        q.setScene(scene);
        q.setOriginalPpd(originalPpd);
        boolean memberActive = isActiveBasic(member);
        q.setMemberActive(memberActive);
        q.setLevelCode(memberActive ? member.getLevel() : null);
        boolean ruleEffective = memberActive
                && rule != null
                && Objects.equals(member.getLevel(), rule.getLevelCode())
                && Objects.equals(scene, rule.getScene())
                && BENEFIT_TYPE_PPD_DISCOUNT.equals(rule.getBenefitType())
                && "1".equals(rule.getEffective())
                && "1".equals(rule.getStatus());
        q.setRuleEffective(ruleEffective);
        int discount = ruleEffective && rule.getDiscountPpd() != null ? Math.max(0, rule.getDiscountPpd()) : 0;
        int min = ruleEffective && rule.getMinActualPpd() != null ? Math.max(0, rule.getMinActualPpd()) : 0;
        q.setDiscountPpd(discount);
        q.setActualPpd(Math.max(min, Math.max(0, originalPpd - discount)));
        return q;
    }

    private boolean isActiveBasic(UserMember member) {
        return member != null
                && LEVEL_BASIC.equals(member.getLevel())
                && member.getExpireAt() != null
                && member.getExpireAt().after(new Date());
    }
}
