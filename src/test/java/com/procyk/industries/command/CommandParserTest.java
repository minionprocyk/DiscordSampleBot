package com.procyk.industries.command;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandParserTest {

    @Test
    public void testCommandParserWithAddCommand() {
        String strCommand = "!test !dome test me";
        Command command = CommandParser.parseCommand(strCommand);
        Command expected = new Command(ReservedCommand.test,"!dome","test me");
        assertEquals(expected,command,"Expected Command object with ReservedCommand of test and key=!dome, value=test me");

    }
    @Test
    public void testCommandParserWithMultiCommands() {
        String strCommand = "!add !money !player !play <link>";
        List<Command> commands = CommandParser.parseCommands(strCommand);
        List<Command> expectedCommands = Arrays.asList(
                new Command(ReservedCommand.add,"!money","!player !play <link>")
        );
        assertEquals(expectedCommands,commands,"Expected one command for add");
    }

    @Test
    public void testCommandParserWithMultiCommandsForUser() {
        String strCommand = "!player !play <link>";
        Map<String,String> optionalArgs = new HashMap<>();
        List<Command> commands = CommandParser.parseCommands(strCommand);
        List<Command> expectedCommands = Arrays.asList(
            new Command(ReservedCommand.player,"!play","<link>")
        );
        assertEquals(expectedCommands,commands,"Expected one command for player");
        assertEquals(Arrays.asList(new Command(ReservedCommand.join,"","")),CommandParser.parseCommands("!join"));
        String testString="!gatorade its what they use to make brawndo";
        assertEquals(Arrays.asList(new Command(ReservedCommand.user,"!gatorade","its what they use to make brawndo")),
                CommandParser.parseCommands(testString));
        assertEquals(Arrays.asList(new Command(ReservedCommand.player,"!play","https://www.youtube.com/watch?v=hT47Ysl-2Ew")),
                CommandParser.parseCommands("!player !play https://www.youtube.com/watch?v=hT47Ysl-2Ew"));

        //test multi command with multi command delimiter
        optionalArgs.clear();
        optionalArgs.put("start","10");
        expectedCommands=Arrays.asList(new Command(ReservedCommand.player,optionalArgs,"!play","https://www.youtube.com/watchmedoggy"),
                new Command(ReservedCommand.user,"!turkey","")
                );
        assertEquals(expectedCommands,CommandParser.parseCommands("!player !play https://www.youtube.com/watchmedoggy start=10-> !turkey"));
        assertEquals(Arrays.asList(new Command(ReservedCommand.add,"!banjo","!player !play https://youtube.com/swag -> !turkey -> !papa")),
                CommandParser.parseCommands("!add !banjo !player !play https://youtube.com/swag -> !turkey -> !papa"));
    }
    @Test
    public void testRegexParse() {
        String test ="!player !play <link> !player !play <other link> ";
        List<String> resultList = CommandParser.testRegexParse(test,CommandParser.allEncompasingPattern);
        List<String> expectedList = Arrays.asList("!player !play <link>","!player !play <other link>");
        assertEquals(expectedList,resultList,"Expected trailing spaces removed from captured groups");

        test = "!add !player !play <linK>";
        resultList = CommandParser.testRegexParse(test,CommandParser.resrvedCommandPattern);
        expectedList = Arrays.asList("add","player");
        assertEquals(expectedList,resultList);

        test = "!daisy -> !chain -> !this -> !command";
        expectedList = Arrays.asList("!daisy","!chain","!this","!command");
        assertEquals(expectedList,CommandParser.testRegexSplit(test,CommandParser.multiCommandDelimiterRegex));
    }
    @Test
    public void testOptionalArgs() {
        String strCommand = "!player !play youtube.skynet/watch?v=abc123 start=10 end=30 volume=10";
        List<Command> parsedCommands = CommandParser.parseCommands(strCommand);
        Map<String,String> expectedOptionalArgs = new HashMap<>();
        expectedOptionalArgs.put("start","10");
        expectedOptionalArgs.put("end","30");
        expectedOptionalArgs.put("volume","10");
        Command expectedCommand = new Command(ReservedCommand.player,expectedOptionalArgs,"!play","youtube.skynet/watch?v=abc123");
        assertEquals(Arrays.asList(expectedCommand),parsedCommands,"Expected parsed command to contain optional args");
    }
    @Test
    public void benchmarkCommandParser() {
        String strCommand = "!add !player !play <linK>";
        String strCommand2 = "!player !play <link> !player !play <other link> start=20 volume=30 end=10";

        for(int i=0;i<100000;i++) {
            String cmd = i%2==0? strCommand: strCommand2;
            CommandParser.parseCommands(cmd);
        }
    }
}