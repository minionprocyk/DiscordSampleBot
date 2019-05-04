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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.security.auth.login.LoginException;

public class BotModule extends AbstractModule{
    private final Logger logger = LoggerFactory.getLogger(BotModule.class);

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
    JDABuilder providesJDABuilder(@Named("token") String token, ListenerAdapter[] eventListener) {
        return new JDABuilder(AccountType.BOT)
                .setToken(token)
                .addEventListener((Object[])eventListener)
                .setStatus(OnlineStatus.ONLINE);
    }
    @Provides @Named("jdbc_url") String providesJDBCUrl() {
        return "jdbc:sqlite:commands.db";
    }
    @Provides @Singleton
    JDA providesJDA(JDABuilder jdaBuilder) {
        try {
            return jdaBuilder.build();
        } catch (LoginException e) {
            logger.error("JDA Failed to launch {}",e);
        }
        return null;
    }
}
