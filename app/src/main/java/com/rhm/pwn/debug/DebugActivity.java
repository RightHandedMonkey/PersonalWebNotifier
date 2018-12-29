package com.rhm.pwn.debug;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.rhm.pwn.R;
import com.rhm.pwn.model.PWNDatabase;
import com.rhm.pwn.model.PWNLog;
import com.rhm.pwn.model.PWNTask;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DebugActivity extends AppCompatActivity {
    List<PWNTask> taskList;
    List<PWNLog> logList;
    List<DebugItem> parsedLogs;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView text = findViewById(R.id.debugTextView);
        text.setMovementMethod(new ScrollingMovementMethod());
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.debugRecyclerView);

        Completable.fromAction(() -> {
                    taskList = PWNDatabase.getInstance(DebugActivity.this)
                            .urlCheckDao().getTasks();
                    logList = PWNDatabase.getInstance(DebugActivity.this)
                            .urlCheckDao().getLogsDescending();
                    parsedLogs = DebugItem.parseLogs(logList);

                }
        ).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(new DebugAdapter(parsedLogs));
                });
    }

}
