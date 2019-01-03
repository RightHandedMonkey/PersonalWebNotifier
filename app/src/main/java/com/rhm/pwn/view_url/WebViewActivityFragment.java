package com.rhm.pwn.view_url;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import com.rhm.pwn.R;
import com.rhm.pwn.model.PWNDatabase;
import com.rhm.pwn.model.URLCheck;
import com.rhm.pwn.model.URLCheckChangeNotifier;
import com.rhm.pwn.utils.PWNUtils;

import org.junit.Assert;

/**
 * A placeholder fragment containing a simple view.
 */
public class WebViewActivityFragment extends Fragment {

    public static String SELECTOR = "selector";

    public boolean enableSelector = false;
    private PWNInteractions interactions;
    private ProgressBar progressBar;

    public WebViewActivityFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        interactions = (PWNInteractions) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactions = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle b = getArguments();
        if (b != null) {
            enableSelector = b.getBoolean(SELECTOR);
        }
        return inflater.inflate(R.layout.fragment_web_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d("SAMB", this.getClass().getName() + ", onViewCreated()");

        super.onViewCreated(view, savedInstanceState);
        WebView wv = view.findViewById(R.id.webView);
        progressBar = view.findViewById(R.id.progressBar);
        configureWebView(wv);
        Integer id = (Integer) getActivity().getIntent().getSerializableExtra(URLCheck.class.getName());
        if (id != null && id > 0) {

            Single.fromCallable(() -> PWNDatabase.getInstance(this.getContext()).urlCheckDao().get(id))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((urlCheck, throwable) -> {
                        Log.d("SAMB", "Loading url '" + urlCheck.getUrl() + "' into webview in fragment");

                        if (isAdded()) {
                            //since we are viewing it now, reset the setting
                            urlCheck.setHasBeenUpdated(false);

                            if (urlCheck != null) {
                                wv.loadUrl(urlCheck.getUrl());
                            } else {
                                wv.loadData("No URL to load", "text/html", "UTF-8");
                            }
                            //save urlc to db
                            Completable.fromAction(() -> {
                                PWNDatabase.getInstance(getActivity().getApplicationContext()).urlCheckDao().update(urlCheck);
                                URLCheckChangeNotifier.getNotifier().update(true);
                            }).subscribeOn(Schedulers.io())
                                    .subscribe();
                        }
                        if (throwable != null) {
                            throw new RuntimeException(throwable);
                        }
                    });
        }
    }

    public void configureWebView(WebView wv) {
        WebSettings settings = wv.getSettings();
        settings.setJavaScriptEnabled(true);
        wv.setWebChromeClient(new WebChromeClient());

        wv.setWebViewClient(
                new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        Log.d("SAMB", "onPageFinished() called for '" + url + "'");
                        if (isAdded()) {
                            String js = "javascript:" + PWNUtils.readResourceAsString(getActivity(), R.raw.selector_min);
                            Assert.assertTrue(!TextUtils.isEmpty(js));
                            view.loadUrl(js);
                            if (interactions != null) {
                                interactions.handlePageLoaded();
                            }
                            updateLoadingIndicator(false);
                            Log.d("SAMB", this.getClass().getName() + " - Finished Loading page");
                        }
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Log.d("SAMB", this.getClass().getName() + " - shouldOverrideUrlLoading() where url: "+ url);
                        if (isAdded()) {
                            updateLoadingIndicator(true);
                        }
                        return false;
                    }
                });
        wv.addJavascriptInterface(new WebInterface(this.getContext(), interactions), "Android");
    }

    @MainThread
    public void updateLoadingIndicator(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}
