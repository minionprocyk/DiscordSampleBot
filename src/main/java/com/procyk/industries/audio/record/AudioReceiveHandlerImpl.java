package com.procyk.industries.audio.record;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;
import edu.cmu.sphinx.util.TimeFrame;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;
import net.dv8tion.jda.core.entities.User;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AudioReceiveHandlerImpl implements AudioReceiveHandler{
    private static final Logger logger = Logger.getLogger(AudioReceiveHandlerImpl.class.getName());

    private static String ACOUSTIC_MODEL_PATH = "resource:/edu/cmu/sphinx/models/en-us/en-us";;
    private static String DICTIONARY_PATH = "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
    private static String LANGUAGE_MODEL_PATH = "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";
    private final List<Byte> bytes = new ArrayList<>();
    private int methodCounter;
    @Inject
    public AudioReceiveHandlerImpl() {
    }

    /**
     * Receive all audio if connected to a voice channel
     * @return
     */
    @Override
    public boolean canReceiveCombined() {
        return true;
    }

    /**
     * Receive user specific audio
     * @return
     */
    @Override
    public boolean canReceiveUser() {
        return false;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        boolean run=false;

        byte[] data = combinedAudio.getAudioData(1.0d);
        methodCounter++;

        for(byte b : data) {
            bytes.add(b);
        }
        //build up the data array until it reaches 5 seconds worth of data. then dispatch

        if(methodCounter>250 && run) {
            methodCounter=0;

            //do something
            Configuration configuration = new Configuration();
            configuration.setAcousticModelPath(ACOUSTIC_MODEL_PATH);
            configuration.setDictionaryPath(DICTIONARY_PATH);
            configuration.setLanguageModelPath(LANGUAGE_MODEL_PATH);
            configuration.setSampleRate(48000);

            //may have to async load audio and offload to continue streaming at a delay
            try {
                StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
                byte[] raw = new byte[bytes.size()];
                for(int i=0;i<bytes.size();i++) {
                    raw[i] = bytes.get(i);
                }
                recognizer.startRecognition(new ByteArrayInputStream(raw));
                SpeechResult result;
                while((result= recognizer.getResult())!=null) {
                    logger.info("Hypothesis from speech recognizer: "+result.getHypothesis());

                    logger.info("Result of individual words");
                    logger.info(
                            result.getWords().stream()
                                    .map(WordResult::getWord)
                                    .collect(Collectors.toList())
                                    .toString());

                }
                recognizer.stopRecognition();

            }catch(Exception e) {
                System.out.println("Failed to recognize speech");
            }
            bytes.clear();
        }

    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {
        System.out.println("Got Audio From : "+userAudio.getUser().getName());
    }
}
