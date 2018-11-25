package com.procyk.industries.audio.playback;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Maintains the tail of the list up to MAX_SIZE of non null elements. The list is backed by an array list
 * which is only added to and never removed. Rather a sublist is created after each queue containing the
 * MAX_SIZE of tracks.
 */
@Singleton
public class TrackScheduler extends AudioEventAdapter{
    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    private final AudioPlayer player;
    private AudioTrack lastTrack;
    private final Deque<AudioTrack> queueTracks;
    private boolean repeat=false;
    private final ExecutorService executorService;

    @Inject
    public TrackScheduler(AudioPlayer player, ExecutorService executorService) {
        this.player = player;
        this.executorService=executorService;
        queueTracks = new ArrayDeque<>();
    }

    private void startTrack(AudioTrack track, boolean noInterrupt) {
        if(null!=track) {
            player.startTrack(track,noInterrupt);
        }
    }
    public void startPreviousTrack() {
        AudioTrack track = getTrack(Direction.previous);
        if(track!=null)
            startTrack(track.makeClone(),false);
    }
    public void startNextTrack() {
        AudioTrack track = getTrack(Direction.next);
        startTrack(track,false);
    }
    public void clearPlaylist() {
        queueTracks.clear();
    }
    public void queue(AudioTrack track) {
        queueTracks.offer(track);
        if(player.isPaused()){
            player.setPaused(false);
        }
        if(player.getPlayingTrack()==null) {
            startNextTrack();
        }
    }
    public boolean getRepeat() {
        return this.repeat;
    }
    public void setRepeat(boolean bRepeat) {
        this.repeat = bRepeat;
    }
    public void cancelPlayingTrack() {
        if(player.getPlayingTrack()!=null) {
            player.playTrack(null);
        }
    }
    @Override
    public void onPlayerPause(AudioPlayer player) {
        // Player was paused;
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        // Player was resumed
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        // A track started playing
        AudioTrackUserData userData = track.getUserData(AudioTrackUserData.class);
        if(userData!=null) {
            //set player volume to user data volume if value is greater than 0, otherwise use player volume
            int volume = userData.getVolume();
            if(volume>0 && volume<=100) {
                player.setVolume(volume);
            }
            if(track.isSeekable()) {
                //always initialize track start as 0 by default, else use userData
                track.setPosition(userData.getStart());
                //set track event handler to start next track if end time is triggered
                long end = userData.getEnd();
                if(end>0) {
                    track.setMarker(
                            new TrackMarker(end,(markerState)->{
                                logger.info("Track Marker Triggered: "+markerState);
                                if(markerState==TrackMarkerHandler.MarkerState.REACHED) {
                                    player.stopTrack();
                                }
                            })
                    );
                }
            }
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        logger.info("Track ended with reason: "+endReason);
        if (endReason.mayStartNext || endReason==AudioTrackEndReason.STOPPED) {
            lastTrack=track;
            if(repeat) {
                startTrack(track.makeClone(),false);
            }
            else {
                startNextTrack();
            }
        }

        // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
        //                       clone of this back to your queue
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        // An already playing track threw an exception (track end event will still be received separately)
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        // Audio track has been unable to provide us any audio, might want to just start a new track
        startNextTrack();
    }
    enum Direction{
        next,previous
    }

    private AudioTrack getTrack(Direction direction) {
        AudioTrack result = null;
        switch(direction) {
            case next:
                result = queueTracks.poll();
                break;
            case previous:
                result =lastTrack;
                break;
            default:
        }
        return result;
    }
    /**
     * @return A string representation of the loaded tracks in the queue in order
     */
    @Override
    public String toString() {
      List<String> tracks = queueTracks.stream()
                .map(track -> String.format("%s:%s",track.getInfo().author,track.getInfo().title))
              .collect(Collectors.toList());
      Collections.reverse(tracks);

    return tracks
            .toString()
            .concat(String.format(" - Currently Playing Track: %s by %s",
                        player.getPlayingTrack()==null ? lastTrack.getInfo().title : player.getPlayingTrack().getInfo().title,
                        player.getPlayingTrack()==null ? lastTrack.getInfo().author : player.getPlayingTrack().getInfo().author
                ));
    }
    public Future<String> playlist() {
        return executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                while(player.getPlayingTrack()==null) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return TrackScheduler.this.toString();
            }
        });
    }
}
