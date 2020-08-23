package com.procyk.industries.bot.event;

import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EventListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class OnBotShutdownImpl extends ListenerAdapter implements EventListener {
    private final ExecutorService executorService;
    @Inject
    public OnBotShutdownImpl(ExecutorService executorService) {
        this.executorService=executorService;
    }
    @Override
    public void onShutdown(ShutdownEvent event) {
        super.onShutdown(event);
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                executorService.shutdownNow();
                System.out.println("System did not exit properly");
                System.exit(-1);
            }
        };
        timer.schedule(timerTask, TimeUnit.SECONDS.toMillis(10));
        executorService.shutdown();
        //remove all wave files created by CreateWaveFileTask
        try {
            Files.walk(Paths.get("/tmp"), 0, FileVisitOption.FOLLOW_LINKS)
                    .filter(path -> path.getFileName().startsWith("sphinx_"))
                    .forEach(path -> {
                        System.out.println("Deleting: "+path.getFileName());
                        path.toFile().delete();
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
