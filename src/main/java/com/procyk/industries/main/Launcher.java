package com.procyk.industries.main;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.procyk.industries.module.AudioServiceModule;
import com.procyk.industries.module.BotModule;
import com.procyk.industries.module.CommandServiceModule;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.logging.Logger;

public class Launcher extends ListenerAdapter{
    private static final Logger logger = Logger.getLogger(Launcher.class.getName());

    public static void main(String[] args) throws LoginException{
        Injector inject = Guice.createInjector(new CommandServiceModule(),new BotModule(), new AudioServiceModule());
        inject.getInstance(JDA.class);
    }
}
