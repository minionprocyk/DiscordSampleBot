package com.procyk.industries.module;

public final class Application {
    private Application() {
        throw new IllegalStateException("Static Property Class");
    }
    public static final String TRACK_SCHEDULER_CANNOT_PLAY_TRACK = "Track cannot be played. Probably a broken link";
}
