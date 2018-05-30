package com.procyk.industries.audio.playback;

import com.google.inject.name.Named;
import com.procyk.industries.command.Command;
import com.procyk.industries.command.CommandParser;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class AudioServiceManager {
    private static final Logger logger = LoggerFactory.getLogger(AudioServiceManager.class);
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final AudioSendHandlerImpl audioSendHandler;
    private final AudioLoadResultHandlerImpl audioLoadResultHandler;
    private final AudioPlayerManager audioPlayerManager;
    private final Path localMusicRootPath;
    private  List<Path> localMusicFiles;
    @Inject
    public AudioServiceManager(AudioPlayerManager audioPlayerManager, AudioPlayer audioPlayer, TrackScheduler trackScheduler, AudioSendHandlerImpl audioSendHandler,
                               AudioLoadResultHandlerImpl audioLoadResultHandler, @Named("LOCAL_MUSIC")Path localMusicRootPath) {
        this.audioPlayerManager=audioPlayerManager;
        this.audioPlayer=audioPlayer;
        this.trackScheduler=trackScheduler;
        this.audioSendHandler=audioSendHandler;
        this.audioLoadResultHandler=audioLoadResultHandler;
        this.localMusicRootPath=localMusicRootPath;
        try {

            localMusicFiles = Files.walk(localMusicRootPath, FileVisitOption.FOLLOW_LINKS)
                    .collect(Collectors.toList()) ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        audioPlayer.addListener(trackScheduler);
        audioPlayer.setVolume(30);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }
    public Path getLocalMusicRootPath() {
        return this.localMusicRootPath;
    }
    public String getSavableLocalTrackAsString(int songIndex) {
        String songPath = getKnownMusic().get(songIndex).toString();
        songPath = songPath.replaceFirst(getLocalMusicRootPath().toString()+"/","");
        return songPath;
    }
    public void next() {
        trackScheduler.startNextTrack();
    }

    public void pause() {
        audioPlayer.setPaused(true);
    }
    public void resume() {
        audioPlayer.setPaused(false);
    }
    public void load(String track) {
        audioPlayerManager.loadItem(track,audioLoadResultHandler);
    }
    public void loadWithArgs(Command command) {
        //get the stuff
        String start = command.getOptionalArg("start");
        String end = command.getOptionalArg("end");
        String volume = command.getOptionalArg("volume");
        long lstart=0,lend=0;
        int lvolume=0;

        lstart = CommandParser.parseSecondsToMillisDecimalFormat(start);
        lend = CommandParser.parseSecondsToMillisDecimalFormat(end);

        if(StringUtils.isNotBlank(volume)) {
            lvolume = Integer.parseInt(volume);
        }
        AudioTrackUserData audioTrackUserData = new AudioTrackUserData(lvolume,lstart,lend);
        AudioLoadResultHandlerImpl audioLoadResultHandlerNew= new AudioLoadResultHandlerImpl(trackScheduler);
        audioLoadResultHandler.setTrackInfo(audioTrackUserData);
        audioLoadResultHandlerNew.setTrackInfo(audioTrackUserData);
        audioPlayerManager.loadItem(command.getValue(),audioLoadResultHandlerNew);
    }

    public void joinRequestedChannel(VoiceChannel voiceChannel, AudioManager audioManager) {
        audioManager.openAudioConnection(voiceChannel);
        audioManager.setSendingHandler(audioSendHandler);
    }
    public void setVolume(int volume) {
        if(volume>100 || volume < 0)return;
        audioPlayer.setVolume(volume);
    }
    public void last() {
        trackScheduler.startPreviousTrack();
    }
    public String getPlayList() {
        return trackScheduler.toString();
    }
    public List<Path> getKnownMusic() {
        return localMusicFiles;
    }
    public void clearPlaylist() {
        trackScheduler.clearPlaylist();
    }

}
