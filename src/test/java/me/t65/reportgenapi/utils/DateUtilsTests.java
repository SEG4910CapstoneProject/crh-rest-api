package me.t65.reportgenapi.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class DateUtilsTests {

    @Test
    public void testIsValidDate_validDate() {
        assertTrue(DateUtils.isValidDate("2024-01-01"));
    }

    @Test
    public void testIsValidDate_invalidDate() {
        assertFalse(DateUtils.isValidDate("2024-13-01"));
    }

    @Test
    public void testIsValidDate_emptyDate() {
        assertFalse(DateUtils.isValidDate(""));
    }

    @Test
    public void testIsValidDate_nullDate() {
        assertThrows(NullPointerException.class, () -> DateUtils.isValidDate(null));
    }

    @Test
    public void testIsValidType_validType() {
        assertTrue(DateUtils.isValidType("DAILY"));
    }

    @Test
    public void testIsValidType_invalidType() {
        assertFalse(DateUtils.isValidType("monthly"));
    }

    @Test
    public void testIsValidType_nullType() {
        assertFalse(DateUtils.isValidType(null));
    }

    @Test
    public void testIsValidSearch_validRange() {
        assertTrue(DateUtils.isValidSearch("2024-01-01", "2024-01-10"));
    }

    @Test
    public void testIsValidSearch_invalidRange() {
        assertFalse(DateUtils.isValidSearch("2024-01-10", "2024-01-01"));
    }

    @Test
    public void testIsValidSearch_invalidStartDate() {
        assertFalse(DateUtils.isValidSearch("invalid-date", "2024-01-01"));
    }

    @Test
    public void testIsValidSearch_invalidEndDate() {
        assertFalse(DateUtils.isValidSearch("2024-01-01", "invalid-date"));
    }

    @Test
    public void testIsValidSearch_nullStartDate() {
        assertThrows(NullPointerException.class, () -> DateUtils.isValidSearch(null, "2024-01-01"));
    }

    @Test
    public void testIsValidSearch_nullEndDate() {
        assertThrows(NullPointerException.class, () -> DateUtils.isValidSearch("2024-01-01", null));
    }
}
