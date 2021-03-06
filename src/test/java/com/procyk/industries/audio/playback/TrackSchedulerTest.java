package com.procyk.industries.audio.playback;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrackSchedulerTest {
    private TrackScheduler scheduler;

    private List<AudioTrack> trackList;
    private AudioTrack track;
    private AudioTrack[] tracks;
    @BeforeEach
    void setup() {
        AudioPlayer audioPlayer = mock(AudioPlayer.class);
        when(audioPlayer.getPlayingTrack()).thenReturn(mock(AudioTrack.class));
        when(audioPlayer.getPlayingTrack().getInfo()).thenReturn(mock(AudioTrackInfo.class));
        scheduler = new TrackScheduler(audioPlayer, Executors.newSingleThreadExecutor());
        track = mock(AudioTrack.class,"first");
        tracks = new AudioTrack[]{
                (mock(AudioTrack.class,"first")),
                mock(AudioTrack.class,"second"),
                mock(AudioTrack.class,"third"),
                mock(AudioTrack.class,"fourth"),
                mock(AudioTrack.class,"fifth")};
        trackList = new ArrayList<>(5);
        Collections.addAll(trackList,tracks);
        for (int i = 0; i < trackList.size(); i++) {
            when(trackList.get(i).getIdentifier()).thenReturn("Track: "+i);
            AudioTrackInfo audioTrackInfo = mock(AudioTrackInfo.class);

            when(trackList.get(i).getInfo()).thenReturn(audioTrackInfo);
        }
    }
    @AfterEach
    void tearDown() {
        scheduler=null;
        trackList.clear();
        tracks=null;
        track=null;
    }
    @Test
    void testStringRepresentationOfScheduler() {
        trackList.forEach(track -> scheduler.queue(track));
        Object[] names = trackList.stream().map(track1 -> String.format("%s:%s",track1.getInfo().author,track1.getInfo().title)).toArray();
    }
    @Test
    void testStartNextAndPreviousTrackWithoutExceedingSize() {
        trackList.forEach(track-> {
            scheduler.queue(track);
            scheduler.startPreviousTrack();
        });

        scheduler.startNextTrack();
        scheduler.startNextTrack();
        scheduler.startPreviousTrack();
        Object[] names = trackList.stream()
                .map(track1 -> String.format("%s:%s",track1.getInfo().author,track1.getInfo().title))
                .toArray();
    }
    @Test
    void testStartNextAndPreviousTrackExceedingSize() {
        for(int i=0;i<2;i++) {
            trackList.forEach(track -> scheduler.queue(track));
        }
        Object[] names = trackList.stream()
                .map(track1 -> String.format("%s:%s",track1.getInfo().author,track1.getInfo().title))
                .toArray();
        Object[] newNames = new Object[names.length*2];
        int count=0;
        for(int i=0;i<2;i++) {
            for (Object name : names) {
                newNames[count++] = name;
            }
        }

        for(int i=0;i<100;i++) {
            scheduler.startPreviousTrack();
        }
    }
}