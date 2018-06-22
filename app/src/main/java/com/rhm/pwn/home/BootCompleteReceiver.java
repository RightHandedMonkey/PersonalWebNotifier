package com.rhm.pwn.home;

import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.rhm.pwn.model.PWNDatabase;
import com.rhm.pwn.model.URLCheck;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by sambo on 10/9/17.
 */

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SAMB", "BootCompleteReceiver event received. Starting PWN job");
        Single<List<URLCheck>> single = Single.fromCallable(() -> PWNDatabase.getInstance(context).urlCheckDao().getAllEnabled())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
        single.subscribe((urlChecks, throwable) -> {
            //Need to reset elapsed realtime on each task on boot since the value resets on the device
            for (URLCheck urlc : urlChecks) {
                urlc.setLastElapsedRealtime(0);
            }
            PWNDatabase.getInstance(context).urlCheckDao().update(urlChecks);
            //Instead of starting a service, schedule a job to happen shortly - address target sdk 26 service issues
            URLCheckJobScheduler.scheduleJob(context, (JobScheduler) context.getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE), 60l);
        });

    }
}
