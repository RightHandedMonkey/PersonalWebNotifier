package com.rhm.pwn.view_url;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.rhm.pwn.R;
import com.rhm.pwn.model.PWNDatabase;
import com.rhm.pwn.model.URLCheck;
import com.rhm.pwn.model.URLCheckChangeNotifier;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class WebViewActivity extends AppCompatActivity implements PWNInteractions {

    private ImageView loadingIndicator;
    private MenuItem loadingIndicatorMenuItem;
    private MenuItem selectMenuItem;
    private MenuItem selectMenuItemActive;
    private boolean stopped = false;
    private boolean loaded = false;
    public boolean selectorActive = false;
    private URLCheck urlc=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("PWN");
        stopped = false;
        loaded = false;
        Integer id = (Integer) getIntent().getSerializableExtra(URLCheck.class.getName());
        if (id != null && id > 0) {
            Single.fromCallable(() -> PWNDatabase.getInstance(this).urlCheckDao().get(id))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((urlCheck, throwable) -> {
                        urlc = urlCheck;
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("SAMB", getClass().getName() + "::onCreateOptionsMenu() called");
        getMenuInflater().inflate(R.menu.menu_pwn_web, menu);
        selectMenuItem = menu.findItem(R.id.select_menu_item_inactive);
        selectMenuItemActive = menu.findItem(R.id.select_menu_item_active);
        loadingIndicatorMenuItem = menu.findItem(R.id.loading_indicator_menu_item);

        if (loadingIndicator == null) {
            loadingIndicator = (ImageView) menu.findItem(R.id.loading_indicator_menu_item).getActionView();
            loadingIndicator.setVisibility(View.VISIBLE);
            loadingIndicatorMenuItem.setVisible(true);
            loadingIndicator.setBackground(getDrawable(R.drawable.loading_monkey_white));
            loadingIndicator.setScaleType(ImageView.ScaleType.FIT_XY);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.loading_animation);
            loadingIndicator.setAnimation(animation);
            animation.setRepeatCount(Animation.INFINITE);
            animation.setRepeatMode(Animation.RESTART);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        loadingIndicator.clearAnimation();
        stopped = true;
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
        loaded = true;
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE);
            loadingIndicator.clearAnimation();
        }
        if (loadingIndicatorMenuItem != null) {
            loadingIndicatorMenuItem.setVisible(false);
        }
    }

    @Override
    public void selectionActive() {
        Log.d("SAMB", "selectionActive() called");
        selectorActive = true;
        //loadingIndicator.setVisibility(View.GONE);
        selectMenuItemActive.setVisible(true);
        selectMenuItem.setVisible(false);
    }

    @Override
    public void selectionInactive() {
        Log.d("SAMB", "selectionInactive() called");
        selectorActive = false;
        //loadingIndicator.setVisibility(View.GONE);
        selectMenuItemActive.setVisible(false);
        selectMenuItem.setVisible(true);
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
