package com.procyk.industries.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.procyk.industries.bot.event.OnBotShutdownImpl;
import com.procyk.industries.bot.event.OnMessageReceivedImpl;
import com.procyk.industries.data.CRUDable;
import com.procyk.industries.data.FirestoreCRUD;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotTestModule extends AbstractModule {
    private final Logger logger = LoggerFactory.getLogger(BotModule.class);

    @Provides
    @Named("token") String providesToken() {
        InputStream in = getClass().getResourceAsStream("/token");
        Properties properties = new Properties();
        String result="";
        try {
            properties.load(in);
            result = properties.getProperty("token");
        } catch (IOException e) {
            logger.error("Could not find token file {}", e);
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

    @Override
    protected void configure() {
        bind(CRUDable.class).to(FirestoreCRUD.class).in(Scopes.SINGLETON);
    }
}
