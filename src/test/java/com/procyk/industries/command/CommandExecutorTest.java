package com.procyk.industries.command;

import com.google.inject.Guice;
import com.procyk.industries.module.AudioServiceModule;
import com.procyk.industries.module.BotModule;
import com.procyk.industries.module.CommandServiceModule;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CommandExecutorTest {
    @Inject
    CommandExecutor commandExecutor;

    MessageChannel messageChannel;
    MessageAction messageAction;
    Member member;
    User user ;
    @BeforeEach
    public void setUp() {
        Guice.createInjector(new BotModule(), new CommandServiceModule(),new AudioServiceModule()).injectMembers(this);
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
    @AfterEach
    public void tearDown() {
        commandExecutor=null;
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
//todo use powermock to execute this test
//        assertTrue(commandExecutor.suggestCommands(testCommand).size()>0);
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