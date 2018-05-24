package com.procyk.industries.audio.playback;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrackSchedulerTest {
    TrackScheduler scheduler;
    AudioPlayer audioPlayer;

    List<AudioTrack> trackList;
    AudioTrack track;
    AudioTrack[] tracks;
    @BeforeEach
    public void setup() {
        audioPlayer = mock(AudioPlayer.class);
        when(audioPlayer.getPlayingTrack()).thenReturn(mock(AudioTrack.class));
        when(audioPlayer.getPlayingTrack().getInfo()).thenReturn(mock(AudioTrackInfo.class));
        scheduler = new TrackScheduler(audioPlayer);
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
    public void tearDown() {
        scheduler=null;
        trackList.clear();
        tracks=null;
        track=null;
    }
    @Test
    void testStringRepresentationOfScheduler() {
        trackList.forEach(track -> scheduler.queue(track));
        Object[] names = trackList.stream().map(track1 -> String.format("%s:%s",track1.getInfo().author,track1.getInfo().title)).toArray();
        assertEquals(String.format("[%s, %s, %s, %s, %s] - Currently Playing Track: null by null",names),scheduler.toString());
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
        Object[] names = trackList.stream().map(track1 -> String.format("%s:%s",track1.getInfo().author,track1.getInfo().title)).toArray();
        assertEquals(String.format("[%s, %s, %s] - Currently Playing Track: null by null",Arrays.copyOfRange(names,2,names.length)),scheduler.toString());
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
            for (int i1 = 0; i1 < names.length; i1++) {
                newNames[count++]=names[i1];
            }
        }
        //make sure previous doesnt go beyond scope after exceeding size
        for(int i=0;i<100;i++) {
            scheduler.startPreviousTrack();
        }
        assertEquals(String.format("[%s, %s, %s, %s, %s, %s, %s, %s, %s, %s] - Currently Playing Track: null by null",newNames),scheduler.toString());
    }
}