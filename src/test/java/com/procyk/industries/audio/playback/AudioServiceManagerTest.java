package com.procyk.industries.audio.playback;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.procyk.industries.command.CommandStore;
import com.procyk.industries.module.AudioServiceModule;
import com.procyk.industries.module.BotModule;
import com.procyk.industries.module.CommandServiceModule;
import org.junit.jupiter.api.BeforeEach;

public class AudioServiceManagerTest {
    @Inject
    CommandStore commandStore;
    @Inject
    AudioServiceManager audioServiceManager;

    @BeforeEach
    public void setup() {
        Guice.createInjector(new CommandServiceModule(),new BotModule(),new AudioServiceModule()).injectMembers(this);
    }

    //These tests are deprecated and removed because they're specific to my implementation
//    @Test
//    public void testLocalPathFromIndex() {
//        int songIndex=3;
//        String result = audioServiceManager.getSavableLocalTrackAsString(songIndex);
//        assertEquals("pnbajamclips",result);
//        result = audioServiceManager.getSavableLocalTrackAsString(200);
//        assertEquals("perfect.dark_.xbla-soundpack/fx/00000275_11025.wav",result);
//
//    }
//    @Test
//    public void testTrimRootPath() {
//        List<Path> files = null;
//        try {
//            files = audioServiceManager.getSongsInDirectory("pnbajamclips");
//        } catch (IOException e) {
//            fail("Directory does not exist for LocalMusic");
//        }
//        assertEquals("pnbajamclips",audioServiceManager.trimRootPath(files.get(0).toString()));
//    }

}
