package com.mmue21.hackysnake.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A Helper class which is used to access the database on a separate thread from the android
 * ui-thread.
 */
public class Concurrency {
    private static final Executor executor = Executors.newSingleThreadExecutor();

    public static void executeAsync(Runnable task) {
        executor.execute(task);
    }

}