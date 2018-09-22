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

import org.jetbrains.annotations.NotNull;
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
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Header;

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

    public static final int MESSAGE_LEN = 768;
    public static final int LONG_MESSAGE_LEN = 768;


    public static boolean doesURLCheckRequireUpdate(URLCheck urlc, long curElapsedRealTime) {
        if (!TextUtils.isEmpty(urlc.getBaseUrl()) && (curElapsedRealTime - urlc.getLastElapsedRealtime()) > urlc.getCheckInterval()*1000) {
            PWNLog.log(URLCheckTask.class.getName(), "doesURLCheckRequireUpdate is TRUE: curElapsedRealTime="+curElapsedRealTime+" - urlc.getLastElapsedRealtime()="+urlc.getLastElapsedRealtime()+" > urlc.getCheckInterval()="+urlc.getCheckInterval());
            return true;
        }
        PWNLog.log(URLCheckTask.class.getName(), "doesURLCheckRequireUpdate is FALSE");
        return false;
    }

    public static long checkAll(List<URLCheck> urlChecks, Context appContext) {
        COUNT = 0;
        TOTAL = 0;
        PWNLog.log(URLCheckTask.class.getName(), "Start check task");
        long curElapsedTime = SystemClock.elapsedRealtime();
        long minIntervalRequested = Long.MAX_VALUE;
        int num = 0;
        for (URLCheck urlCheck : urlChecks) {
            minIntervalRequested = Math.min(minIntervalRequested, urlCheck.getCheckInterval());
            PWNLog.log(URLCheckTask.class.getName(), "Update check for #" + num + ", " + urlCheck.getDisplayTitle());
            if (URLCheckTask.doesURLCheckRequireUpdate(urlCheck, curElapsedTime)) {
                TOTAL++;
                PWNLog.log(URLCheckTask.class.getName(), "Scheduling check for #" + num);
                URLCheckTask.handleURLCheckAction(urlCheck, appContext);
            } else {
                PWNLog.log(URLCheckTask.class.getName(), "Skipping check for #" + num);
            }
            num++;
        }

        if (urlChecks.size() > 0) {
            return minIntervalRequested;
        } else {
            return 0;
        }
    }

    public static void checkForNotification(Context appContext) {
        PWNLog.log(URLCheckTask.class.getName(), "Checking if notifications are needed");

        Completable.fromAction(() -> {
            List<URLCheck> list = PWNDatabase.getInstance(appContext).urlCheckDao().getAll();
            List<URLCheck> updated = new ArrayList<>();
            StreamSupport.stream(list)
                    .filter(urlCheck -> urlCheck.isHasBeenUpdated() && !urlCheck.isUpdateShown())
                    .forEach(urlCheck -> {
                        PWNLog.log(URLCheckTask.class.getName(), "Found url updated at: " + urlCheck.getDisplayTitle());
                        updated.add(urlCheck);
                        urlCheck.setUpdateShown(true);
                        //save that the tasks have been notified to the user
                        PWNDatabase.getInstance(appContext).urlCheckDao().update(urlCheck);
                    });
            if (updated != null && updated.size() > 0) {
                PWNLog.log(URLCheckTask.class.getName(), "Some urls updated, prepare to show notifications for #" + updated.size());
                //Set the notifications
                String shortMessage = buildShortNotificationMessage(updated);
                String longMessage = buildLongNotificationMessage(updated);
                //create notification
                if (PWNUtils.isAppIsInBackground(appContext)) {
                    PWNLog.log(URLCheckTask.class.getName(), "App in the background, showing notifications");
                    createNotifications(appContext, updated);
                } else {
                    PWNLog.log(URLCheckTask.class.getName(), "App in the foreground, suppressing notifications");
                }
                //create system badge
            } else {

            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    private static void checkIfAllDone(Context appContext) {
        PWNLog.log(URLCheckTask.class.getName(), "Checking if complete");

        if (COUNT >= TOTAL) {
            PWNLog.log(URLCheckTask.class.getName(), "Complete, broadcasting results update");
            URLCheckJobCompletedNotifier.getNotifier().update();
            URLCheckChangeNotifier.getNotifier().update(false);
            checkForNotification(appContext);
        } else {
            PWNLog.log(URLCheckTask.class.getName(), "Not yet complete");
        }
    }


    public static void handleURLCheckAction(URLCheck urlc, Context appContext) {
        PWNLog.log(URLCheckTask.class.getName(), "Beginning check for " + urlc.getDisplayTitle());

        if (!urlc.isUrlValid()) {
            PWNLog.log(URLCheckTask.class.getName(), "Skipping, URL not valid");
            return;
        }
        Retrofit retrofit = PWNRetroFitConnector.getInstance(urlc.getBaseUrl());
        PWNRetroFitConnector.GetPage getPageService = retrofit.create(PWNRetroFitConnector.GetPage.class);
        getPageService.GetPageAsString(urlc.getUrl()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                PWNLog.log(URLCheckTask.class.getName(), "Got response");
                PWNLog.log(URLCheckTask.class.getName(), "Headers:");
                PWNLog.log(URLCheckTask.class.getName(), response.headers().toString());
                Document doc = Jsoup.parse(response.body().toString());
                PWNLog.log(URLCheckTask.class.getName(), "JSON Parsed");
                Element tags = null;
                try {
                    PWNLog.log(URLCheckTask.class.getName(), "Searching for CSS");
                    tags = doc.select(urlc.getCssSelectorToInspect()).first();
                    if (tags != null) {
                        PWNLog.log(URLCheckTask.class.getName(), "CSS item found");
                    } else {
                        PWNLog.log(URLCheckTask.class.getName(), "CSS item NOT found");
                    }
                    PWNLog.log(URLCheckTask.class.getName(), "Getting page title");
                    String title = doc.select("title").first().text();
                    if (!TextUtils.isEmpty(title)) {
                        urlc.setTitle(title);
                        PWNLog.log(URLCheckTask.class.getName(), "Found page title: " + title);
                    } else {
                        PWNLog.log(URLCheckTask.class.getName(), "Page title NOT found");
                    }
                } catch (Selector.SelectorParseException e) {
                    PWNLog.log(URLCheckTask.class.getName(), "Error parsing selector\r\n" + e.getMessage(), "E");
                    urlc.setLastRunCode(URLCheck.CODE_RUN_FAILURE);
                    urlc.setLastRunMessage("Could not parse CSS selector. Please correct and retry.");
                } catch (IllegalArgumentException e) {
                    PWNLog.log(URLCheckTask.class.getName(), "Empty or invalid CSS\r\n" + e.getMessage(), "E");
                    urlc.setLastRunCode(URLCheck.CODE_RUN_FAILURE);
                    urlc.setLastRunMessage("CSS selector must not be empty. Please correct and retry.");
                }
                urlc.setLastChecked(Calendar.getInstance().getTime().toString());
                if (tags != null) {
                    PWNLog.log(URLCheckTask.class.getName(), "CSS result tag was ok");
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
                    //Try to use page headers for 'Last-Modified': 'Sat, 21 Apr 2018 16:35:49 GMT'
                    if (!TextUtils.isEmpty(getLastModifiedDate(response.headers()))) {
                        PWNLog.log(URLCheckTask.class.getName(), "CSS result not available, checking via Last-Modified header");
                        //the content has changed from the last time
                        if (urlc.lastValue == null || !urlc.lastValue.equals(getLastModifiedDate(response.headers()))) {
                            urlc.setHasBeenUpdated(true);
                            urlc.setUpdateShown(false);
                        }
                        urlc.setLastValue(getLastModifiedDate(response.headers()));
                        urlc.setLastRunCode(URLCheck.CODE_RUN_SUCCESSFUL);
                        urlc.setLastRunMessage("Last run successful");
                        urlc.setLastElapsedRealtime(SystemClock.elapsedRealtime());
                        Log.d("SAMB", URLCheckTask.class.getName() + " - Successfully completed retrieval URLCheck for: " + urlc.getUrl());
                    } else {
                        PWNLog.log(URLCheckTask.class.getName(), "CSS result tag was NULL");
                        urlc.setLastRunCode(URLCheck.CODE_RUN_FAILURE);
                        urlc.setLastRunMessage("Could not find the section of the page to search for. Check that CSS selector still exists for the page\n" + urlc.getCssSelectorToInspect());
                        Log.e("SAMB", URLCheckTask.class.getName() + " - Failed css retrieval URLCheck for: " + urlc.getUrl());
                    }
                }
                //save urlc to db
                Completable.fromAction(() -> {
                    PWNDatabase.getInstance(appContext).urlCheckDao().update(urlc);
                    PWNLog.log(URLCheckTask.class.getName(), "Results saved to DB");
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
                PWNLog.log(URLCheckTask.class.getName(), "Failure for " + urlc.getDisplayTitle() + " of \r\n" + t.getMessage());
                Log.e("SAMB", "Error Occurred", t);
                urlc.setLastChecked(Calendar.getInstance().getTime().toString());
                urlc.setLastRunCode(URLCheck.CODE_RUN_FAILURE);
                urlc.setLastRunMessage(t.getMessage());
                if (!urlc.isHTTPS()) {
                    urlc.setLastRunMessage(t.getMessage() + "\r\nTry setting URL to be https://");
                }
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

    private static String getLastModifiedDate(Headers headers) {
        return headers.get("Last-Modified");
    }

    public static void createNotifications(Context appContext, List<URLCheck> updatedItems) {
        for (int index = 0; index < updatedItems.size(); index++) {
            URLCheck urlc = updatedItems.get(index);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(appContext, PWNApp.CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_stat_name)
                            .setContentTitle(urlc.title)
                            .setContentText(urlc.getDisplayBody())
                            .setAutoCancel(true);
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(buildLongNotificationMessage(urlc)));
            mBuilder.setContentText(urlc.getDisplayBody());
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(appContext, PWNHomeActivity.class);
            resultIntent.putExtra(URLCheck.class.getName(), urlc.getId());
            resultIntent.putExtra(URLCheck.URL, urlc.getUrl());
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
            mNotificationManager.notify(urlc.getId(), mBuilder.build());
            index++;
        }

        Completable.fromAction(() -> {
            for (URLCheck urlc : updatedItems) {
                urlc.setUpdateShown(true);
            }
            PWNLog.log(URLCheckTask.class.getName(), "Marking url checks as having been displayed to the user in DB");
            PWNDatabase.getInstance(appContext).urlCheckDao().update(updatedItems);
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    static @NotNull String buildShortNotificationMessage(@NonNull List<URLCheck> list) {
        String message = getShortUpdateText(list.get(0).getLastValue())+"";
        return message.trim();
    }

    static @NotNull String buildLongNotificationMessage(@NonNull List<URLCheck> list) {
        String longText = "";
        for (int i = 0; i < list.size(); i++) {
            longText += getShortUpdateText(list.get(i).getDisplayTitle()) + "\n" + getLongUpdateText(list.get(i).getLastValue()) + "\n";
            if (i < list.size() - 1) {
                longText += "\n";
            }
        }
        //subtract title from this message
        String message = subtractText(longText, getTitle(list));
        return message.trim();
    }

    static @NotNull String buildLongNotificationMessage(@NonNull URLCheck item) {
        String longText = "";
        longText += getShortUpdateText(item.getDisplayTitle()) + "\n" + getLongUpdateText(item.getLastValue()) + "\n";
        //subtract title from this message
        String message = subtractText(longText, item.getTitle());
        return message.trim();
    }

    public static @NotNull String subtractText(String longText, String shortText) {
        if(longText.startsWith(shortText)) {
            return longText.substring(shortText.length());
        }
        return longText;
    }

    public static @NotNull String getShortUpdateText(@NotNull String text) {
        if (text != null && text.length() > MESSAGE_LEN) {
            return text.substring(0, MESSAGE_LEN - 1) + "…\n";
        }
        return text;
    }

    public static String getLongUpdateText(String text) {
        if (text != null && text.length() > LONG_MESSAGE_LEN) {
            return text.substring(0, LONG_MESSAGE_LEN - 1) + "…\n";
        }
        return text;
    }

    public static String getTitle(@NotNull List<URLCheck> list) {
        String title="";
        if (!list.isEmpty()) {
            title = list.get(0).title;
        }
        return title;
    }

}
