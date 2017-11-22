package rhm.com.pwn.home;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rhm.com.pwn.BuildConfig;
import rhm.com.pwn.model.PWNDatabase;
import rhm.com.pwn.model.PWNTask;
import rhm.com.pwn.model.URLCheck;
import rhm.com.pwn.model.URLCheckTask;
import rhm.com.pwn.utils.PWNUtils;

/**
 * Created by sambo on 9/4/17.
 */

public class URLCheckJobScheduler extends JobService {
    boolean keepProcessing = true;
    static volatile boolean started = false;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        started = true;
        Log.d("SAMB", this.getClass().getName() + " onStartJob() called");
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
                task.setActualExecutionTime(PWNUtils.getCurrentSystemDate());
                Log.d("SAMB", this.getClass().getName() + " Job #"+taskid+" actual execution date: "+PWNUtils.getCurrentSystemDate());
                PWNDatabase.getInstance(getApplicationContext()).urlCheckDao().updateTask(task);
            }
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

    @WorkerThread
    public static void scheduleJob(Context context, JobScheduler js, Long delayInSec) {
        js.cancelAll();
        ComponentName mServiceComponent = new ComponentName(context, URLCheckJobScheduler.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, mServiceComponent);
        long multiplier = 1000; //sec to milli
        long minStart = delayInSec - delayInSec / 10;
        long maxStart = delayInSec + delayInSec / 10;
        long minLatency = Math.max(1000, minStart * multiplier);
        long deadline = Math.max(15000, maxStart * multiplier);
        builder.setMinimumLatency(minLatency); // wait - at least 1 second
        builder.setOverrideDeadline(deadline); // maximum delay - at least 15 sec
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        builder.setRequiresCharging(false); // we don't care if the device is charging or no
        builder.setRequiresDeviceIdle(false);

        // Extras, work duration.
        PWNTask task = new PWNTask(minLatency, deadline, PWNUtils.getCurrentSystemDate());
        long taskId = PWNDatabase.getInstance(context).urlCheckDao().insertTask(task);
        PersistableBundle extras = new PersistableBundle();
        extras.putLong(PWNTask.class.getName(), taskId);
        builder.setExtras(extras);
        js.schedule(builder.build());

        Log.d("SAMB", URLCheckJobScheduler.class.getName() + " - Job queued to start in " + delayInSec + " seconds");
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
}
