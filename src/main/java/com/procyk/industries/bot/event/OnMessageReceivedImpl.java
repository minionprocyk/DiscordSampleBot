package com.procyk.industries.bot.event;

import com.google.inject.Inject;
import com.procyk.industries.bot.util.MessageHandler;
import com.procyk.industries.command.CommandService;
import com.procyk.industries.command.Message;
import com.procyk.industries.module.Application;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;

public class OnMessageReceivedImpl extends ListenerAdapter{
    private final CommandService commandService;
    private static final Map<String,MemberEvent> nameToMemberEvent = new HashMap<>();

    @Inject
    public OnMessageReceivedImpl(CommandService commandService) {
        this.commandService=commandService;
    }

    /**
     * Events registered are automatically unregistered after fired.
     */
    public static void registerMemberEvent(String nick, MemberEvent memberEvent) {
        nameToMemberEvent.put(nick,memberEvent);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot())return;
        Member member;
        if(null == (member = event.getMember())) {
            MessageHandler.sendMessage(event.getChannel(), "I failed to look you up, I can't register this command");
            return;
        }
        super.onMessageReceived(event);
        Message message = new Message(event.getMessage().getContentRaw());

        String memberNick = member.getEffectiveName();
        MemberEvent memberEvent;
        if(nameToMemberEvent.size()>0
                && (memberEvent = (nameToMemberEvent.get(memberNick)))!=null
                || (memberEvent = (nameToMemberEvent.get(Application.DEFAULT_MEMBER)))!=null) {
            nameToMemberEvent.remove(memberNick);
            nameToMemberEvent.remove(Application.DEFAULT_MEMBER);
            String payload = event.getMessage().getContentRaw();
            memberEvent.fire(payload);
        }
        else {
            commandService.performUserRequest(event,message);
        }
    }
}
