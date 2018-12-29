package com.rhm.pwn.model

import android.util.Log

import java.util.Observable

/**
 * Created by sambo on 8/29/2017.
 */

class URLCheckJobCompletedNotifier : Observable() {

    fun update() {
        setChanged()
        notifyObservers()
        Log.d("SAMB", this.javaClass.name + " - Completed checking URLs task")
    }

    companion object {
        @JvmField
        val notifier = URLCheckJobCompletedNotifier()
    }
}
