package com.procyk.industries.bot.event;

import com.procyk.industries.command.CommandStore;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.inject.Inject;
import java.util.EventListener;
import java.util.concurrent.ExecutorService;

public class OnBotShutdownImpl extends ListenerAdapter implements EventListener {
    private final CommandStore commandStore;
    private final ExecutorService executorService;
    @Inject
    public OnBotShutdownImpl(CommandStore commandStore, ExecutorService executorService) {
        this.commandStore=commandStore;
        this.executorService=executorService;
    }
    @Override
    public void onShutdown(ShutdownEvent event) {
        super.onShutdown(event);
        commandStore.persist();
        executorService.shutdown();
    }
}
