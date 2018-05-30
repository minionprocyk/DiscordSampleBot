package com.procyk.industries.strings;

import java.util.regex.Pattern;

/**
 * Functional Interface for returning a given string based on a provided input.
 * @see com.procyk.industries.command.CommandParser#searchAndReplace(String, Pattern, StringModifier)
 */
public interface StringModifier {
    String apply(String input);
}
