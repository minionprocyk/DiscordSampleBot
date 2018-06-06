package com.procyk.industries.command;

import com.procyk.industries.strings.StringModifier;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Executable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides static utility methods for parsing information from discord message strings
 */
public class CommandParser {
    private static final Logger logger = LoggerFactory.getLogger(CommandParser.class);
    private static final String reservedCommandRegex;
    private static final String reservedPlayerCommandRegex;
    private static final String subCommandRegex;
    private static final String singleLineCommandRegex;
    static {
        //generate reserved command or regex
        StringBuilder builder = new StringBuilder(100);
        for (ReservedCommand reservedCommand : ReservedCommand.values()) {
            builder.append("(\\b"+reservedCommand.name()+"\\b)|");
        }
        builder.deleteCharAt(builder.lastIndexOf("|"));
        reservedCommandRegex = builder.toString();

        //generate resrvedcommand.playercommand or regex
        builder = new StringBuilder(100);
        for (ReservedCommand.PlayerCommands reservedCommand : ReservedCommand.PlayerCommands.values()) {
            builder.append("(\\b"+reservedCommand.name()+"\\b)|");
        }
        builder.deleteCharAt(builder.lastIndexOf("|"));
        reservedPlayerCommandRegex = builder.toString();

        //generate single line command regex
        builder = new StringBuilder(100);
        for(ReservedCommand reservedCommand : ReservedCommand.values()) {
            if(reservedCommand.isSingleLineCommand()) {
                builder.append("(\\b").append(reservedCommand.name()).append("\\b)").append("|");
            }
        }
        builder.deleteCharAt(builder.lastIndexOf("|"));
        singleLineCommandRegex = builder.toString();
        //generate reservedcommand.allSubCommand or regex
        subCommandRegex=reservedPlayerCommandRegex;
    }

    public static final String capture_reservedCommand="reservedCommand";
    public static final String capture_line="line";
    public static final String capture_command="command";
    public static final String capture_value="value";
    public static final String userAddRegex = String.format("(?<%s>^![\\w]+)\\s(?<%s>.*)",capture_command,capture_value);
    public static final String userCommandRegex = String.format("(?<%s>^![\\w]+)",capture_command);

    public static final String commandArgumentsRegex = "((?<=\\s)\\w+=[^\\s]+)(?<!\\s)";
    public static final String getCapture_reservedPlayerCommand = String.format("((?<=!)(%s))", reservedPlayerCommandRegex);
    public static final String getCapture_reservedCommand = String.format("((?<=!)(%s))", reservedCommandRegex);
    public static final String allEncompasingRegex= String.format("(?<%s>(!(\\w+))\\s*(!(%s))?([^!]*)(?<!\\s))",capture_command,subCommandRegex);
    public static final String replaceDigitsAfterPlayLocalCommand = "(?<=\\Q!playlocal\\E\\s?)(\\d+)";
    /**
     * Additional delimiters that can be used to daisy chain commands
     */
    public static final String multiCommandDelimiterRegex = "\\s*((->)|($=>$))\\s*";

    public static final Pattern userAddCommandPattern = Pattern.compile(userAddRegex);
    public static final Pattern userCommandPattern = Pattern.compile(userCommandRegex);
    public static final Pattern allEncompasingPattern = Pattern.compile(allEncompasingRegex);
    public static final Pattern resrvedCommandPattern = Pattern.compile(getCapture_reservedCommand);
    public static final Pattern reservedPlayerCommandPattern = Pattern.compile(reservedPlayerCommandRegex);
    public static final Pattern optionalArgumentsPattern = Pattern.compile(commandArgumentsRegex);
    public static final Pattern replaceDigitsAfterPlayLocalCommandPattern = Pattern.compile(replaceDigitsAfterPlayLocalCommand);
//    public static final Pattern multiCommandPattern = Pattern.compile(multiCommandDelimiterRegex);

    private String prefix;
    private String[] multiDelimiters;
    @Inject
    public CommandParser(@Named("prefix") String prefix, @Named("multi_delimiter") String[] multiDelimiters) {
        this.prefix=prefix;
        this.multiDelimiters=multiDelimiters;
    }

    public static List<String> testRegexParse(String s, Pattern pattern) {
        Matcher matcher = pattern.matcher(s);
        List<String> result=new ArrayList<>();
        while(matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }
    public static List<String> testRegexSplit(String s, String regex) {
        return Arrays.asList(s.split(regex));
    }

    /**
     *
     * @param val
     * @return A long value representing milliseconds from a decimal.
     * 0 if any error occured
     */
    public static long parseSecondsToMillisDecimalFormat(String val) {
        DecimalFormat decimalFormat = new DecimalFormat();
        if(StringUtils.isNotBlank(val)) {
            try {
                return (long) (decimalFormat.parse(val).doubleValue()*(double)1000);
            } catch (ParseException e) {
                logger.error(e.getMessage(),e);
            }
        }
        return 0;
    }
    public static Command parseCommand(String value) {
        return parseCommand(value,false);
    }
    public static ReservedCommand.PlayerCommands parsePlayerCommand(String value) {
        if(StringUtils.isNotBlank(value)) {
            Matcher reservedCommandMatcher = reservedPlayerCommandPattern.matcher(value);
            if (reservedCommandMatcher.find() && reservedCommandMatcher.start()==1) {
                String strReservedCommand = reservedCommandMatcher.group();
                return ReservedCommand.PlayerCommands.valueOf(strReservedCommand);
            }
        }
        return ReservedCommand.PlayerCommands.error;
    }
    public static ReservedCommand parseReservedCommand(String value) {
        if(StringUtils.isNotBlank(value)) {
            Matcher reservedCommandMatcher = resrvedCommandPattern.matcher(value);
            if (reservedCommandMatcher.find() && reservedCommandMatcher.start()==1) {
                String strReservedCommand = reservedCommandMatcher.group();
                ReservedCommand reservedCommand = ReservedCommand.valueOf(strReservedCommand);
                return reservedCommand;
            }
            else {
                return ReservedCommand.user;
            }
        }
        return ReservedCommand.none;
    }
    /**
     * Parses a string of text using the reservedCommandRegex pattern {@link #userAddRegex} and attempts to return a Command based on the
     * submission. This method does not infer any reserved commands into the command object (which it should)
     * @param value The string to evaluate
     * @return A {@code Command } representing the input source
     */
    public static Command parseCommand(String value,boolean grabWholeTextAsValue) {
        Command returnCommand = new Command();
        Map<String,String> optionalArgsMap = new HashMap<>();
        String command = "";
        String entry = "";
//        if(StringUtils.isNotBlank(value)) {
//            Matcher reservedCommandMatcher = resrvedCommandPattern.matcher(value);
//            if (reservedCommandMatcher.find() && reservedCommandMatcher.start()==1) {
//                String strReservedCommand = reservedCommandMatcher.group();
//                ReservedCommand reservedCommand = ReservedCommand.valueOf(strReservedCommand);
//                value = removePrefix(value.replaceFirst(strReservedCommand, "")).trim();
//                returnCommand.setReservedCommand(reservedCommand);
//            }
//            else {
//                returnCommand.setReservedCommand(ReservedCommand.user);
//            }
//        }
        ReservedCommand reservedCommand = parseReservedCommand(value);
        if(reservedCommand.isNonUserCommand()) {
            value = removePrefix(value.replaceFirst(reservedCommand.name(), "")).trim();
            returnCommand.setReservedCommand(reservedCommand);
        }
        else {
            returnCommand.setReservedCommand(ReservedCommand.user);
        }
        Matcher userAddMatcher = userAddCommandPattern.matcher(value);
        Matcher userCommandMatcher = userCommandPattern.matcher(value);

        if(userAddMatcher.matches()) {
            command = userAddMatcher.group(capture_command);
            entry = userAddMatcher.group(capture_value);
            //check the entry field for optional arguments
            if(grabWholeTextAsValue==false) {
                Matcher optionalArgsMatcher = optionalArgumentsPattern.matcher(entry);
                while(optionalArgsMatcher.find()) {
                    String optionalArgs = optionalArgsMatcher.group();
                    String[] argToValue = optionalArgs.split("=");
                    entry = entry.replace(optionalArgs,"");
                    optionalArgsMap.put(argToValue[0],argToValue[1]);
                }
            }

            returnCommand.setKey(command);
            returnCommand.setValue(entry.trim());
            returnCommand.setOptionalArgsToValue(optionalArgsMap);
        }
        else if(userCommandMatcher.matches()) {
            command = userCommandMatcher.group(capture_command);
            returnCommand.setKey(command);
        }

        return returnCommand;
    }
    /**
     * Parsers a string of text to retrieve one or more commands determined by
     * a contained multi value delimiter. This approach will fill the command object
     * with its corresponding ReservedCommand and perform any sanitization required
     * @param value
     * @return
     */
    public static List<Command> parseCommands(String value) {
        List<Command> commands = new ArrayList<>();
        ReservedCommand reservedCommand = parseReservedCommand(value);
        if(reservedCommand.isSingleLineCommand()) {
            Command command=null;
            ReservedCommand lastReservedCommand =reservedCommand;
            command = parseCommand(value.substring(reservedCommand.name().length()+1).trim(),true);
            command.setReservedCommand(lastReservedCommand);
            commands.add(command);
        }
        else {
            String[] matches = value.split(multiCommandDelimiterRegex);
            for (String strMatch : matches) {
                String strLineCommand = strMatch;
                Matcher allEncompasing = allEncompasingPattern.matcher(strLineCommand);
                while (allEncompasing.find()) {
                    String strCommand = allEncompasing.group(capture_command);
                    Command command = parseCommand(strCommand);
                    commands.add(command);
                }
            }
        }
        return commands;
    }

    /**
     * Search for an instance of the given pattern and pass it to a function that can use the found string and replace it
     * with something else.
     * @param text The text
     * @param regex Matching Pattern
     * @return The original String where all matching substrings defined by regex replaced with the result of
     * StringModifier.
     */
    public static String searchAndReplace(String text, Pattern regex, StringModifier stringModifier) {
        Matcher matcher = regex.matcher(text);
        StringBuffer stringBuffer = new StringBuffer(text.length());
        while(matcher.find()) {
            String match = matcher.group();
            String modifiedString = stringModifier.apply(match);
            matcher.appendReplacement(stringBuffer,modifiedString);
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }
    private static String removePrefix(String s) {
        String prefix = "!";
        if(s.startsWith(prefix))
            return s.replaceFirst(Pattern.quote("!"),"");
        else
            return s;
    }
}
