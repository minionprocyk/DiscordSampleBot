package com.procyk.industries.audio.record;

import com.google.inject.Provider;
import com.procyk.industries.command.CommandService;
import com.procyk.industries.concurrent.ThreadPoolManager;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;
import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.procyk.industries.concurrent.ThreadPoolManager.getInstance;

public class AudioReceiveHandlerImpl implements AudioReceiveHandler{
    private static final Logger logger = Logger.getLogger(AudioReceiveHandlerImpl.class.getName());
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private long before = System.nanoTime();
    private long counter=0;
    private Provider<CommandService> commandServiceProvider;
    @Inject
    public AudioReceiveHandlerImpl(Provider<CommandService> commandServiceProvider) {
        this.commandServiceProvider=commandServiceProvider;
    }

    /**
     * Receive all audio if connected to a voice channel
     * @return True if all audio is captured; False otherwise
     */
    @Override
    public boolean canReceiveCombined() {
        return false;
    }

    /**
     * Receive user specific audio
     * @return True if user specific audio is captured; False otherwise
     */
    @Override
    public boolean canReceiveUser() {
        return true;
    }

    public static void transcodeFromFile()throws Exception {
        Configuration configuration = new Configuration();

        // Load model from the jar
        configuration
                .setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");

        // You can also load model from folder
        // configuration.setAcousticModelPath("file:en-us");

        configuration
                .setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration
                .setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

        configuration.setSampleRate(48000);
        StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(
                configuration);
        InputStream stream = AudioReceiveHandlerImpl.class
                .getResourceAsStream("/com/procyk/industries/audio/record/testme.wav");
        stream.skip(44);

        // Simple recognition with generic model
        recognizer.startRecognition(stream);
        SpeechResult result;
        while ((result = recognizer.getResult()) != null) {

            System.out.format("Hypothesis: %s\n", result.getHypothesis());

            System.out.println("List of recognized words and their times:");
            for (WordResult r : result.getWords()) {
                System.out.println(r);
            }

            System.out.println("Best 3 hypothesis:");
            for (String s : result.getNbest(3))
                System.out.println(s);

        }
        recognizer.stopRecognition();

    }
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

                    logger.info("Result of individual words");
                    logger.info(
                            result.getWords().stream()
                                    .map(WordResult::getWord)
                                    .collect(Collectors.toList())
                                    .toString());
                    if(result.getHypothesis().equalsIgnoreCase("increase"))
                        break;

            }

            recognizer.stopRecognition();

        }catch(Exception e) {
            System.out.println("Failed to recognize speech");
            e.printStackTrace();
        }
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {


    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {
        byte[] data = userAudio.getAudioData(1.0d);
        try {
            byteArrayOutputStream.write(data);
        }catch(Exception e) {
            e.printStackTrace();
        }
        //todo more sophisticated approach on determining line cutoffs for processing
        long now = System.nanoTime();
        long timediff = TimeUnit.NANOSECONDS.toSeconds(now-before);
        if(timediff >= 3) {
            String fileName = "sphinx_"+counter++;
            getInstance()
                    .submit(new CreateWaveFileTask(byteArrayOutputStream,fileName));
            Future<String> futureHypothesis = ThreadPoolManager.getInstance()
                    .submit(new SpeechToTextTask(fileName));

            CompletableFuture.supplyAsync(()-> {
                try {
                    String val = futureHypothesis.get();
                    if(StringUtils.isNotBlank(val)) {
                        logger.info("Performing action with hypothesis: "+val);
                        commandServiceProvider.get()
                                .performCustomJDAEvent(val,userAudio.getUser());
                    }
                } catch (InterruptedException| ExecutionException  e) {
                    logger.info("Failed to use Future Hypothesis");
                    e.printStackTrace();

                }
                return "";
            });
            before=now;
            byteArrayOutputStream = new ByteArrayOutputStream();
        }

    }
    public static void main(String[] args) throws  Exception{
//        System.out.println(AudioReceiveHandler.OUTPUT_FORMAT.getFrameSize());
//        AudioFormat targetFormat = new AudioFormat(16000, 16, 1, true, false);
//
//        System.out.println(targetFormat.getFrameSize());
        AudioReceiveHandlerImpl.handleLiveAudio();
//        transcodeFromFile();
    }
}
