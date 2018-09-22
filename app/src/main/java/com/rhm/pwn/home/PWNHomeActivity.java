package com.rhm.pwn.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.rhm.pwn.BuildConfig;
import com.rhm.pwn.R;
import com.rhm.pwn.debug.DebugActivity;
import com.rhm.pwn.getting_started.GetStartedFragment;
import com.rhm.pwn.model.URLCheck;
import com.rhm.pwn.utils.PWNUtils;

public class PWNHomeActivity extends AppCompatActivity {
    private static boolean gettingStartedChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!gettingStartedChecked) {
            checkForGettingStarted();
        }

        setContentView(R.layout.activity_pwnhome);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            // Create an instance of the dialog fragment and show it
            DialogFragment dialog = new URLCheckDialog();
            dialog.show(getSupportFragmentManager(), URLCheckDialog.class.getName());
        });

        checkForDeepLink();
    }

    private void checkForDeepLink() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int value = extras.getInt(URLCheck.class.getName(), -1);
            String url = extras.getString(URLCheck.URL, "");
            if (value > 0 && !TextUtils.isEmpty(url)) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(this, Uri.parse(url));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (BuildConfig.DEBUG) {
            getMenuInflater().inflate(R.menu.menu_pwn_home, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_debug && BuildConfig.DEBUG) {
            Intent i = new Intent(this, DebugActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkForGettingStarted() {
        if (!PWNUtils.wasGettingStartedShown(this)) {
            PWNUtils.setGettingStartedShown(this);
            // Create and show the dialog.
            GetStartedFragment.newInstance().show(getSupportFragmentManager(), GetStartedFragment.TAG);
        }
        gettingStartedChecked = true;
    }

}
