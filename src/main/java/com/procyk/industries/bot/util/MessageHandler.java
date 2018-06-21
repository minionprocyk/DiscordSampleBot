package com.procyk.industries.bot.util;

import net.dv8tion.jda.core.entities.MessageChannel;
import org.apache.commons.lang3.StringUtils;

public class MessageHandler {
    static int MESSAGE_SIZE=1990;
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
                    handled = (handled+MESSAGE_SIZE) > message.length() ? message.length() : handled+MESSAGE_SIZE;
                }
            } else {
                sendMessageHandler(messageChannel, message);
            }
        }
    }
    private static void sendMessageHandler(MessageChannel messageChannel, String message) {
        StringBuilder sb = new StringBuilder(message.length()+6);
        sb.append("```")
                .append(message)
                .append("```");
        messageChannel.sendMessage(sb.toString()).queue();
    }
}
