package com.rhm.pwn.debug;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import com.rhm.pwn.R;
import com.rhm.pwn.model.PWNDatabase;
import com.rhm.pwn.model.PWNTask;

public class DebugActivity extends AppCompatActivity {
    List<PWNTask> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView text = findViewById(R.id.debugTextView);
        text.setMovementMethod(new ScrollingMovementMethod());
        setSupportActionBar(toolbar);

        Completable.fromAction(() -> {
                    list = PWNDatabase.getInstance(DebugActivity.this)
                            .urlCheckDao().getTasks();

                }
        ).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    JobScheduler js = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
                    List<JobInfo> jobs = js.getAllPendingJobs();

                    String msg = "Pending Jobs:";
                    for(JobInfo job: jobs) {
                        Long minLatS = job.getMinLatencyMillis()/1000;
                        String timeFormatter= "%d days:%d min:%02d sec";
                        String minLatHuman = String.format(timeFormatter, minLatS/60/60, minLatS/60, minLatS%60);
                        Long deadlineS = job.getMaxExecutionDelayMillis()/1000;
                        String deadlineHuman = String.format(timeFormatter, deadlineS/60/60, deadlineS/60, deadlineS%60);

                        msg += String.format("Job#%d, minLat:%s, deadline:%s",job.getId(), minLatHuman, deadlineHuman);
                    }
                    msg += "\r\nJob history:";
                    for (PWNTask p : list) {
                        msg += p.toString() + "\r\n";
                    }
                    text.setText(msg);
                    Log.d("SAMB", "Message: "+msg);
                });
    }

}
