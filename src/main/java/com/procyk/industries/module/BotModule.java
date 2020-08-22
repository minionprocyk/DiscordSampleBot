package com.procyk.industries.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.procyk.industries.bot.event.OnBotShutdownImpl;
import com.procyk.industries.bot.event.OnMessageReceivedImpl;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
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
    JDABuilder providesJDABuilder(@JDAToken String token, ListenerAdapter[] eventListener) {
        return JDABuilder.createDefault(token)
                .addEventListeners((Object[]) eventListener)
                .setStatus(OnlineStatus.ONLINE)
                .disableCache(CacheFlag.MEMBER_OVERRIDES,CacheFlag.ACTIVITY)
                .setCompression(Compression.NONE)
                .setActivity(Activity.listening("To my peeps"))
                .setMemberCachePolicy(MemberCachePolicy.VOICE.or(MemberCachePolicy.OWNER))
                .setChunkingFilter(ChunkingFilter.NONE)
                .disableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING)
                .setLargeThreshold(50)

                ;
    }
    @Provides @JDBCUrl String providesJDBCUrl() {
        return "jdbc:sqlite:commands.db";
    }

    @Provides @Singleton
    JDA providesJDA(JDABuilder jdaBuilder) {
        try {
            return jdaBuilder.build();
        } catch (LoginException e) {
            logger.error("JDA Failed to launch",e);
        }
        return null;
    }
}
