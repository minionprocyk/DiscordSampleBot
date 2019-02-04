package com.procyk.industries.bot.event;

import com.procyk.industries.command.CommandService;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JDATaskDelegator extends ListenerAdapter {
    private final CommandService commandService;

    @Inject
    public JDATaskDelegator(CommandService commandService) {
        this.commandService=commandService;
    }
    public void performAsyncTask(Runnable task) {
        task.run();
    }



}
