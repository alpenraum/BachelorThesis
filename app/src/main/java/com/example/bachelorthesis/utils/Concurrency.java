package com.example.bachelorthesis.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A Helper class which is used to access the database on a separate thread from the android
 * ui-thread.
 */
public class Concurrency {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static Future<?> executeAsync(Runnable task) {
       return executor.submit(task);
    }

}