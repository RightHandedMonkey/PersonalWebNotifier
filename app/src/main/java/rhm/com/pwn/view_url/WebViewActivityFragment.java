package rhm.com.pwn.view_url;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rhm.com.pwn.R;
import rhm.com.pwn.model.PWNDatabase;
import rhm.com.pwn.model.URLCheck;
import rhm.com.pwn.model.URLCheckChangeNotifier;

/**
 * A placeholder fragment containing a simple view.
 */
public class WebViewActivityFragment extends Fragment {

    public WebViewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_web_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d("SAMB", this.getClass().getName()+", onViewCreated()");

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
        wv.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("SAMB", this.getClass().getName()+" - Finished Loading page");
            }
        });

    }

}
