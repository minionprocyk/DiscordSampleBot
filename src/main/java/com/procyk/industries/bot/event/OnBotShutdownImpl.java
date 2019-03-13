package com.procyk.industries.bot.event;

import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.inject.Inject;
import java.util.EventListener;
import java.util.concurrent.ExecutorService;

public class OnBotShutdownImpl extends ListenerAdapter implements EventListener {
    private final ExecutorService executorService;
    @Inject
    public OnBotShutdownImpl(ExecutorService executorService) {
        this.executorService=executorService;
    }
    @Override
    public void onShutdown(ShutdownEvent event) {
        super.onShutdown(event);
        executorService.shutdown();
    }
}
