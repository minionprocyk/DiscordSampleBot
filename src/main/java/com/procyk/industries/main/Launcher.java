package com.procyk.industries.main;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.procyk.industries.module.AudioServiceModule;
import com.procyk.industries.module.BotModule;
import com.procyk.industries.module.CommandServiceModule;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class Launcher extends ListenerAdapter{
    public static void main(String[] args){
        Injector inject = Guice.createInjector(new CommandServiceModule(),new BotModule(), new AudioServiceModule());
        inject.getInstance(JDA.class);
        LoggerFactory.getLogger(Launcher.class).info("All Modules injected. JDA is starting...");
    }
}
