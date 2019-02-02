package com.procyk.industries.audio.record;



import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

import java.util.Optional;

public class TranscriberDemo {
    private static String ACOUSTIC_MODEL_PATH;
    private static String DICTIONARY_PATH;
    private static String LANGUAGE_MODEL_PATH;

    public static void main(String[] args) {
        //do something
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath(ACOUSTIC_MODEL_PATH);
        configuration.setDictionaryPath(DICTIONARY_PATH);
        configuration.setLanguageModelPath(LANGUAGE_MODEL_PATH);

        try {
            LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
            recognizer.startRecognition(true);
            SpeechResult result = recognizer.getResult();
            recognizer.stopRecognition();

        }catch(Exception e) {
            System.out.println("Failed to recognize speech");
        }


    }

}
