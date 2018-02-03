package com.rhm.pwn.model;

import android.util.Log;

import java.util.Observable;

/**
 * Created by sambo on 8/29/2017.
 */

public class URLCheckJobCompletedNotifier extends Observable {
    private static URLCheckJobCompletedNotifier instance = new URLCheckJobCompletedNotifier();

    public void update() {
        setChanged();
        notifyObservers();
        Log.d("SAMB", this.getClass().getName()+" - Completed checking URLs task");
    }

    public static URLCheckJobCompletedNotifier getNotifier() {
        return instance;
    }
}
