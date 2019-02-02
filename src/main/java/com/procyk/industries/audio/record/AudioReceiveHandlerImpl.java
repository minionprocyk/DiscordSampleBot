package com.procyk.industries.audio.record;

import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;
import net.dv8tion.jda.core.entities.User;

import javax.inject.Inject;
import java.util.stream.Collectors;

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
        System.out.println("Got combined audio from users: "+
                combinedAudio.getUsers().stream()
        .map(User::getName)
        .collect(Collectors.joining()));
    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {
        System.out.println("Got Audio From : "+userAudio.getUser().getName());
    }
}
