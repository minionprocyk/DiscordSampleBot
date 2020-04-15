package com.procyk.industries.command;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.procyk.industries.module.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.junit.jupiter.api.*;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommandServiceTest {
    @Inject
    private CommandService commandService;

    private MessageReceivedEvent messageReceivedEvent;
    private MessageChannel messageChannel;
    private MessageAction messageAction;
    private TextChannel textChannel;
    private Member member;
    private Guild guild;
    private User user;
    private Message message;

    @BeforeAll
    void setUp() {
        Guice.createInjector(new CommandServiceTestModule(),new BotTestModule(),new AudioServiceModule()).injectMembers(this);
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
        when(member.getUser()).thenReturn(user);
        when(member.hasPermission(Permission.ADMINISTRATOR)).thenReturn(true);

        //mock message receive events
        when(messageReceivedEvent.getAuthor()).thenReturn(user);
        when(messageReceivedEvent.getGuild()).thenReturn(guild);
        when(messageReceivedEvent.getChannel()).thenReturn(messageChannel);
        when(messageReceivedEvent.getTextChannel()).thenReturn(textChannel);
        when(messageReceivedEvent.getMember()).thenReturn(member);
        when(messageReceivedEvent.getMember().getUser()).thenReturn(user);

    }

    @AfterAll
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

        //delete the command
        message = new Message("!delete !zzTestTony");
        commandService.performUserRequest(messageReceivedEvent,message);

    }

}