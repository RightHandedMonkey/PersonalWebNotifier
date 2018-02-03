package com.rhm.pwn.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.rhm.pwn.BuildConfig;
import com.rhm.pwn.R;
import com.rhm.pwn.debug.DebugActivity;

public class PWNHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pwnhome);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            // Create an instance of the dialog fragment and show it
            DialogFragment dialog = new URLCheckDialog();
            dialog.show(getSupportFragmentManager(), URLCheckDialog.class.getName());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (BuildConfig.DEBUG) {
            getMenuInflater().inflate(R.menu.menu_pwnhome, menu);
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
}
