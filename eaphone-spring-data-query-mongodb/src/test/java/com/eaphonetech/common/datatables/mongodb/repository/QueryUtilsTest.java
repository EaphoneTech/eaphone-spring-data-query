package com.eaphonetech.common.datatables.mongodb.repository;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Test;

public class QueryUtilsTest {

    private void testGetLikeFilterPattern(String like, String expected) {
        Pattern converted = QueryUtils.getLikeFilterPattern(like);
        String actual = converted == null ? null : converted.pattern();
        assertEquals(expected, actual);
    }

    @Test
    public void testGetLikeFilterPattern() {
        testGetLikeFilterPattern(null, null);
        testGetLikeFilterPattern("", "^$");
        testGetLikeFilterPattern("bcd", "^bcd$");
        testGetLikeFilterPattern("%bcd", ".*bcd$");
        testGetLikeFilterPattern("bcd%", "^bcd.*");
        testGetLikeFilterPattern("%bcd%", ".*bcd.*");
        testGetLikeFilterPattern(".+", "\\.\\+");
    }

}
