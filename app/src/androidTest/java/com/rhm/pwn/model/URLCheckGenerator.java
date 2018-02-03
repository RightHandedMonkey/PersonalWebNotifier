package com.rhm.pwn.model;

/**
 * Created by sambo on 8/29/2017.
 */

public class URLCheckGenerator {
    public static URLCheck getSample(int index) {
        URLCheck urlc = new URLCheck();
        urlc.setCssSelectorToInspect("css_class#" + index);
        urlc.setUrl("http://cnn.com/#" + index);
        urlc.setLastChecked("last_checked#" + index);
        urlc.setLastUpdated("last_updated#" + index);
        urlc.setLastValue("Some value #" + index);
        urlc.setEnableNotifications(index % 2 == 0);
        urlc.setHasBeenUpdated(index % 2 == 0);
        urlc.setCheckInterval(index);
        return urlc;
    }
}
