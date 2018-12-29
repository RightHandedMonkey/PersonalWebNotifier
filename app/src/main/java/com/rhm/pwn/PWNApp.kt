package com.rhm.pwn

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log

import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import java.util.Observable
import java.util.Observer

import com.rhm.pwn.home.URLCheckJobScheduler
import com.rhm.pwn.model.PWNDatabase
import com.rhm.pwn.model.PWNLog
import com.rhm.pwn.model.URLCheckChangeNotifier

/**
 * Created by sambo on 8/28/2017.
 */

class PWNApp : Application(), Observer {

    override fun onCreate() {
        super.onCreate()
        PWNLog.appContext = applicationContext
        Fabric.with(this, Crashlytics())
        registerNotifications()
        URLCheckChangeNotifier.getNotifier().addObserver(this)
        Log.d("SAMB", "Application Created.")
        PWNDatabase.getInstance(applicationContext)
        Log.d("SAMB", "Room Persistence Library Initialized.")
        //The service should only not be running on first start - this avoids duplicate calls to onStartCommand
        startService()
    }

    override fun onTerminate() {
        Log.d("SAMB", this.javaClass.name + " - Application Shutting down")
        super.onTerminate()
        URLCheckChangeNotifier.getNotifier().deleteObserver(this)
    }

    override fun update(observable: Observable, o: Any) {
        if (observable is URLCheckChangeNotifier) {
            if (o is Boolean && o) {
                startService()
            }
        } else {
            throw RuntimeException("Update received from unexpected Observable type")
        }
    }

    private fun startService() {
        if (!URLCheckJobScheduler.isStarted()) {
            URLCheckJobScheduler.start(this)
        }
    }

    private fun registerNotifications() {
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // The user-visible name of the channel.
        val name = getString(R.string.app_name)
        // The user-visible description of the channel.
        val description = getString(R.string.app_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        // Configure the notification channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = description
            mChannel.enableLights(true)
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mNotificationManager.createNotificationChannel(mChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "PWNApp"
    }
}
