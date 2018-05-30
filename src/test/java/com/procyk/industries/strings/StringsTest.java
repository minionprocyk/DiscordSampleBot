package com.procyk.industries.strings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringsTest {
    Strings strings = new Strings();
    @Test
    public void testStringMatcher() {
        String text = "!imdieing";
        String match = "!imdying";
        int allowedErrors = 2;

        assertTrue(
                Strings.almostMatchesByFrontAndBackIgnoringLength().almostMatches(text,match,allowedErrors)
        );

        text = "!imdieing123";
        assertFalse(
                Strings.almostMatchesByFrontAndBackIgnoringLength().almostMatches(text,match,allowedErrors)
        );

    }
    @Test
    public void testStringJointMatcher() {
        String text = "!cunt";
        String match = "!imdieing";
        int allowedErrors=2;

        assertFalse(strings.almostMatches(text,match,allowedErrors)) ;
        assertTrue(strings.almostMatches("!catdog","!catdawg",allowedErrors));
    }
}
