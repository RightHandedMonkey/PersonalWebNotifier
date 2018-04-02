package com.rhm.pwn;

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.util.Observable;
import java.util.Observer;

import com.rhm.pwn.home.URLCheckJobScheduler;
import com.rhm.pwn.home.URLCheckService;
import com.rhm.pwn.model.PWNDatabase;
import com.rhm.pwn.model.PWNLog;
import com.rhm.pwn.model.URLCheckChangeNotifier;

/**
 * Created by sambo on 8/28/2017.
 */

public class PWNApp extends Application implements Observer {

    public static final String CHANNEL_ID="PWNApp";


    @Override
    public void onCreate() {
        super.onCreate();
        PWNLog.appContext = getApplicationContext();
        Fabric.with(this, new Crashlytics());
        registerNotifications();
        URLCheckChangeNotifier.getNotifier().addObserver(this);
        Log.d("SAMB", "Application Created.");
        PWNDatabase.getInstance(getApplicationContext());
        Log.d("SAMB", "Room Persistence Library Initialized.");
        //The service should only not be running on first start - this avoids duplicate calls to onStartCommand
        if (!isURLCheckServiceRunning()) {
            startService();
        }
    }

    @Override
    public void onTerminate() {
        Log.d("SAMB", this.getClass().getName()+" - Application Shutting down");
        super.onTerminate();
        URLCheckChangeNotifier.getNotifier().deleteObserver(this);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof URLCheckChangeNotifier) {
            if (o instanceof  Boolean && (Boolean)((Boolean) o).booleanValue()) {
                startService();
            }
        } else {
            throw new RuntimeException("Update received from unexpected Observable type");
        }
    }

    public void startService() {
        Intent service = new Intent(getApplicationContext(), URLCheckService.class);
        service.putExtra(URLCheckService.class.getName(), true);
        getApplicationContext().startService(service);
    }

    public void registerNotifications() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // The user-visible name of the channel.
        CharSequence name = getString(R.string.app_name);
        // The user-visible description of the channel.
        String description = getString(R.string.app_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        // Configure the notification channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    private boolean isURLCheckServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (URLCheckService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
