package com.rhm.pwn.utils;

import android.os.Looper;
import android.util.Log;

/**
 * Created by sambo on 9/4/17.
 */

public class TestUtils {
    public static boolean isOnMainThread() {
        // On UI thread.
// Not on UI thread.
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static void logThreadName(String tag, String msgPrefix) {
        String threadName = Thread.currentThread().getName();
        if (isOnMainThread()) {
            Log.d(tag, msgPrefix+":This is running on the main thread - "+threadName);
        } else {
            Log.d(tag, msgPrefix+":This is running on a background thread - "+threadName);
        }
    }
}
