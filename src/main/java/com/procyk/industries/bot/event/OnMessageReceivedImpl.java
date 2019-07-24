package com.procyk.industries.bot.event;

import com.google.inject.Inject;
import com.procyk.industries.command.CommandService;
import com.procyk.industries.command.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;

public class OnMessageReceivedImpl extends ListenerAdapter{
    private final CommandService commandService;
    private static Map<String,MemberEvent> nameToMemberEvent = new HashMap<>();

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
        super.onMessageReceived(event);
        Message message = new Message(event.getMessage().getContentRaw());

        MemberEvent memberEvent;
        String memberNick = event.getMember().getEffectiveName();
        if(nameToMemberEvent.size()>0
                && (memberEvent = (nameToMemberEvent.get(memberNick)))!=null
                || (memberEvent = (nameToMemberEvent.get("anyone")))!=null) {
            nameToMemberEvent.remove(memberNick);
            nameToMemberEvent.remove("anyone");
            String payload = event.getMessage().getContentRaw();
            memberEvent.fire(payload);
        }
        else {
            commandService.performUserRequest(event,message);
        }
    }
}
