package com.procyk.industries.concurrent;

import javax.inject.Inject;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPoolManager {
    private final ExecutorService executorService;
    private static ThreadPoolManager threadPoolManager;
//    @Inject
    private ThreadPoolManager() {
      this.executorService = Executors.newCachedThreadPool();
    }
    public static ThreadPoolManager getInstance() {
        if(threadPoolManager==null) {
            threadPoolManager = new ThreadPoolManager();
        }
        return threadPoolManager;
    }

    public Future<?> run(Runnable task) {
        return executorService.submit(task);
    }
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }
    public void shutdownAll() {
        executorService.shutdown();
    }
}
