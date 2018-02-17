package com.rhm.pwn.model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import java8.util.stream.StreamSupport;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import com.rhm.pwn.PWNApp;
import com.rhm.pwn.R;
import com.rhm.pwn.home.PWNHomeActivity;
import com.rhm.pwn.network.PWNRetroFitConnector;
import com.rhm.pwn.utils.PWNUtils;

/**
 * Created by sambo on 9/3/2017.
 */

public class URLCheckTask {
    volatile static int COUNT = 0;
    volatile static int SKIPPED = 0;
    static int TOTAL = 0;

    public static final int MESSAGE_LEN = 36;
    public static final int LONG_MESSAGE_LEN = 128;


    public static boolean doesURLCheckRequireUpdate(URLCheck urlc, long curElapsedRealTime) {
        if (curElapsedRealTime - urlc.getLastElapsedRealtime() > urlc.getCheckInterval() && !TextUtils.isEmpty(urlc.getBaseUrl())) {
            return true;
        }
        return false;
    }

    public static long checkAll(List<URLCheck> urlChecks, Context appContext) {
        COUNT = 0;
        TOTAL = 0;
        long curElapsedTime = SystemClock.elapsedRealtime();
        long minIntervalRequested = Long.MAX_VALUE;
        for (URLCheck urlCheck : urlChecks) {
            minIntervalRequested = Math.min(minIntervalRequested, urlCheck.getCheckInterval());
            if (URLCheckTask.doesURLCheckRequireUpdate(urlCheck, curElapsedTime)) {
                TOTAL++;
                Log.d("SAMB", URLCheckTask.class.getName() + " - Queuing URLCheck for: " + urlCheck.getUrl());
                URLCheckTask.handleURLCheckAction(urlCheck, appContext);
            } else {
                Log.d("SAMB", URLCheckTask.class.getName() + " - Skipping URLCheck for: " + urlCheck.getUrl());
            }
        }

        if (urlChecks.size() > 0) {
            return minIntervalRequested;
        } else {
            return 0;
        }
    }

    public static void checkForNotification(Context appContext) {

        Completable.fromAction(() -> {
            List<URLCheck> list = PWNDatabase.getInstance(appContext).urlCheckDao().getAll();
            List<URLCheck> updated = new ArrayList<>();
            StreamSupport.stream(list)
                    .filter(urlCheck -> urlCheck.isHasBeenUpdated() && !urlCheck.isUpdateShown())
                    .forEach(urlCheck -> {
                        updated.add(urlCheck);
                        urlCheck.setUpdateShown(true);
                        //save that the tasks have been notified to the user
                        PWNDatabase.getInstance(appContext).urlCheckDao().update(urlCheck);
                    });
            if (updated != null && updated.size() > 0) {
                //Set the notifications
                String shortMessage = buildShortNotificationMessage(updated);
                String longMessage = buildLongNotificationMessage(updated);
                //create notification
                if (PWNUtils.isAppIsInBackground(appContext)) {
                    createNotifications(appContext, updated.get(0).title, shortMessage, longMessage, updated);
                }
                //create system badge
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    private static void checkIfAllDone(Context appContext) {
        if (COUNT >= TOTAL) {
            URLCheckJobCompletedNotifier.getNotifier().update();
            URLCheckChangeNotifier.getNotifier().update(false);
            checkForNotification(appContext);
        }
    }


    public static void handleURLCheckAction(URLCheck urlc, Context appContext) {
        if (!urlc.isUrlValid()) {
            return;
        }
        Retrofit retrofit = PWNRetroFitConnector.getInstance(urlc.getBaseUrl());
        PWNRetroFitConnector.GetPage getPageService = retrofit.create(PWNRetroFitConnector.GetPage.class);
        getPageService.GetPageAsString(urlc.getUrl()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Document doc = Jsoup.parse(response.body().toString());
                Element tags = null;
                try {
                    tags = doc.select(urlc.getCssSelectorToInspect()).first();
                    String title = doc.select("title").first().text();
                    if (!TextUtils.isEmpty(title)) {
                        urlc.setTitle(title);
                    }
                } catch (Selector.SelectorParseException e) {
                    urlc.setLastRunCode(URLCheck.CODE_RUN_FAILURE);
                    urlc.setLastRunMessage("Could not parse CSS selector. Please correct and retry.");
                } catch (IllegalArgumentException e) {
                    urlc.setLastRunCode(URLCheck.CODE_RUN_FAILURE);
                    urlc.setLastRunMessage("CSS selector must not be empty. Please correct and retry.");
                }
                urlc.setLastChecked(Calendar.getInstance().getTime().toString());
                if (tags != null) {
                    //the content has changed from the last time
                    if (!urlc.lastValue.equals(tags.text())) {
                        urlc.setHasBeenUpdated(true);
                        urlc.setUpdateShown(false);
                    }
                    urlc.setLastValue(tags.text());
                    urlc.setLastRunCode(URLCheck.CODE_RUN_SUCCESSFUL);
                    urlc.setLastRunMessage("Last run successful");
                    urlc.setLastElapsedRealtime(SystemClock.elapsedRealtime());
                    Log.d("SAMB", URLCheckTask.class.getName() + " - Successfully completed retrieval URLCheck for: " + urlc.getUrl());
                } else {
                    urlc.setLastRunCode(URLCheck.CODE_RUN_FAILURE);
                    urlc.setLastRunMessage("Could not find the section of the page to search for. Check that CSS selector still exists on page");
                    Log.e("SAMB", URLCheckTask.class.getName() + " - Failed css retrieval URLCheck for: " + urlc.getUrl());
                }
                //save urlc to db
                Completable.fromAction(() -> {
                    PWNDatabase.getInstance(appContext).urlCheckDao().update(urlc);
                    COUNT++;
                    //Notify observers an update is ready
                    checkIfAllDone(appContext);
                }).subscribeOn(Schedulers.io())
                        .subscribe();
                if (tags != null) {
                    Log.d("SAMB", "Response was: " + tags.text());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("SAMB", "Error Occurred", t);
                urlc.setLastChecked(Calendar.getInstance().getTime().toString());
                urlc.setLastRunCode(URLCheck.CODE_RUN_FAILURE);
                urlc.setLastRunMessage(t.getMessage());
                //save urlc to db
                Completable.fromAction(() -> {
                    PWNDatabase.getInstance(appContext).urlCheckDao().update(urlc);
                    COUNT++;
                    Log.e("SAMB", URLCheckTask.class.getName() + " - Failed retrieval URLCheck for: " + urlc.getUrl(), t);
                    checkIfAllDone(appContext);
                }).subscribeOn(Schedulers.io())
                        .subscribe();

            }
        });
    }

    public static void createNotifications(Context appContext, String title, String shortMsg, String longMsg, List<URLCheck> updatedItems) {
        // The id of the channel.
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(appContext)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(title)
                        .setContentText(shortMsg)
                        .setChannelId(PWNApp.CHANNEL_ID)
                        .setAutoCancel(true);
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Pages have been updated");
        StreamSupport.stream(Arrays.asList(longMsg.split("\n"))).forEach(s -> inboxStyle.addLine(s));
        mBuilder.setStyle(inboxStyle);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(appContext, PWNHomeActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(appContext);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(PWNHomeActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(1, mBuilder.build());
        Completable.fromAction(() -> {
            for (URLCheck urlc : updatedItems) {
                urlc.setUpdateShown(true);
            }
            PWNDatabase.getInstance(appContext).urlCheckDao().update(updatedItems);
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    private static String buildShortNotificationMessage(@NonNull List<URLCheck> list) {
        String message = "";
        message = list.get(0).getLastValue();
        return message;
    }

    private static String buildLongNotificationMessage(@NonNull List<URLCheck> list) {
        String message = "";
        for (int i = 0; i < list.size(); i++) {
            message += getShortUpdateText(list.get(i).getDisplayTitle()) + "\n" + getLongUpdateText(list.get(i).getLastValue()) + "\n";
            if (i < list.size() - 1) {
                message += "\n";
            }
        }
        return message;
    }

    public static String getShortUpdateText(String text) {
        if (text.length() > MESSAGE_LEN) {
            return text.substring(0, MESSAGE_LEN - 1) + "…\n";
        }
        return text;
    }

    public static String getLongUpdateText(String text) {
        if (text.length() > LONG_MESSAGE_LEN) {
            return text.substring(0, LONG_MESSAGE_LEN - 1) + "…\n";
        }
        return text;
    }

}
