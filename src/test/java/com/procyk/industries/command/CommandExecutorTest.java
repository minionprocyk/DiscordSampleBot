package com.procyk.industries.command;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.procyk.industries.module.AudioServiceModule;
import com.procyk.industries.module.BotModule;
import com.procyk.industries.module.CommandServiceModule;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandExecutorTest {
    Map<String,String> commands;
    @Inject
    CommandExecutor commandExecutor;

    MessageChannel messageChannel;
    MessageAction messageAction;
    Member member;
    User user ;
    @BeforeEach
    public void setUp() {
        Guice.createInjector(new CommandServiceModule(),new AudioServiceModule()).injectMembers(this);
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
        when(user.getName()).thenReturn("bob");
    }
    @AfterEach
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
    public void testReflexiveCommands() {

    }
    @Test
    public void testGroupCommands() {
        Map<String,String> optionalArgs = new HashMap<>();
        Command command = new Command(ReservedCommand.group,optionalArgs,"!spongebob","!icecream");
        commandExecutor.groupCommands(messageChannel,member,command);
    }

}