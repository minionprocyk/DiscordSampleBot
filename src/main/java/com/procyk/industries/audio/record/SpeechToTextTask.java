package com.procyk.industries.audio.record;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class SpeechToTextTask implements Callable<String> {
    private static final Logger logger = Logger.getLogger(SpeechToTextTask.class.getName());
    private StreamSpeechRecognizer speechRecognizer;
    private String filename;
    private static final Configuration configuration = new Configuration();
    static {
        String ACOUSTIC_MODEL_PATH = "resource:/edu/cmu/sphinx/models/en-us/en-us";
//        String DICTIONARY_PATH = "resource:/com/procyk/industries/audio/record/7469.dic";
//        String LANGUAGE_MODEL_PATH = "resource:/com/procyk/industries/audio/record/7469.lm";

        //todo should we build our own dictionary? no dict but use grammar?
        configuration.setAcousticModelPath(ACOUSTIC_MODEL_PATH);
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

    }
    public SpeechToTextTask(String filename) {
        this.filename=filename;
        try {
            this.speechRecognizer = new StreamSpeechRecognizer(configuration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public String call() throws Exception {
        String ret="";

        //wait for the resource to come alive
        String pathWithFile = "/tmp/"+filename+".wav";
        File waveFile = new File(pathWithFile);
        do {
            if(!waveFile.isFile())
                Thread.sleep(50);
        }while(!waveFile.isFile());

        //wait for the resource to finish loading
        long lastLength=0L;
        do {
            Thread.sleep(50);
            long fileLength = waveFile.length();
            if(fileLength>lastLength)
                lastLength=fileLength;
            else
                break;
        }while(true);

        //resource has loaded, start recognizing
        InputStream finalStream = new FileInputStream(waveFile);
        finalStream.skip(44);

        logger.info("Starting recognition on: "+filename);
        speechRecognizer.startRecognition(finalStream);
        SpeechResult result;
        while((result= speechRecognizer.getResult())!=null) {
            if(StringUtils.isNotBlank(result.getHypothesis())) {
                logger.info("Hypothesis from speech recognizer: "+result.getHypothesis());
                ret = result.getHypothesis();
                break;
            }
        }
        speechRecognizer.stopRecognition();
        waveFile.delete();
        return ret;
    }
}
