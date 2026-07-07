package com.ypat.enums;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserProfessTest {
    @Test
    public void publicProfessOptionsOnlyExposeSixCurrentRoles() {
        List<String> values = UserProfess.getPublicValues();
        assertEquals(Arrays.asList("6", "0", "2", "9", "3", "1"), values);
    }

    @Test
    public void displayNameKeepsHistoricalValuesAndRenamesMakeup() {
        assertEquals("化妆师", UserProfess.getNameByCode("2"));
        assertEquals("摄像师", UserProfess.getNameByCode("9"));
        assertEquals("个人", UserProfess.getNameByCode("4"));
        assertEquals("演员", UserProfess.getNameByCode("5"));
        assertEquals("其他", UserProfess.getNameByCode("7"));
        assertEquals("素人模特", UserProfess.getNameByCode("8"));
    }

    @Test
    public void validityAllowsNewVideographerAndHistoricalValues() {
        assertTrue(UserProfess.isValid("9"));
        assertTrue(UserProfess.isValid("8"));
    }
}
