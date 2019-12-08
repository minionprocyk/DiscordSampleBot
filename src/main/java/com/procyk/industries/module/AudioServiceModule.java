package com.procyk.industries.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.procyk.industries.audio.playback.AudioLoadResultHandlerImpl;
import com.procyk.industries.audio.playback.AudioSendHandlerImpl;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioServiceModule extends AbstractModule{
    private final Logger logger = LoggerFactory.getLogger(AudioServiceModule.class);
    @Provides @Singleton
    AudioPlayerManager providesAudioPlayerManager() {
        return new DefaultAudioPlayerManager();
    }
    @Provides @Singleton AudioPlayer providesAudioPlayer(AudioPlayerManager audioPlayerManager) {
        return audioPlayerManager.createPlayer();
    }
    @Provides @Singleton @Named("LOCAL_MUSIC")
    Path providesAudioRootLocalMusicFolder (@Named("APP_PATH") Path path) {
        Path musicPath = path.resolve("LocalMusic");
        if(!musicPath.toFile().exists())
            try {
                Files.createDirectory(musicPath);
            }catch(IOException e) {
                logger.info("Local Music directory already exists");
            }

        return musicPath;
    }

    @Provides @Singleton
    ExecutorService providesExecutorService() {
        return Executors.newSingleThreadExecutor();
    }
    @Override
    protected void configure() {
        super.configure();
        bind(AudioLoadResultHandler.class).to(AudioLoadResultHandlerImpl.class).in(Scopes.NO_SCOPE);
        bind(AudioSendHandler.class).to(AudioSendHandlerImpl.class);
    }
}
