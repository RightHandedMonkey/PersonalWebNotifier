package com.rhm.pwn.home;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import com.rhm.pwn.model.PWNDatabase;
import com.rhm.pwn.model.PWNLog;
import com.rhm.pwn.model.PWNTask;
import com.rhm.pwn.model.URLCheck;
import com.rhm.pwn.model.URLCheckTask;
import com.rhm.pwn.utils.PWNUtils;

import junit.framework.Assert;

/**
 * Created by sambo on 9/4/17.
 */

public class URLCheckJobScheduler extends JobService {
    boolean keepProcessing = true;
    static volatile boolean started = false;

    @Override
    @MainThread
    public boolean onStartJob(JobParameters jobParameters) {
        started = true;
        PWNLog.log(URLCheckJobScheduler.class.getName(), "onStartJob() called - params: "+jobParameters.toString());
        Single<List<URLCheck>> single = Single.fromCallable(() -> PWNDatabase.getInstance(this.getApplicationContext()).urlCheckDao().getAllEnabled()
        )
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
        single.subscribe((urlChecks, throwable) -> {

            Log.d("SAMB", this.getClass().getName() + " ** Executing scheduled job **");
            //Get existing task and update it
            int taskid = (int) jobParameters.getExtras().getLong(PWNTask.class.getName(), -1);
            if (taskid > 0) {
                PWNTask task = PWNDatabase.getInstance(getApplicationContext()).urlCheckDao().getTask(taskid);
                //Note: It's possible the task was in memory but removed from the database. Skip if not found
                if (task != null) {
                    task.setActualExecutionTime(PWNUtils.getCurrentSystemFormattedDate());
                    Log.d("SAMB", this.getClass().getName() + " Job #" + taskid + " actual execution date: " + PWNUtils.getCurrentSystemFormattedDate());
                    PWNDatabase.getInstance(getApplicationContext()).urlCheckDao().updateTask(task);
                } else {
                    Log.e("SAMB", this.getClass().getName() + "Task was not found in database, so execution time could not be udpdated");
                }
            }
            jobFinished(jobParameters, false);

            if (urlChecks.size() < 1) {
                keepProcessing = false;
                stopSelf();
                return;
            }

            long nextJobDelay = URLCheckTask.checkAll(urlChecks, this.getApplicationContext());
            //Start next JobScheduler
            if (nextJobDelay > 0 && nextJobDelay < Long.MAX_VALUE) {
                scheduleJob(this, (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE), nextJobDelay);
                keepProcessing = true;
            } else {
                keepProcessing = false;
                stopSelf();
            }
            if (throwable != null) {
                Log.e("SAMB", this.getClass().getName() + ", Error occurred reading from database", throwable);
            }
        });
        return keepProcessing;
    }

    public static void cleanOldTasks(Context context) {
        PWNDatabase.getInstance(context).urlCheckDao().reduceTasks();
        Log.d("SAMB", URLCheckJobScheduler.class.getName() + " - Removed tasks in db beyond the allowed limit");
    }

    @WorkerThread
    public static void scheduleJob(Context context, JobScheduler js, Long delayInSec) {
        js.cancelAll();
        Assert.assertTrue(js.getAllPendingJobs().size() == 0);
        cleanOldTasks(context);
        ComponentName mServiceComponent = new ComponentName(context, URLCheckJobScheduler.class);
        long multiplierMS = 1000; //sec to milli
        long minStartDelayDivisor = 10;
        long minStart = delayInSec - delayInSec / minStartDelayDivisor;
        long minLatency = Math.max(1000, minStart * multiplierMS);
        Calendar timeout = Calendar.getInstance();
        timeout.setTimeInMillis((new Date()).getTime() + minLatency);

        // Extras, work duration.
        PWNTask task = new PWNTask(PWNUtils.getCurrentSystemFormattedDate(), PWNUtils.getFormattedDate(timeout.getTime()));
        long taskId = PWNDatabase.getInstance(context).urlCheckDao().insertTask(task);
        JobInfo.Builder builder = new JobInfo.Builder((int) taskId, mServiceComponent);
        builder.setMinimumLatency(minLatency); // wait - at least 1 second
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        //run no matter after 1.25 times the min latency so at least it reschedules the job
        builder.setOverrideDeadline(minLatency+minLatency*1/4);
        builder.setRequiresCharging(false); // we don't care if the device is charging or not
        builder.setRequiresDeviceIdle(false);
        PersistableBundle extras = new PersistableBundle();
        extras.putLong(PWNTask.class.getName(), taskId);
        builder.setExtras(extras);

        int jobSceduledResult = js.schedule(builder.build());
        if (jobSceduledResult == JobScheduler.RESULT_FAILURE) {
            PWNLog.log(URLCheckJobScheduler.class.getName(), "Job could not be queued to start", "E");
        } else {
            PWNLog.log(URLCheckJobScheduler.class.getName(), "Job queued to start as early as: " + PWNUtils.getFormattedDate(timeout.getTime()));
        }
    }

    @Override
    public void onCreate() {
        Log.d("SAMB", this.getClass().getName() + " onCreate() called");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d("SAMB", this.getClass().getName() + " onDestroy() called");
        super.onDestroy();
        started = false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d("SAMB", this.getClass().getName() + " onStopJob() called");
        return true;
    }

    public static boolean isStarted() {
        return started;
    }

    public static void start(Context context) {
        Completable.fromAction(() -> URLCheckJobScheduler.scheduleJob(context, (JobScheduler) context.getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE), 60l))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }
}
