package com.procyk.industries.audio.playback;

import com.procyk.industries.module.Application;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.TrackMarker;
import com.sedmelluq.discord.lavaplayer.track.TrackMarkerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Maintains and orchestrates a list of tracks to be played.
 */
@Singleton
public class TrackScheduler extends AudioEventAdapter{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AudioPlayer player;
    private AudioTrack lastTrack;
    private AudioTrackUserData lastTrackUserData;
    private final Deque<AudioTrack> queueTracks;
    private boolean repeat=false;
    private final ExecutorService executorService;
    private static final long TIMEOUT=5000;

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
        AudioTrack track = getTrack(Direction.PREVIOUS);
        if(track!=null)
            startTrack(track,false);
    }
    public void startNextTrack() {
        AudioTrack track = getTrack(Direction.NEXT);
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

    /**
     * Takes time and adjusts the track to the specified time
     * @param time
     */
    public void seekTrack(long time) {
        AudioTrack currTrack = player.getPlayingTrack();
        if(currTrack!=null && currTrack.isSeekable()) {
            player.getPlayingTrack().setPosition(time);
        }
    }

    public void rewind(long time) {
        changeFromCurrentTime(time,false);
    }
    public void fastForward(long time) {
        changeFromCurrentTime(time, true);
    }
    private void changeFromCurrentTime(long time, boolean forward) {
        AudioTrack currTrack = player.getPlayingTrack();
        if(currTrack==null)
            return;
        long curr = currTrack.getPosition();
        long len = currTrack.getDuration();

        if(forward) {
            long fflen = curr+time;
            if(fflen < len)
                seekTrack(fflen);
        }
        else {
            long rrlen = curr-time;
            if(rrlen >= 0L)
                seekTrack(rrlen);
        }

    }
    @Override
    public void onPlayerPause(AudioPlayer player) { }

    @Override
    public void onPlayerResume(AudioPlayer player) { }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        AudioTrackUserData userData = track.getUserData(AudioTrackUserData.class);
        if(userData!=null) {
            int volume = userData.getVolume();
            if(volume>0 && volume<=100) {
                player.setVolume(volume);
            }
            if(track.isSeekable()) {
                track.setPosition(userData.getStart());
                long end = userData.getEnd();
                if(end>0) {
                    track.setMarker(
                            new TrackMarker(end,(markerState)->{
                                logger.info("Track Marker Triggered: {}",markerState);
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
        logger.info("Track ended with reason: {}",endReason);
        if (endReason.mayStartNext || endReason==AudioTrackEndReason.STOPPED) {
            lastTrack=track;
            lastTrackUserData=track.getUserData(AudioTrackUserData.class);

            if(repeat) {
                AudioTrack newTrack = getTrack(Direction.PREVIOUS);
                startTrack(newTrack,false);
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
        NEXT, PREVIOUS
    }

    private AudioTrack getTrack(Direction direction) {
        AudioTrack result = null;
        switch(direction) {
            case NEXT:
                result = queueTracks.poll();
                break;
            case PREVIOUS:
                if(lastTrack!=null) {
                    result =lastTrack.makeClone();
                    result.setUserData(lastTrackUserData);
                }
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
                .map(track -> track.getInfo().title)
                .collect(Collectors.toList());

        StringBuilder stringBuilder = new StringBuilder(100);
        if(queueTracks.size()>1) {
            stringBuilder.append("Up Next: ");
            for(int i=1;i<tracks.size();i++) {
                if(i!=1)
                    stringBuilder.append(i).append(". ");
                stringBuilder.append(tracks.get(i-1))
                        .append(System.lineSeparator());
            }
            stringBuilder.append(System.lineSeparator());
        }

        stringBuilder.append("Currently Playing Track: ")
            .append(player.getPlayingTrack()==null ? lastTrack.getInfo().title : player.getPlayingTrack().getInfo().title);

        if(player.getPlayingTrack()!=null) {
            AudioTrack currTrack = player.getPlayingTrack();
            long curr = currTrack.getPosition();
            long duration = currTrack.getDuration();
            stringBuilder.append(
                    String.format(" [%02d:%02d / %02d:%02d]",
                            TimeUnit.MILLISECONDS.toMinutes(curr),
                            TimeUnit.MILLISECONDS.toSeconds(curr) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(curr)),
                            TimeUnit.MILLISECONDS.toMinutes(duration),
                            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                    )
            )
            ;
        }


        return stringBuilder.toString();
    }
    public Future<String> playlist() {
        return executorService.submit(()-> {
            long start=System.currentTimeMillis();
            while(player.getPlayingTrack()==null) {
                long waited = System.currentTimeMillis() - start;
                try {
                    if(waited >= TIMEOUT)
                        Thread.currentThread().interrupt();
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return Application.TRACK_SCHEDULER_CANNOT_PLAY_TRACK;
                }
            }
            return TrackScheduler.this.toString();
        });
    }
}
