package com.procyk.industries.command;

import com.google.inject.Guice;
import com.procyk.industries.module.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommandExecutorTest {
    Map<String,String> commands;
    @Inject
    CommandExecutor commandExecutor;

    MessageChannel messageChannel;
    MessageAction messageAction;
    Member member;
    User user ;
    @BeforeAll
    public void setUp() {
        Guice.createInjector(new BotTestModule(), new CommandServiceTestModule(),new AudioServiceModule()).injectMembers(this);
        commands = new HashMap<String, String>()
        {{
          put("!test","test action");
        }};

        messageChannel = mock(MessageChannel.class);
        messageAction = mock(MessageAction.class);
        doNothing().when(messageAction).queue();
        when(messageChannel.sendMessage((String)notNull())).thenReturn(messageAction);
        member = mock(Member.class);
        user = mock(User.class);
        when(member.getUser()).thenReturn(user);
        when(member.hasPermission(Permission.ADMINISTRATOR)).thenReturn(true);
        when(user.getName()).thenReturn("bob");
    }
    @AfterAll
    public void tearDown() {
        commandExecutor=null;
        commands=null;
    }
    @Test
    public void testAddDeleteEdit() {
        Command command = new Command("!peanut","butter");
        commandExecutor.addCommand(messageChannel,command);
        commandExecutor.deleteCommand(messageChannel,member,command);
        commandExecutor.editCommand(messageChannel,member,command);
    }

    @Test
    public void testSuggestCommands() {
        String testCommand= "!test";

        assertTrue(commandExecutor.suggestCommands(testCommand).size()>0);
    }
    @Test
    public void testGroupCommands() {
        Map<String,String> optionalArgs = new HashMap<>();
        Command command = new Command(ReservedCommand.group,optionalArgs,"!spongebob","!icecream");
        commandExecutor.groupCommands(messageChannel,member,command);
    }
    @Test
    void testRenameCommand() {
        Command command = new Command("!zzOriginal", "this is some command");
        Command rename = new Command(ReservedCommand.rename, "!zzOriginal", "!zzNew");
        Command delete = new Command("!zzNew", "this is some command");
        commandExecutor.addCommand(messageChannel, command);
        commandExecutor.renameCommand(messageChannel,member, rename);
        commandExecutor.deleteCommand(messageChannel, member, delete);
    }

}