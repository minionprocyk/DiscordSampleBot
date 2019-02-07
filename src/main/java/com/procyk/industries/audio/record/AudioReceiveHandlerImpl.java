package com.procyk.industries.audio.record;

import com.google.inject.Provider;
import com.procyk.industries.command.CommandExecutor;
import com.procyk.industries.concurrent.ThreadPoolManager;
import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import static com.procyk.industries.concurrent.ThreadPoolManager.getInstance;

public class AudioReceiveHandlerImpl implements AudioReceiveHandler{
    private static final Logger logger = LoggerFactory.getLogger(AudioReceiveHandlerImpl.class);
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private long before = System.nanoTime();
    private long counter=0;
    private Provider<CommandExecutor> commandExecutorProvider;

    @Inject
    public AudioReceiveHandlerImpl(Provider<CommandExecutor> commandServiceProvider) {
        this.commandExecutorProvider =commandServiceProvider;
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

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        throw new UnsupportedOperationException("Combined audio speech recognition not supported");
    }

    private void createAudioTasks(UserAudio userAudio) {
        String fileName = "sphinx_"+counter++;
        //todo replace with Provider<ThreadPoolmanager>.get
        getInstance()
                .submit(new CreateWaveFileTask(byteArrayOutputStream,fileName));
        Future<String> futureHypothesis = ThreadPoolManager.getInstance()
                .submit(new SpeechToTextTask(fileName));

        CompletableFuture.supplyAsync(()-> {
            try {
                String val = futureHypothesis.get();
                if(StringUtils.isNotBlank(val)) {
                    logger.info("Performing action with hypothesis: {}",val);
                    commandExecutorProvider.get()
                            .handleVoiceCommands(val,userAudio.getUser());
                }
            } catch (InterruptedException| ExecutionException  e) {
                logger.info("Failed to use Future Hypothesis: ",e);
                Thread.currentThread().interrupt();
            }
            return "";
        });
        before=System.nanoTime();
        byteArrayOutputStream = new ByteArrayOutputStream();
        scheduled=false;
    }

    private Timer timer = new Timer();
    private boolean scheduled=false;
    private static final long RECOGNITION_DELAY = 300L;


    /**
     * As userAudio comes in, write all data to a {@link ByteArrayOutputStream}. Wait until there is a duration
     * of silence, then dispatch all data to the appropriate audio tasks for processing.
     * @param userAudio User Audio
     */
    @Override
    public void handleUserAudio(UserAudio userAudio) {
        byte[] data = userAudio.getAudioData(1.0d);
        try {
            byteArrayOutputStream.write(data);
        }catch(Exception e) {
            logger.warn("Failed to write some bytes to speech recognition stream",e);
        }
        long now = System.nanoTime();
        long timediff = TimeUnit.NANOSECONDS.toMillis(now-before);

        if(timediff < RECOGNITION_DELAY) {
            timer.cancel();
            timer = new Timer();
            scheduled=false;
        }
        if(!scheduled) {
            //todo replace with Provider<AudioTaskFactory>.get ? inject userAudio into constructor?
            timer.schedule(new AudioTaskFactory(userAudio),RECOGNITION_DELAY);
            scheduled=true;
        }
        before = System.nanoTime();
    }

    private class AudioTaskFactory extends TimerTask {
        private final Logger logger = LoggerFactory.getLogger(AudioTaskFactory.class);
        private final UserAudio userAudio;
        AudioTaskFactory(UserAudio userAudio) {
            this.userAudio=userAudio;
        }
        @Override
        public void run() {
            logger.info("Starting Audio Tasks");
            createAudioTasks(userAudio);
        }
    }
}
