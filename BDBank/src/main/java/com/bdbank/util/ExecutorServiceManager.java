package com.bdbank.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central place that owns every background thread the app uses:
 *  - recurring jobs (dollar-rate ticker, notification pump)
 *  - one-off async jobs (login check, transfer processing, chat auto-reply)
 * Using a single shared, named, daemon thread pool instead of "new Thread()" everywhere
 * is the correct multithreading practice: bounded resources + graceful JVM shutdown.
 */
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
