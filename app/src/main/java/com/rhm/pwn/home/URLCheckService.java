package com.rhm.pwn.home;

import android.app.Service;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import com.rhm.pwn.model.PWNDatabase;
import com.rhm.pwn.model.URLCheck;
import com.rhm.pwn.model.URLCheckTask;

public class URLCheckService extends Service {

    boolean started = false;
    long nextJobDelay = Long.MAX_VALUE;

    public URLCheckService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean restart;
        if (intent != null) {
            restart = intent.getExtras().getBoolean(this.getClass().getName(), false);
        } else {
            restart = false;
        }
        Log.d("SAMB", this.getClass().getName() + " - onStartCommand received");
        //get list of enabled checks, if less than 1 then stopSelf
        Single<List<URLCheck>> single = Single.fromCallable(() -> PWNDatabase.getInstance(this.getApplicationContext()).urlCheckDao().getAllEnabled())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
        single.subscribe((urlChecks, throwable) -> {
            if (urlChecks.size() < 1) {
                Log.d("SAMB", this.getClass().getName() + " - No items to schedule checks for - stopSelf() called");
                stopSelf();
                return;
            }
            if (!started || restart) {

                nextJobDelay = URLCheckTask.checkAll(urlChecks, this.getApplicationContext());
                started = true;
                Log.d("SAMB", this.getClass().getName() + " - Scheduling next job to happen in " + nextJobDelay + " secs");
                //URLCheckJobScheduler.createJob(this.getApplicationContext(), nextJobDelay);
                //check if service already running
                URLCheckJobScheduler.scheduleJob(this.getApplicationContext(), (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE), nextJobDelay);
            } else {
                Log.d("SAMB", this.getClass().getName() + " - No more jobs needed for scheduling");
            }

        });
        Log.d("SAMB", this.getClass().getName() + " Service started");
        return Service.START_STICKY;

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SAMB", this.getClass().getName() + " Service stopped");
    }
}
