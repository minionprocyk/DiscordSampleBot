package com.procyk.industries.audio.record;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.WordResult;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TestSpeechRecognition {
    private static final Logger logger = Logger.getLogger(TestSpeechRecognition.class.getName());

    public static void handleLiveAudio() {
        //may have to async load audio and offload to continue streaming at a delay
        try {
            logger.info("Starting the live speech recognizer");
            Configuration configuration = new Configuration();
            String ACOUSTIC_MODEL_PATH = "resource:/edu/cmu/sphinx/models/en-us/en-us";
            String DICTIONARY_PATH = "resource:/com/procyk/industries/audio/record/7469.dic";
            String LANGUAGE_MODEL_PATH = "resource:/com/procyk/industries/audio/record/7469.lm";

            configuration.setAcousticModelPath(ACOUSTIC_MODEL_PATH);
            configuration.setDictionaryPath(DICTIONARY_PATH);
            configuration.setLanguageModelPath(LANGUAGE_MODEL_PATH);

            LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
            recognizer.startRecognition(true);
            logger.info("Reading thoughts");
            while(true) {
                SpeechResult result = recognizer.getResult();
                logger.info("Hypothesis from speech recognizer: "+result.getHypothesis());

                logger.log(Level.INFO,"Result of individual words {0}",
                        result.getWords().stream()
                                .map(WordResult::getWord)
                                .collect(Collectors.toList())
                );

                if(result.getHypothesis().equalsIgnoreCase("increase"))
                    break;

            }

            recognizer.stopRecognition();

        }catch(Exception e) {
            logger.warning("Failed to recognize speech");
        }
    }
}
