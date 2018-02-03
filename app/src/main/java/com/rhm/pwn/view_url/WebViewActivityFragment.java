package com.rhm.pwn.view_url;

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

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import com.rhm.pwn.R;
import com.rhm.pwn.model.PWNDatabase;
import com.rhm.pwn.model.URLCheck;
import com.rhm.pwn.model.URLCheckChangeNotifier;
import com.rhm.pwn.utils.PWNUtils;

import junit.framework.Assert;

/**
 * A placeholder fragment containing a simple view.
 */
public class WebViewActivityFragment extends Fragment {

    public static String SELECTOR = "selector";

    public boolean enableSelector = false;

    public WebViewActivityFragment() {
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
        configureWebView(wv);
        Integer id = (Integer) getActivity().getIntent().getSerializableExtra(URLCheck.class.getName());
        if (id != null && id > 0) {

            Single.fromCallable(() -> PWNDatabase.getInstance(this.getContext()).urlCheckDao().get(id))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((urlCheck, throwable) -> {
                        Log.d("SAMB", "Loading url into webview in fragment");

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
                                URLCheckChangeNotifier.getNotifier().update(false);
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
                        String js = "javascript:"+PWNUtils.readResourceAsString(getContext(), R.raw.selector_min);
                        Assert.assertTrue(!TextUtils.isEmpty(js));
                        view.loadUrl(js);
                        Log.d("SAMB", this.getClass().getName() + " - Finished Loading page");
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        if (null == url) {
                            return false;
                        }

                        try {
                            if (url.contains("http")) {
                                return true;
                            } else {
                                return false;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }

                });
        /*
                @Override

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (null == url) {
                return false;
            }

            try {
                if (url.contains("http")) {
                    AppUtils.openBrowser(mActivity, url);
                    return true;
                } else if (url.contains("@") && url.startsWith("mailto:")) {
                    url = url.replace("mailto:", "").trim();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{url});
                    intent.setType("message/rfc822");
                    startActivity(intent);
                    return true;
                } else if (url.toLowerCase().startsWith("tel:")) {
                    Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(tel);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.loadUrl("javascript:function () { $('body').html( $('body').html().replace(" +
                    "/(\\d\\d\\d-\\d\\d\\d-\\d\\d\\d\\d)/g,'<a href=\"#\">$1</a>') ); }");
        }
         */

    }

}
