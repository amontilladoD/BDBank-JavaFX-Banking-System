package com.bdbank.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class ExecutorServiceManager {
    private static final ExecutorServiceManager INSTANCE = new ExecutorServiceManager();
    public static ExecutorServiceManager get() { return INSTANCE; }

    private final ScheduledExecutorService scheduler;

    private ExecutorServiceManager() {
        AtomicInteger counter = new AtomicInteger(1);
        ThreadFactory factory = r -> {
            Thread t = new Thread(r, "BDBank-Worker-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
        scheduler = Executors.newScheduledThreadPool(4, factory);
    }

    public ScheduledExecutorService scheduler() { return scheduler; }

    public void shutdown() { scheduler.shutdownNow(); }
}
