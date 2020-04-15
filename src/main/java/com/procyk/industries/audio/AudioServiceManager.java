package com.procyk.industries.audio;

import com.google.inject.name.Named;
import com.procyk.industries.audio.playback.AudioLoadResultHandlerImpl;
import com.procyk.industries.audio.playback.AudioSendHandlerImpl;
import com.procyk.industries.audio.playback.AudioTrackUserData;
import com.procyk.industries.audio.playback.TrackScheduler;
import com.procyk.industries.command.Command;
import com.procyk.industries.command.CommandParser;
import com.procyk.industries.module.LocalMusicPath;
import com.procyk.industries.strings.Strings;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Singleton
public class AudioServiceManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final AudioSendHandler audioSendHandler;
    private final AudioReceiveHandler audioReceiveHandler;
    private final AudioLoadResultHandler audioLoadResultHandler;
    private final AudioPlayerManager audioPlayerManager;
    private final Path localMusicRootPath;
    private List<Path> localMusicFiles;
    @Inject
    public AudioServiceManager(AudioPlayerManager audioPlayerManager, AudioPlayer audioPlayer, TrackScheduler trackScheduler,
                               AudioSendHandlerImpl audioSendHandler, AudioLoadResultHandlerImpl audioLoadResultHandler,
                               AudioReceiveHandler audioReceiveHandler, @LocalMusicPath Path localMusicRootPath) {
        this.audioPlayerManager=audioPlayerManager;
        this.audioPlayer=audioPlayer;
        this.trackScheduler=trackScheduler;
        this.audioSendHandler=audioSendHandler;
        this.audioLoadResultHandler=audioLoadResultHandler;
        this.audioReceiveHandler=audioReceiveHandler;
        this.localMusicRootPath=localMusicRootPath;
        try {
            localMusicFiles = Files.walk(localMusicRootPath, FileVisitOption.FOLLOW_LINKS)
                    .collect(Collectors.toList()) ;
        } catch (IOException e) {
            logger.info("Local Music Files not found.", e);
        }
        audioPlayer.addListener(trackScheduler);
        audioPlayer.setVolume(30);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }
    public Path getLocalMusicRootPath() {
        return this.localMusicRootPath;
    }

    /**
     * Lists all items, directories and files, contained within the folder specified off of the LocalMusic
     * root directory. If the directory parameter is empty then the root path will be listed instead.
     * @param directory Directory off of LocalMusic root directory
     * @return A list of song Path in the requested directory
     */
    public List<Path> getSongsInDirectory(String directory) throws IOException {
        directory = directory == null ? "" : directory;
       return Files.walk(getLocalMusicRootPath().resolve(directory),1, FileVisitOption.FOLLOW_LINKS)
                .filter(path-> !path.equals(getLocalMusicRootPath()))
                .collect(Collectors.toList());
    }
    public String trimRootPath(String songPath) {
        if(songPath.equals(getLocalMusicRootPath().toString()))
            throw new IllegalArgumentException("Cannot accept LocalMusic root path as a trimmable path");
        int indexOfSongPath = songPath.indexOf(getLocalMusicRootPath().toString());
        int length = getLocalMusicRootPath().toString().length();
        return songPath.substring(indexOfSongPath+length+"/".length());
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
    public AudioPlayer getAudioPlayer() {
        return this.audioPlayer;
    }
    public void repeat(boolean repeat) {
        trackScheduler.setRepeat(repeat);
    }
    public TrackScheduler getTrackScheduler() {
        return this.trackScheduler;
    }
    public void resume() {
        audioPlayer.setPaused(false);
    }
    public void load(String track) {
        audioPlayerManager.loadItem(track,audioLoadResultHandler);
    }
    public void loadWithArgs(Command command) {
        String strStart = command.getOptionalArg("start");
        String strEnd = command.getOptionalArg("end");
        String strVolume = command.getOptionalArg("volume");
        long start,end;
        int volume=0;

        start = CommandParser.parseSecondsToMillisDecimalFormat(strStart);
        end = CommandParser.parseSecondsToMillisDecimalFormat(strEnd);

        if(Strings.isNotBlank(strVolume)) {
            volume = Integer.parseInt(strVolume);
        }
        AudioTrackUserData audioTrackUserData = new AudioTrackUserData(volume,start,end);
        AudioLoadResultHandlerImpl audioLoadResultHandlerNew= new AudioLoadResultHandlerImpl(trackScheduler);
        audioLoadResultHandlerNew.setTrackInfo(audioTrackUserData);

        try {
            audioPlayerManager.loadItem(command.getValue(),audioLoadResultHandlerNew).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.info("Thread interrupted while loading track", e);
            Thread.currentThread().interrupt();
        }
    }

    public void joinRequestedChannel(VoiceChannel voiceChannel, AudioManager audioManager) {
        audioManager.openAudioConnection(voiceChannel);
        audioManager.setSendingHandler(audioSendHandler);
//        audioManager.setReceivingHandler(audioReceiveHandler);//dont load if you want to avoid voice commands
    }
    public void setVolume(int volume) {
        if(volume>100)
            volume=100;
        if(volume<0)
            volume=0;
        audioPlayer.setVolume(volume);
    }
    public void last() {
        trackScheduler.startPreviousTrack();
    }
    public void fastForward(String time) {
        trackScheduler.fastForward(CommandParser.parseSecondsToMillisDecimalFormat(time));
    }
    public void rewind(String time) {
        trackScheduler.rewind(CommandParser.parseSecondsToMillisDecimalFormat(time));
    }
    public void seek(String time) {
        trackScheduler.seekTrack(CommandParser.parseSecondsToMillisDecimalFormat(time));
    }

    /**
     * Returns the playlist constructed from the {@link TrackScheduler}
     * @return
     */
    public String getPlayList() {
        try {
            return trackScheduler.playlist().get();
        } catch (InterruptedException e) {
            logger.warn("Interrupted by process");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            logger.error("Error executing playlist, not good", e);
        }
        return "";
    }
    public List<Path> getKnownMusic() {
        return localMusicFiles;
    }
    public void clearPlaylist() {
        trackScheduler.clearPlaylist();
    }
    public void endCurrentSong() {
        trackScheduler.cancelPlayingTrack();
    }

}
