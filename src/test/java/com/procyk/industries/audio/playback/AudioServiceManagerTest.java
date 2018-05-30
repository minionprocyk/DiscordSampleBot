package com.procyk.industries.audio.playback;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.procyk.industries.command.CommandStore;
import com.procyk.industries.module.AudioServiceModule;
import com.procyk.industries.module.BotModule;
import com.procyk.industries.module.CommandServiceModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AudioServiceManagerTest {
    @Inject
    CommandStore commandStore;
    @Inject
    AudioServiceManager audioServiceManager;

    @BeforeEach
    public void setup() {
        Guice.createInjector(new CommandServiceModule(),new BotModule(),new AudioServiceModule()).injectMembers(this);
    }

    @Test
    public void testLocalPathFromIndex() {
        int songIndex=3;
        String result = audioServiceManager.getSavableLocalTrackAsString(songIndex);
        assertEquals("pnbajamclips",result);
    }
}
