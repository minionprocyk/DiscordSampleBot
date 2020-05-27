package com.procyk.industries.command;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandParserTest {

    @Test
    void testCommandParserWithAddCommand() {
        String strCommand = "!test !dome test me";
        Command command = CommandParser.parseCommand(strCommand);
        Command expected = new Command(ReservedCommand.test,"!dome","test me");
        assertEquals(expected,command,"Expected Command object with ReservedCommand of test and key=!dome, value=test me");

    }
    @Test
    void testCommandParserWithMultiCommands() {
        String strCommand = "!add !money !player !play <link>";
        List<Command> commands = CommandParser.parseCommands(strCommand);
        List<Command> expectedCommands = Collections.singletonList(
                new Command(ReservedCommand.add, "!money", "!player !play <link>")
        );
        assertEquals(expectedCommands,commands,"Expected one command for add");
    }

    @Test
    void testCommandParserWithMultiCommandsForUser() {
        String strCommand = "!player !play <link>";
        Map<String,String> optionalArgs = new HashMap<>();
        List<Command> commands = CommandParser.parseCommands(strCommand);
        List<Command> expectedCommands = Collections.singletonList(
                new Command(ReservedCommand.player, "!play", "<link>")
        );
        assertEquals(expectedCommands,commands,"Expected one command for player");
        assertEquals(Collections.singletonList(new Command(ReservedCommand.join, "", "")),CommandParser.parseCommands("!join"));
        String testString="!gatorade its what they use to make brawndo";
        assertEquals(Collections.singletonList(new Command(ReservedCommand.user, "!gatorade", "its what they use to make brawndo")),
                CommandParser.parseCommands(testString));
        assertEquals(Collections.singletonList(new Command(ReservedCommand.player, "!play", "https://www.youtube.com/watch?v=hT47Ysl-2Ew")),
                CommandParser.parseCommands("!player !play https://www.youtube.com/watch?v=hT47Ysl-2Ew"));

        //test multi command with multi command delimiter
        optionalArgs.clear();
        optionalArgs.put("start","10");
        expectedCommands=Arrays.asList(new Command(ReservedCommand.player,optionalArgs,"!play","https://www.youtube.com/watchmedoggy"),
                new Command(ReservedCommand.user,"!turkey","")
                );
        assertEquals(expectedCommands,CommandParser.parseCommands("!player !play https://www.youtube.com/watchmedoggy start=10-> !turkey"));
        assertEquals(Collections.singletonList(new Command(ReservedCommand.add, "!banjo", "!player !play https://youtube.com/swag -> !turkey -> !papa")),
                CommandParser.parseCommands("!add !banjo !player !play https://youtube.com/swag -> !turkey -> !papa"));
    }
    @Test
    void testRegexParse() {
        String test ="!player !play <link> !player !play <other link> ";
        List<String> resultList = CommandParser.testRegexParse(test,CommandParser.allEncompasingPattern);
        List<String> expectedList = Arrays.asList("!player !play <link>","!player !play <other link>");
        assertEquals(expectedList,resultList,"Expected trailing spaces removed from captured groups");

        test = "!add !player !play <linK>";
        resultList = CommandParser.testRegexParse(test,CommandParser.resrvedCommandPattern);
        expectedList = Arrays.asList("add","player", "play");
        assertEquals(expectedList,resultList);

        test = "!daisy -> !chain -> !this -> !command";
        expectedList = Arrays.asList("!daisy","!chain","!this","!command");
        assertEquals(expectedList,CommandParser.testRegexSplit(test,CommandParser.multiCommandDelimiterRegex));
    }
    @Test
    void testOptionalArgs() {
        String strCommand = "!player !play youtube.skynet/watch?v=abc123 start=10 end=30 volume=10";
        List<Command> parsedCommands = CommandParser.parseCommands(strCommand);
        Map<String,String> expectedOptionalArgs = new HashMap<>();
        expectedOptionalArgs.put("start","10");
        expectedOptionalArgs.put("end","30");
        expectedOptionalArgs.put("volume","10");
        Command expectedCommand = new Command(ReservedCommand.player,expectedOptionalArgs,"!play","youtube.skynet/watch?v=abc123");
        assertEquals(Collections.singletonList(expectedCommand),parsedCommands,"Expected parsed command to contain optional args");
    }
    @Test
    void benchmarkCommandParser() {
        String strCommand = "!add !player !play <linK>";
        String strCommand2 = "!player !play <link> !player !play <other link> start=20 volume=30 end=10";

        for(int i=0;i<100000;i++) {
            String cmd = i%2==0? strCommand: strCommand2;
            CommandParser.parseCommands(cmd);
        }
    }
    @Test
    void testPlayLocalCommands() {
        String strCommand = "!player !playlocal perfectdark/soundwave.mp3";
        List<Command> parsedCommands = CommandParser.parseCommands(strCommand);
        Command expectedCommand = new Command(ReservedCommand.player,"!playlocal","perfectdark/soundwave.mp3");
        assertEquals(Collections.singletonList(expectedCommand),parsedCommands,"Expected parsed command to contain wave file");
    }
    @Test
    void testPlayerLocalMusicCommand() {
        String strCommand = "!player !localmusic pnbajamclips/turtleme";
        List<Command> parsedCommands = CommandParser.parseCommands(strCommand);
        Command expectedCommand = new Command(ReservedCommand.player,"!localmusic","pnbajamclips/turtleme");
        assertEquals(Collections.singletonList(expectedCommand),parsedCommands,"Expected parsed command to contain directory");
    }
    @Test
    void testSearchAndReplaceForNumbersInCommands() {
        String text = "!player !playlocal 401";
        String result = CommandParser.searchAndReplace(text,CommandParser.replaceDigitsAfterPlayLocalCommandPattern,(str)-> {
            int i = Integer.parseInt(str);
            i++;
            return Integer.toString(i);
        });
        assertEquals("!player !playlocal 402",result,"Expected 401 to be incremented to 402");

        String text2 = "!player !playlocal 401 start=0.1 end=0.8";
        result = CommandParser.searchAndReplace(text2,CommandParser.replaceDigitsAfterPlayLocalCommandPattern,(str)-> {
            int i = Integer.parseInt(str);
            i++;
            return Integer.toString(i);
        });
        assertEquals("!player !playlocal 402 start=0.1 end=0.8",result,"Expected 401 to be incremented to 402");

    }
    @Test
    void testParseRenameCommand() {
        String text = "!rename !zzOriginal !zzNew";
        Command cmd = CommandParser.parseCommand(text);
        assertEquals(ReservedCommand.rename, cmd.getReservedCommand());
        assertEquals("!zzOriginal", cmd.getKey());
        assertEquals("!zzNew", cmd.getValue());

        List<Command> cmds = CommandParser.parseCommands(text);
        assertEquals(cmds.get(0),cmd);
    }
    @Test
    void testParsePlay() {
        String text = "!play youtube.com/watch?buyme_things start=10 end=20";
        Command cmd = CommandParser.parseCommand(text);
        Map<String,String> optionalCommands = new HashMap<>();
        optionalCommands.put("start","10");
        optionalCommands.put("end","20");

        assertEquals(ReservedCommand.player, cmd.getReservedCommand());
        assertEquals("youtube.com/watch?buyme_things", cmd.getValue());
        assertEquals(optionalCommands,cmd.getOptionalArgsToValue());
        List<Command> cmds = CommandParser.parseCommands(text);
        assertEquals(cmds.get(0),cmd);
    }
    @Test
    void testSearch() {
        String text = "!search something on youtube";
        Command cmd = CommandParser.parseCommand(text);
        System.out.println(cmd.toString());
    }

}