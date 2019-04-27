package com.rhm.pwn.debug;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rhm.pwn.R;
import com.rhm.pwn.model.PWNDatabase;
import com.rhm.pwn.model.PWNLog;
import com.rhm.pwn.model.PWNTask;
import com.rhm.pwn.model.URLCheck;
import com.rhm.pwn.model.URLCheckChangeNotifier;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kotlinx.coroutines.GlobalScope;

public class DebugActivity extends AppCompatActivity {
    List<PWNTask> taskList;
    List<PWNLog> logList;
    List<DebugItem> parsedLogs;
    RecyclerView recyclerView;
    List<URLCheck> urlChecks;

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
                            .urlCheckDao().tasks();
                    logList = PWNDatabase.getInstance(DebugActivity.this)
                            .urlCheckDao().logsDescending();
                    parsedLogs = DebugItem.parseLogs(logList);

                }
        ).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(new DebugAdapter(parsedLogs));
                });
    }

    public void invalidateUrlChecks(View view) {
        Completable.fromAction(() -> {
                    urlChecks = PWNDatabase.getInstance(DebugActivity.this)
                            .urlCheckDao().all();
                }
        ).observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    Log.d("SAMB", "Started invalidating shown notifications");
                    for (URLCheck urlc : urlChecks) {
                        urlc.setHasBeenUpdated(true);
                        urlc.setUpdateShown(false);
                        PWNDatabase.getInstance(DebugActivity.this).urlCheckDao().update(urlc);
                    }
                    Log.d("SAMB", "Finished invalidating shown notifications");
                    Completable.fromAction(() -> {
                                Toast.makeText(this, "Invalidated, background the app now", Toast.LENGTH_SHORT).show();
                            }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
                    URLCheckChangeNotifier.getNotifier().update(true);
                });
    }
}
