package com.procyk.industries.strings;

/**
 * Defines a set of rules that will almost match a given string.
 */
interface StringMatcher {
    boolean almostMatches(String text, String match, int allowedErrors);
}
