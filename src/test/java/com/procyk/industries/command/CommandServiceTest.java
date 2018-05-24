package com.procyk.industries.command;

import com.google.inject.Guice;
import com.procyk.industries.module.AudioServiceModule;
import com.procyk.industries.module.BotModule;
import com.procyk.industries.module.CommandServiceModule;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommandServiceTest {
    @Inject
    CommandService commandService;
    MessageReceivedEvent messageReceivedEvent;
    MessageChannel messageChannel;
    MessageAction messageAction;
    TextChannel textChannel;
    Member member;
    Guild guild;
    User user;
    Message message;

    @BeforeEach
    void setUp() {
        Guice.createInjector(new CommandServiceModule(),new BotModule(),new AudioServiceModule()).injectMembers(this);
        messageReceivedEvent = mock(MessageReceivedEvent.class);
        messageChannel = mock(MessageChannel.class);
        messageAction = mock(MessageAction.class);
        textChannel = mock(TextChannel.class);
        member = mock(Member.class);
        guild = mock(Guild.class);
        user = mock(User.class);

        //mock event objects
        when(user.isBot()).thenReturn(false);
        doNothing().when(messageAction).queue();
        when(messageChannel.sendMessage((String)notNull())).thenReturn(messageAction);

        //mock message receive events
        when(messageReceivedEvent.getAuthor()).thenReturn(user);
        when(messageReceivedEvent.getGuild()).thenReturn(guild);
        when(messageReceivedEvent.getChannel()).thenReturn(messageChannel);
        when(messageReceivedEvent.getTextChannel()).thenReturn(textChannel);
        when(messageReceivedEvent.getMember()).thenReturn(member);
        when(messageReceivedEvent.getMember().getUser()).thenReturn(user);
    }

    @AfterEach
    void tearDown() {
        commandService=null;
        message=null;
    }

    @Test
    void testPerformEmptyUserRequest() {
        message = new Message("test");
        commandService.performUserRequest(messageReceivedEvent,message);
    }

    @Test
    void testReflexiveCommands() {
        //add a reflexive command
        message = new Message("!add !zzTestTony !player !volume 50 !player !play <link>");
        commandService.performUserRequest(messageReceivedEvent,message);

        //call the command expecting player volume to be 50 and for the player to play the link resource
        message = new Message("!zzTestTony");
        commandService.performUserRequest(messageReceivedEvent,message);


    }

}