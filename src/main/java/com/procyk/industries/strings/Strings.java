package com.procyk.industries.strings;

import java.util.ArrayList;
import java.util.List;

public class Strings implements StringMatcher{

    /**
     * Matches char ignoring case
     * @param a char
     * @param b char
     * @return True if a matches b irrespective of case
     */
    private static boolean charAlmostMatches(char a, char b) {
        return Character.toLowerCase(a) == Character.toLowerCase(b);
    }

    /**
     * An implementation of StringMatcher where all characters are determined to almost match in sequential order
     * from left to right up to the length of the smallest input as determined by {@link #charAlmostMatches(char, char)}
     * @return A StringMatcher who matches character from left to right.
     */
    private static StringMatcher almostMatchesByFront() {
        return (text,match,allowedErrors) -> {
            char[] textChars = text.toCharArray();
            char[] matchChars = match.toCharArray();
            int minLength = Math.min(textChars.length,matchChars.length);
            int maxLength = Math.max(textChars.length,matchChars.length);
            int errors = maxLength-minLength;

            for(int i=0;i<minLength;i++) {
                char tc = textChars[i];
                char mc = matchChars[i];
                if(!charAlmostMatches(tc,mc)
                && (++errors > allowedErrors)) {
                        return false;
                }
            }
            return true;
        };
    }
    /**
     * An implementation of StringMatcher where all characters are determined to almost match if the first half of n
     * characters and last half of n characters of the smallest input are determined to almost match by {@link #charAlmostMatches(char, char)}.
     * If the smallest length input is an odd number, then the middle character is checked against both the left and the right
     * side of the larger length input, and if either matches then it is determined to almost match.
     *
     * @return A StringMatcher who matches character from left to right and right to left.
     */
     static StringMatcher almostMatchesByFrontAndBackIgnoringLength() {
        return (text,match,allowedErrors) -> {
            char[] textChars = text.toCharArray();
            char[] matchChars = match.toCharArray();
            int minLength = Math.min(textChars.length,matchChars.length);
            int maxLength  = Math.max(textChars.length,matchChars.length);
            int errors = maxLength-minLength;
            if(minLength<4 || errors >= allowedErrors)
                return false;
            errors=0;
            //determine how many times we go from left->right and right->left
            //lets travel the least length / 2 where the odd character will match from the left or right
            for(int i=0;i<minLength/2;i++) {
                char tc = textChars[i];
                char mc = matchChars[i];
                if(!charAlmostMatches(tc,mc) && (++errors > allowedErrors))
                    return false;
            }
            int textCharsIndex = textChars.length-1;
            int matchCharsIndex = matchChars.length-1;
            for(int i=0;i<minLength/2;i++,textCharsIndex--,matchCharsIndex--) {
                char tc = textChars[textCharsIndex];
                char mc = matchChars[matchCharsIndex];
                if(!charAlmostMatches(tc,mc) && (++errors > allowedErrors))
                        return false;
            }
            //observe the middle character of the lower length array and match that to the left or right of the larger one
            if(minLength%2!=0) {
                char[] lowerArray = textChars.length < matchChars.length ? textChars : matchChars;
                char[] higherArray = textChars.length >= matchChars.length ? textChars : matchChars;
                char compare = lowerArray[lowerArray.length/2];

                int fromLeft = minLength/2;
                int fromRight = higherArray.length-fromLeft;
                if(!charAlmostMatches(compare,higherArray[fromLeft])
                && !charAlmostMatches(compare, higherArray[fromRight]))
                    return ++errors <= allowedErrors;
            }
            return true;
        };
    }

    /**
     * Converts a given text into a list of strings broken out as chunks.
     * @param text Input Text
     * @param threshold Number of chars to chunk together
     * @return A collection of chunked items inside the text by the given threshold.
     */
    private static List<String> makeWordChunkWithThreshold(String text, int threshold) {
        List<String> matchChunks = new ArrayList<>();
        for(int i=0;i<text.length()-threshold+1;i++) {
            String matchChunk = text.substring(i,threshold+i);
            matchChunks.add(matchChunk);
        }
        return matchChunks;
    }
    /**
     * Matches longer text using chunks chars.
     * @return A StringMatcher who matches text on a chunk of chars
     */
    private static StringMatcher almostMatchesByWord() {
        final int threshold=4;
        return (text, match, allowedErrors) -> {
            if(text.length() < threshold || match.length() < threshold)
                return false;
            List<String> matchChunks = makeWordChunkWithThreshold(match,threshold);

            for(int i=0;i<text.length()-threshold+1;i++) {
                String textChunk = text.substring(i,threshold+i);
                boolean matched = matchChunks.stream()
                                            .anyMatch(s -> s.equalsIgnoreCase(textChunk));
                if(matched)return true;
            }
            return false;
        };
    }

    public static boolean isBlank(String value) {
        return null == value || "".equals(value);
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static boolean isEmpty(String value) {
        return isBlank(value) || isBlank(value.replaceAll("\\s",""));
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    public static String defaultIfBlank(String value, String strDefault) {
        return isBlank(value) ? strDefault : value;
    }

    public static boolean containsWhitespace(String value) {
        return value.contains(" ");
    }


    /**
     *  Performs a more relaxed string search to check if two strings are equal. Ignores casing and allowed n number of errors.
     *  Will also match if the beginning of the match string loosely matches the beginning of the text or if each end
     *  of a given string matches.
     * @param text Input Text
     * @param match Match to
     * @param allowedErrors Error Threshold
     * @return True if match almost matches a given input text without exceeding allowedErrors.
     */
    @Override
    public  boolean almostMatches(String text, String match, int allowedErrors) {
        return almostMatchesByFront().almostMatches(text,match,allowedErrors)
                || almostMatchesByFrontAndBackIgnoringLength().almostMatches(text,match,allowedErrors)
                || almostMatchesByWord().almostMatches(text, match, allowedErrors);
    }
}
