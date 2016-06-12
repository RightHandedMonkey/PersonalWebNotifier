package com.rhm.pwn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.rhm.pwn.R;
import com.rhm.pwn.fragments.URLDisplayFragment;
import com.rhm.pwn.fragments.URLListFragment;
import com.rhm.pwn.fragments.URLNewItemDialog;
import com.rhm.pwn.models.URLItem;

public class URLMainActivity extends AppCompatActivity implements URLListFragment.OnListFragmentInteractionListener {

    public static final int ADD_URL_ITEM_REQUEST_CODE = 88437;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setContentView(R.layout.activity_urlmain);
        //Which layout did Android select?  The large or small?
        //If small, do what's below.  Otherwise the fragments are loaded by the other layout
        if (findViewById(R.id.url_layout_container) != null) {
            //Android selected the smaller layout here
            if (savedInstanceState != null) {
                return;
            }


            URLListFragment firstFragment = new URLListFragment();
            firstFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.url_layout_container, firstFragment).commit();

        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getSupportFragmentManager();
                URLNewItemDialog addItemDialog = URLNewItemDialog.newInstance("", 0,"Add an item to watch list");
                addItemDialog.show(fm, addItemDialog.getClass().getName());

//                Snackbar.make(view, "Add new URL item", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_urlmain, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_URL_ITEM_REQUEST_CODE) {
            //In a task, create a new URL item and launch the edit page with that id number

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(URLItem item) {
        Log.d(this.getClass().getName(), "Object select = " + item.getId());
        URLDisplayFragment urlDisplayFragment = (URLDisplayFragment)
                getSupportFragmentManager().findFragmentById(R.id.url_display_fragment);

        if (urlDisplayFragment != null) {
            //Fragment exists, so update it
            urlDisplayFragment.updateSelectedItem(item.url);
        } else {
            //create fragment
            URLDisplayFragment newUrlDisplay = URLDisplayFragment.newInstance(item.url);
            Bundle args = new Bundle();
            args.putLong(URLDisplayFragment.SELECTED_URL_ITEM, item.getId());
            args.putString(URLDisplayFragment.SELECTED_URL, item.url);
            newUrlDisplay.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.url_layout_container, newUrlDisplay);
            transaction.addToBackStack(null);
            transaction.commit();

        }
    }
}
