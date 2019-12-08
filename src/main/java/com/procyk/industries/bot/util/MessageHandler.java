package com.procyk.industries.bot.util;

import net.dv8tion.jda.core.entities.MessageChannel;
import org.apache.commons.lang3.StringUtils;

public final class MessageHandler {
    private static final int MESSAGE_SIZE=1990;

    private MessageHandler() {
        throw new IllegalStateException("Static Utility Class");
    }
    /**
     * Generic handler for sending messages through the bot. Accounts for maximum string lengths and queues up chunks
     * of messages when the message is too big.
     */
    public static void sendMessage(MessageChannel messageChannel, String message) {
        if(StringUtils.isNotBlank(message)) {
            if(message.length()>MESSAGE_SIZE) {
                int handled = MESSAGE_SIZE;
                int start=0;
                while(handled < message.length()) {
                    sendMessageHandler(messageChannel, message.substring(start, handled));
                    start=handled;
                    handled = Math.min((handled + MESSAGE_SIZE), message.length());
                }
            } else {
                sendMessageHandler(messageChannel, message);
            }
        }
    }
    private static void sendMessageHandler(MessageChannel messageChannel, String message) {
        if(StringUtils.isBlank(message))
            return;
        String sb = "```" +
                message +
                "```";
        messageChannel.sendMessage(sb).queue();
    }
}
