package com.procyk.industries.module;

public final class Application {
    private Application() {
        throw new IllegalStateException("Static Property Class");
    }
    public static final String TRACK_SCHEDULER_CANNOT_PLAY_TRACK = "Track cannot be played. Probably a broken link";

    public static final String DEFAULT_MEMBER = "anyone";
    public static final long MEMBER_EVENT_TIMEOUT_IN_SECONDS=30;

    public static final String PARSER_NO_MATCH_FOUND="No match found";
}
