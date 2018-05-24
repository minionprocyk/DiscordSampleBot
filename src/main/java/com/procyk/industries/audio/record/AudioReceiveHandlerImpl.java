package com.procyk.industries.audio.record;

import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;

import javax.inject.Inject;

public class AudioReceiveHandlerImpl implements AudioReceiveHandler{
    @Inject
    public AudioReceiveHandlerImpl() {

    }

    @Override
    public boolean canReceiveCombined() {
        return false;
    }

    @Override
    public boolean canReceiveUser() {
        return false;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {

    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {

    }
}
