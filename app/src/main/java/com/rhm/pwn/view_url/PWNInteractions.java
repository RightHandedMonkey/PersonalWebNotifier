package com.rhm.pwn.view_url;

/**
 * Created by sambo on 3/10/18.
 */

interface PWNInteractions {
    void handlePageLoaded();

    void selectionActive();

    void selectionInactive();

    boolean handleCSSSelected(String cssSelector);
}
