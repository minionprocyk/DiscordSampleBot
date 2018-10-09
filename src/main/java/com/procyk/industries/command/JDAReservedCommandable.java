package com.procyk.industries.command;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface JDAReservedCommandable {
    void apply(MessageReceivedEvent event, Command command, CommandService instance);
}
