package com.procyk.industries.bot.util;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import org.apache.commons.lang3.StringUtils;

public class MessageHandler {
    static int MESSAGE_SIZE=2000;
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
                    messageChannel.sendMessage(message.substring(start, handled));
                    start=handled;
                    handled = (handled+MESSAGE_SIZE) > message.length() ? message.length() : handled+MESSAGE_SIZE;
                }
            }
        }
    }
}
