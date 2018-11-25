package com.procyk.industries.audio.playback;

import com.google.inject.name.Named;
import com.procyk.industries.command.Command;
import com.procyk.industries.command.CommandParser;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
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
import java.util.concurrent.ExecutionException;
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

    /**
     * Lists all items, directories and files, contained within the folder specified off of the LocalMusic
     * root directory. If the directory parameter is empty then the root path will be listed instead.
     * @param directory Directory off of LocalMusic root directory
     * @return A list of song Path in the requested directory
     */
    public List<Path> getSongsInDirectory(String directory) throws IOException {
        directory = directory == null ? "" : directory;
       return Files.walk(getLocalMusicRootPath().resolve(directory),1, FileVisitOption.FOLLOW_LINKS)
                .filter(path-> path.equals(getLocalMusicRootPath())==false)
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
        //get the stuff
        String strStart = command.getOptionalArg("start");
        String strEnd = command.getOptionalArg("end");
        String strVolume = command.getOptionalArg("volume");
        long start,end;
        int volume=0;

        start = CommandParser.parseSecondsToMillisDecimalFormat(strStart);
        end = CommandParser.parseSecondsToMillisDecimalFormat(strEnd);

        if(StringUtils.isNotBlank(strVolume)) {
            volume = Integer.parseInt(strVolume);
        }
        AudioTrackUserData audioTrackUserData = new AudioTrackUserData(volume,start,end);
        AudioLoadResultHandlerImpl audioLoadResultHandlerNew= new AudioLoadResultHandlerImpl(trackScheduler);
        audioLoadResultHandler.setTrackInfo(audioTrackUserData);
        audioLoadResultHandlerNew.setTrackInfo(audioTrackUserData);

        //syncronously wait for load to finish to fix playlist printing
        //TODO would prefer to use future api like... loadItem.then(print playlist),
        try {
            audioPlayerManager.loadItem(command.getValue(),audioLoadResultHandlerNew).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void joinRequestedChannel(VoiceChannel voiceChannel, AudioManager audioManager) {
        audioManager.openAudioConnection(voiceChannel);
        audioManager.setSendingHandler(audioSendHandler);
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

    /**
     * Returns the playlist constructed from the {@link TrackScheduler}
     * @return
     */
    public String getPlayList() {
        try {
            return trackScheduler.playlist().get();
        } catch (InterruptedException e) {
           logger.warn("Interrupted by process");
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
