package com.ypat.service;

import com.ypat.MemberBenefitQuoteQo;
import com.ypat.entity.MemberBenefitRule;
import com.ypat.entity.UserMember;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

public class MemberBenefitCalculatorTest {
    @Test
    public void activeBasicMemberGetsSubmitDiscount() {
        MemberBenefitCalculator c = new MemberBenefitCalculator();
        MemberBenefitRule rule = rule(2, 0, "1", "1");
        MemberBenefitQuoteQo q = c.calculate("SUBMIT_YPAT", 5, activeMember(), rule);
        assertTrue(q.getMemberActive());
        assertEquals(Integer.valueOf(5), q.getOriginalPpd());
        assertEquals(Integer.valueOf(2), q.getDiscountPpd());
        assertEquals(Integer.valueOf(3), q.getActualPpd());
        assertTrue(q.getRuleEffective());
    }

    @Test
    public void discountNeverDropsBelowMinimum() {
        MemberBenefitCalculator c = new MemberBenefitCalculator();
        MemberBenefitQuoteQo q = c.calculate("SUBMIT_YPAT", 5, activeMember(), rule(9, 2, "1", "1"));
        assertEquals(Integer.valueOf(2), q.getActualPpd());
    }

    @Test
    public void expiredMemberGetsNoDiscount() {
        MemberBenefitCalculator c = new MemberBenefitCalculator();
        MemberBenefitQuoteQo q = c.calculate("SUBMIT_YPAT", 5, expiredMember(), rule(2, 0, "1", "1"));
        assertFalse(q.getMemberActive());
        assertEquals(Integer.valueOf(0), q.getDiscountPpd());
        assertEquals(Integer.valueOf(5), q.getActualPpd());
    }

    @Test
    public void disabledRuleGetsNoDiscount() {
        MemberBenefitCalculator c = new MemberBenefitCalculator();
        MemberBenefitQuoteQo q = c.calculate("SUBMIT_YPAT", 5, activeMember(), rule(2, 0, "1", "0"));
        assertEquals(Integer.valueOf(0), q.getDiscountPpd());
        assertEquals(Integer.valueOf(5), q.getActualPpd());
        assertFalse(q.getRuleEffective());
    }

    @Test
    public void mismatchedRuleLevelGetsNoDiscount() {
        MemberBenefitCalculator c = new MemberBenefitCalculator();
        MemberBenefitRule rule = rule(2, 0, "1", "1");
        rule.setLevelCode("VIP");
        MemberBenefitQuoteQo q = c.calculate("SUBMIT_YPAT", 5, activeMember(), rule);
        assertEquals(Integer.valueOf(0), q.getDiscountPpd());
        assertEquals(Integer.valueOf(5), q.getActualPpd());
        assertFalse(q.getRuleEffective());
    }

    @Test
    public void nullSceneGetsNoDiscount() {
        MemberBenefitCalculator c = new MemberBenefitCalculator();
        MemberBenefitQuoteQo q = c.calculate(null, 5, activeMember(), rule(2, 0, "1", "1"));
        assertEquals(Integer.valueOf(0), q.getDiscountPpd());
        assertEquals(Integer.valueOf(5), q.getActualPpd());
        assertFalse(q.getRuleEffective());
    }

    @Test
    public void nullRuleGetsNoDiscount() {
        MemberBenefitCalculator c = new MemberBenefitCalculator();
        MemberBenefitQuoteQo q = c.calculate("SUBMIT_YPAT", 5, activeMember(), null);
        assertEquals(Integer.valueOf(0), q.getDiscountPpd());
        assertEquals(Integer.valueOf(5), q.getActualPpd());
        assertFalse(q.getRuleEffective());
    }

    private static MemberBenefitRule rule(int discount, int min, String effective, String status) {
        MemberBenefitRule r = new MemberBenefitRule();
        r.setLevelCode("BASIC");
        r.setScene("SUBMIT_YPAT");
        r.setBenefitType("PPD_DISCOUNT");
        r.setDiscountPpd(discount);
        r.setMinActualPpd(min);
        r.setEffective(effective);
        r.setStatus(status);
        return r;
    }

    private static UserMember activeMember() {
        UserMember m = new UserMember();
        m.setUserId(1L);
        m.setLevel("BASIC");
        m.setExpireAt(daysFromNow(1));
        return m;
    }

    private static UserMember expiredMember() {
        UserMember m = activeMember();
        m.setExpireAt(daysFromNow(-1));
        return m;
    }

    private static Date daysFromNow(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, days);
        return c.getTime();
    }
}
