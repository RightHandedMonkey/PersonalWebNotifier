package com.rhm.pwn.application;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.rhm.pwn.models.URLItem;
import com.urbanairship.UAirship;

/**
 * Created by sambo on 4/25/2016.
 */
public class URLMainApp extends com.activeandroid.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
        Log.d(this.getClass().getName(), "App onCreate called");
        createSamples();

        UAirship.takeOff(this, new UAirship.OnReadyCallback() {
            @Override
            public void onAirshipReady(UAirship airship) {
                // Enable user notifications
                airship.getPushManager().setUserNotificationsEnabled(true);
            }
        });
    }

    public void createSamples() {
        new Delete().from(URLItem.class).execute();

        URLItem url1 = new URLItem();
        url1.name = "A.N.N.E.";
        url1.url = "https://www.kickstarter.com/projects/1445624543/anne";
        url1.save();

        URLItem url2 = new URLItem();
        url2.name = "Paradise Lost - First Contact";
        url2.url = "https://www.kickstarter.com/projects/1183462809/paradise-lost-first-contact";
        url2.save();
        Log.d(this.getClass().getName(), "Created Samples for URLMain");
    }
}
