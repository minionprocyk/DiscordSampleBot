package com.procyk.industries.audio.record;

import com.procyk.industries.command.CommandExecutor;
import com.procyk.industries.strings.Strings;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AudioReceiveHandlerImpl implements AudioReceiveHandler{
    private static final Logger logger = LoggerFactory.getLogger(AudioReceiveHandlerImpl.class);
    private static ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private long before = System.nanoTime();
    private long counter=0;
    private Provider<CommandExecutor> commandExecutorProvider;
    private Timer timer = new Timer();
    private boolean scheduled=false;
    private static final long RECOGNITION_DELAY = 300L;
    private final TaskFactory taskFactory;
    @Inject
    public AudioReceiveHandlerImpl(Provider<CommandExecutor> commandServiceProvider,
                                   TaskFactory taskFactory) {
        this.commandExecutorProvider=commandServiceProvider;
        this.taskFactory=taskFactory;
    }

    @Override
    public boolean canReceiveCombined() {
        return false;
    }

    @Override
    public boolean canReceiveUser() {
        return true;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        throw new UnsupportedOperationException("Combined audio speech recognition not supported");
    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {
        byte[] data = userAudio.getAudioData(1.0d);
        try {
            byteArrayOutputStream.writeBytes(data);
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
    private void createAudioTasks(UserAudio userAudio){
        String fileName = "sphinx_"+counter++;//todo use Application.class constants
        logger.info("Creating audio tasks with file: {}",fileName);
        try {
            CompletableFuture.runAsync(taskFactory.create(byteArrayOutputStream,fileName))
                    .thenApplyAsync((s) -> {
                        try {
                            return taskFactory.create(fileName).call();
                        } catch (Exception e) {
                            logger.warn("Failed to translate speech to text", e);
                            return "FAILED";
                        }
                    })
                    .thenAcceptAsync(hypothesis -> {
                        if(Strings.isNotBlank(hypothesis)) {
                            logger.info("Performing action with hypothesis: {}",hypothesis);
                            commandExecutorProvider.get()
                                    .handleVoiceCommands(hypothesis,userAudio.getUser());
                        }
                    }).thenAccept((a)-> {
                        if(Paths.get("/tmp",fileName+".wav").toFile().delete())
                            logger.info("{}.wav was deleted", fileName);
                        else
                            logger.info("{}.wav failed to be deleted", fileName);
            });
        } catch (Exception e) {
            logger.warn("Failed to create wave file",e);
        }
        before=System.nanoTime();
        byteArrayOutputStream = new ByteArrayOutputStream();
        scheduled=false;
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
