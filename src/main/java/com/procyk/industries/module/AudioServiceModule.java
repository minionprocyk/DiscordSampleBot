package com.procyk.industries.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.procyk.industries.audio.playback.AudioLoadResultHandlerImpl;
import com.procyk.industries.audio.playback.AudioSendHandlerImpl;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import net.dv8tion.jda.core.audio.AudioSendHandler;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AudioServiceModule extends AbstractModule{
    @Provides @Singleton AudioPlayerManager providesAudioPlayerManager() {
        return new DefaultAudioPlayerManager();
    }
    @Provides @Singleton AudioPlayer providesAudioPlayer(AudioPlayerManager audioPlayerManager) {
        return audioPlayerManager.createPlayer();
    }
    @Provides @Singleton
    @Named("LOCAL_MUSIC")
    Path providesAudioRootLocalMusicFolder (@Named("APP_PATH") Path path) {
        //noinspection CatchMayIgnoreException
        try {
            Files.createDirectory(path.resolve("LocalMusic"));
        } catch (IOException e) {        }
        return path.resolve("LocalMusic");
    }
    @Override
    protected void configure() {
        super.configure();
        bind(AudioLoadResultHandler.class).to(AudioLoadResultHandlerImpl.class).in(Scopes.NO_SCOPE);
        bind(AudioSendHandler.class).to(AudioSendHandlerImpl.class);
    }
}
