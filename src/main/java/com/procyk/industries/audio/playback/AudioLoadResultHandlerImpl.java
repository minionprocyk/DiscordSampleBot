package com.procyk.industries.audio.playback;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.inject.Inject;

public class AudioLoadResultHandlerImpl implements AudioLoadResultHandler {
    private final TrackScheduler trackScheduler;
    private AudioTrackUserData audioTrackUserData;
    @Inject
    public AudioLoadResultHandlerImpl(TrackScheduler trackScheduler) {
        this.trackScheduler=trackScheduler;
    }
    @Override
    public void trackLoaded(AudioTrack track) {
        track.setUserData(audioTrackUserData);
        trackScheduler.queue(track);
    }
    public void setTrackInfo(AudioTrackUserData audioTrackUserData) {
        this.audioTrackUserData=audioTrackUserData;
    }
    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        for (AudioTrack track : playlist.getTracks()) {
            trackScheduler.queue(track);
        }
    }

    @Override
    public void noMatches() {
        // Notify the user that we've got nothing
    }

    @Override
    public void loadFailed(FriendlyException throwable) {
        // Notify the user that everything exploded
    }
}
