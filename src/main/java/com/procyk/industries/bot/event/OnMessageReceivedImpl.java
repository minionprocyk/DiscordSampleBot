package com.procyk.industries.bot.event;

import com.google.inject.Inject;
import com.procyk.industries.command.CommandService;
import com.procyk.industries.command.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnMessageReceivedImpl extends ListenerAdapter{
    private static final Logger logger = LoggerFactory.getLogger(OnMessageReceivedImpl.class);
    private final CommandService commandService;

    @Inject
    public OnMessageReceivedImpl(CommandService commandService) {
        this.commandService=commandService;
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);
        Message message = new Message(event.getMessage().getContentRaw());
        commandService.performUserRequest(event,message);
    }
}
