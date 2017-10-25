package rhm.com.pwn.utils;

import android.os.Looper;
import android.util.Log;

/**
 * Created by sambo on 9/4/17.
 */

public class TestUtils {
    public static boolean isOnMainThread() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            // On UI thread.
            return true;
        } else {
            // Not on UI thread.
            return false;
        }
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
