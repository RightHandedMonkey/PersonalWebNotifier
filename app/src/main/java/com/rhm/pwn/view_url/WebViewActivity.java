package com.rhm.pwn.view_url;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.rhm.pwn.R;
import com.rhm.pwn.model.PWNDatabase;
import com.rhm.pwn.model.URLCheck;
import com.rhm.pwn.model.URLCheckChangeNotifier;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class WebViewActivity extends AppCompatActivity implements PWNInteractions {

    private MenuItem selectMenuItemInactive;
    private MenuItem selectMenuItemActive;
    public boolean selectorActive = false;
    private URLCheck urlc=null;

    public static final int HIGHLIGHT_ROW_FROM_CSS_SELECTOR=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("PWN");
        Integer id = (Integer) getIntent().getSerializableExtra(URLCheck.class.getName());
        if (id != null && id > 0) {
            Single.fromCallable(() -> PWNDatabase.getInstance(this).urlCheckDao().get(id))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((urlCheck, throwable) -> urlc = urlCheck);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("SAMB", getClass().getName() + "::onCreateOptionsMenu() called");
        getMenuInflater().inflate(R.menu.menu_pwn_web, menu);
        selectMenuItemInactive = menu.findItem(R.id.select_menu_item_inactive);
        selectMenuItemActive = menu.findItem(R.id.select_menu_item_active);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("SAMB", getClass().getName() + "::onOptionsItemSelected() called - for item#" + item.getItemId());

        if (item.getItemId() == R.id.select_menu_item_active) {
            selectionInactive();
            return true;
        } else if (item.getItemId() == R.id.select_menu_item_inactive) {
            selectionActive();
            return true;
        }
        return false;
    }

    @Override
    public void handlePageLoaded() {
        selectMenuItemActive.setVisible(true);
        selectMenuItemInactive.setVisible(false);
    }

    @Override
    public void selectionActive() {
        Log.d("SAMB", "selectionActive() called");
        selectorActive = true;
        //loadingIndicator.setVisibility(View.GONE);
        selectMenuItemActive.setVisible(true);
        selectMenuItemInactive.setVisible(false);
    }

    @Override
    public void selectionInactive() {
        Log.d("SAMB", "selectionInactive() called");
        selectorActive = false;
        //loadingIndicator.setVisibility(View.GONE);
        selectMenuItemActive.setVisible(false);
        selectMenuItemInactive.setVisible(true);
    }

    @Override
    public boolean handleCSSSelected(String css) {
        Log.d("SAMB", "handleCSSSelected(...) '"+css+"'");

        Completable.fromAction(() -> {
            urlc.setCssSelectorToInspect(css);
            PWNDatabase.getInstance(this.getApplicationContext()).urlCheckDao().update(urlc);
            URLCheckChangeNotifier.getNotifier().update(false);
            this.finish();
        }).subscribeOn(Schedulers.io())
                .subscribe();
        selectionInactive();

        return false;
    }
}
