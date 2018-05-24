package com.procyk.industries.bot.event;

import com.procyk.industries.command.CommandStore;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.inject.Inject;
import java.util.EventListener;

public class OnBotShutdownImpl extends ListenerAdapter implements EventListener {
    private final CommandStore commandStore;
    @Inject
    public OnBotShutdownImpl(CommandStore commandStore) {
        this.commandStore=commandStore;
    }
    @Override
    public void onShutdown(ShutdownEvent event) {
        super.onShutdown(event);
        commandStore.persist();
    }
}
