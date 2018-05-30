package com.procyk.industries.audio.playback;

public class AudioTrackUserData {
    private int volume;
    /**
    time to start the track in seconds;
     */
    private long start;
    /**
     * how long to play the track. Play until track ends if 0
     */
    private long end;
    public AudioTrackUserData(int volume, long start, long end) {
        this.volume=volume;
        this.start=start;
        this.end = end;
    }
    public AudioTrackUserData() {
        this(0,0,0);
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }
}
