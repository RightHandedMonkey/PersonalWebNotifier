package com.rhm.pwn.model

import java.util.Observable

/**
 * Created by sambo on 8/29/2017.
 */

class URLCheckChangeNotifier : Observable() {

    fun update(b: Boolean?) {
        setChanged()
        notifyObservers(b)
    }

    companion object {
        private val notifier = URLCheckChangeNotifier()

        @JvmStatic
        fun getNotifier() : URLCheckChangeNotifier {
            return notifier
        }
    }
}
