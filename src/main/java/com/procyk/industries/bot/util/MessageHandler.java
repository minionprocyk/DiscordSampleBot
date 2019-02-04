package com.procyk.industries.bot.util;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.Optional;

public class MessageHandler {
    static final int MESSAGE_SIZE=1990;
    private final JDA jda;

    @Inject
    public MessageHandler(JDA jda) {
        this.jda=jda;
    }

    /**
     * Generic handler for sending messages through the bot. Accounts for maximum string lengths and queues up chunks
     * of messages when the message is too big.
     */
    public void sendMessage(String message) {
        if(StringUtils.isNotBlank(message)) {
            if(message.length()>MESSAGE_SIZE) {
                int handled = MESSAGE_SIZE;
                int start=0;
                while(handled < message.length()) {
                    sendMessageHandler(message.substring(start, handled));
                    start=handled;
                    handled = (handled+MESSAGE_SIZE) > message.length() ? message.length() : handled+MESSAGE_SIZE;
                }
            } else {
                sendMessageHandler(message);
            }
        }
    }
    private void sendMessageHandler(String message) {
        Optional<TextChannel> optionalTextChannel= jda.getTextChannels().stream()
                .filter(TextChannel::canTalk)
                .filter(channel-> channel.getName().equalsIgnoreCase("General"))
                .findFirst();

        if(optionalTextChannel.isPresent()) {
            TextChannel textChannel = optionalTextChannel.get();
            if(StringUtils.isBlank(message))
                return;
            String sb = "```" +
                    message +
                    "```";
            textChannel.sendMessage(sb).queue();
        }

    }
}
