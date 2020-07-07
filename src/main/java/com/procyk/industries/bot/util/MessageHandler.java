package com.procyk.industries.bot.util;

import com.procyk.industries.strings.Strings;
import net.dv8tion.jda.api.entities.MessageChannel;

public final class MessageHandler {
    private static MessageChannel lastMessageChannel;
    private static final int MESSAGE_SIZE=1990;

    private MessageHandler() {
        throw new IllegalStateException("Static Utility Class");
    }
    /**
     * Generic handler for sending messages through the bot. Accounts for maximum string lengths and queues up chunks
     * of messages when the message is too big.
     */
    public static void sendMessage(MessageChannel messageChannel, String message) {

        if(Strings.isNotBlank(message)) {
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
    public static void sendMessage(String message) {
        sendMessage(lastMessageChannel, message);
    }
    private static void sendMessageHandler(MessageChannel messageChannel, String message) {
        if(messageChannel==null || Strings.isBlank(message))
            return;
        lastMessageChannel=messageChannel;
        String sb = "```" +
                message +
                "```";
        messageChannel.sendMessage(sb).queue();
    }
}
