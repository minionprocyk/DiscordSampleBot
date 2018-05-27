package com.procyk.industries.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.procyk.industries.bot.event.OnBotShutdownImpl;
import com.procyk.industries.bot.event.OnMessageReceivedImpl;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Pattern;

public class BotModule extends AbstractModule{

    @Provides @Named("token") String providesToken() {
        Path path = Paths.get(getClass().getClassLoader().getResource("token").getPath());
        Properties properties = new Properties();
        String result="";
        try {
            properties.load(Files.newInputStream(path));
            result = properties.getProperty("token");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    @Provides @Named("prefix") String providesPrefix() {
        return "!";
    }
    @Provides @Named("multi_delimiter") String[] providesMultiCommandDelimiter() {
        return new String[]{"$=>$","->"};
    }
    @Provides ListenerAdapter[] providesListenerAdapters(OnMessageReceivedImpl onMessageReceived, OnBotShutdownImpl onBotShutdown) {
        return new ListenerAdapter[]{
                onMessageReceived,
                onBotShutdown
        };
    }

    @Provides
    JDABuilder providesJDABuilder(@Named("token") String token, ListenerAdapter... eventListener) {
        return new JDABuilder(AccountType.BOT)
                .setToken(token)
                .addEventListener(eventListener)
                .setStatus(OnlineStatus.ONLINE);
    }
    @Provides @Singleton
    JDA providesJDA(JDABuilder jdaBuilder) {
        try {
            return jdaBuilder.buildAsync();
        } catch (LoginException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected void configure() {
        super.configure();
//        bind(ListenerAdapter.class).to(OnMessageReceivedImpl.class);
//        bind(ListenerAdapter.class).to(OnBotShutdownImpl.class);
    }
}
