package com.procyk.industries.strings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringsTest {
    private final Strings strings = new Strings();
    int allowedErrors=2;
    @Test
    void testStringMatcher() {
        String text = "!imdieing";
        String match = "!imdying";

        assertTrue(
                Strings.almostMatchesByFrontAndBackIgnoringLength().almostMatches(text,match,allowedErrors)
        );

        text = "!imdieing123";
        assertFalse(
                Strings.almostMatchesByFrontAndBackIgnoringLength().almostMatches(text,match,allowedErrors)
        );

    }
    @Test
    void testStringJointMatcher() {
        String text = "!cunt";
        String match = "!imdieing";

        assertFalse(strings.almostMatches(text,match,allowedErrors)) ;
        assertTrue(strings.almostMatches("!catdog","!catdawg",allowedErrors));
    }
    @Test
    void testWholeWordStringMatcher() {
        String text="!yugisambastudio";
        String match="!yugino";

        assertTrue(strings.almostMatches(text,match,allowedErrors));

        text="!sambastudioyugi";
        assertTrue(strings.almostMatches(text,match,allowedErrors));

        text="!sambayugistudio";
        assertTrue(strings.almostMatches(text,match,allowedErrors));
    }
    @Test
    void benchmarkStringMatcherForFasterVersion() {
        String text="!abcdefjkalsdflkjsdf";
        String match="zzzzzzzzzzzzzzzZZZ";
        strings.almostMatches(text,match,allowedErrors);

        int runs = 1;//1000000;
        long start = System.currentTimeMillis();
        for(int i=0;i<runs;i++) {
            strings.almostMatches(text,match,allowedErrors);
        }
        long end = System.currentTimeMillis();

//        System.out.println("Time elapsed: "+ (end-start)+"ms");
    }
}
