package com.rhm.pwn.model;

import java.util.Observable;

/**
 * Created by sambo on 8/29/2017.
 */

public class URLCheckChangeNotifier extends Observable {
    private static URLCheckChangeNotifier instance = new URLCheckChangeNotifier();

    public void update(Boolean b) {
        setChanged();
        notifyObservers(b);
    }

    public static URLCheckChangeNotifier getNotifier() {
        return instance;
    }
}
