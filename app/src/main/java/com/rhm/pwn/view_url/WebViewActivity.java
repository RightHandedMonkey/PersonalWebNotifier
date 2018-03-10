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

public class WebViewActivity extends AppCompatActivity implements PWNInteractions {

    private ImageView loadingIndicator;
    private MenuItem selectMenuItem;
    private ImageView selectMenuItemView;
    private boolean stopped = false;
    private boolean loaded = false;
    public boolean selectorActive = false;

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pwn_web, menu);
        selectMenuItem = menu.getItem(1);
        loadingIndicator = (ImageView) menu.findItem(R.id.loading_indicator_menu_item).getActionView();
        loadingIndicator.setVisibility(View.VISIBLE);
        loadingIndicator.setBackground(getDrawable(R.drawable.loading_monkey_white));
        loadingIndicator.setScaleType(ImageView.ScaleType.FIT_XY);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.loading_animation);
        loadingIndicator.setAnimation(animation);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RESTART);

        selectMenuItemView = (ImageView) menu.findItem(R.id.select_menu_item).getActionView();
        selectMenuItemView.setBackground(getDrawable(R.drawable.target));


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
        Log.d("SAMB", "onOptionsItemSelected() called - for item#" + item.getItemId());

        if (item.getItemId() == 1) {
            if (selectorActive) {
                selectionInactive();
            } else {
                selectionActive();
            }
            return true;
        }
        return false;
    }

    @Override
    public void handlePageLoaded() {
        loaded = true;
        selectMenuItem.setEnabled(true);
        selectMenuItem.setVisible(true);
        selectMenuItemView.setEnabled(true);
        selectionInactive();
        if (loadingIndicator != null) {
            loadingIndicator.clearAnimation();
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.loading_stop_animation);
            loadingIndicator.setAnimation(animation);
            animation.setRepeatCount(0);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!stopped) {
                        loadingIndicator.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    @Override
    public void selectionActive() {
        Log.d("SAMB", "selectionActive() called");
        selectMenuItemView.setBackground(getDrawable(R.drawable.target_active));
    }

    @Override
    public void selectionInactive() {
        Log.d("SAMB", "selectionInactive() called");
        selectMenuItemView.setBackground(getDrawable(R.drawable.target));
    }

    @Override
    public boolean handleCSSSelected(String css) {
        Log.d("SAMB", "handleCSSSelected(...) called");
        return false;
    }
}
