package com.rhm.pwn.view_url;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by sambo on 2/18/18.
 */

public class WebInterface {
    Context appContext;
    PWNInteractions interactor;

    /** Instantiate the interface and set the context */
    WebInterface(Context c, PWNInteractions interactor) {
        appContext = c;
        this.interactor = interactor;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void onCssSelectorClicked(String cssSelector) {
        Toast.makeText(appContext, "Select css = '"+cssSelector+"'", Toast.LENGTH_SHORT);
        interactor.handleCSSSelected(cssSelector);
    }
}
