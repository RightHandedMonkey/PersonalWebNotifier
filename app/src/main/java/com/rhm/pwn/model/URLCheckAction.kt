package com.rhm.pwn.model

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.MainThread
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.FragmentManager
import com.rhm.pwn.home.URLCheckDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class URLCheckAction(private val appContext: Context, private val notificationService: NotificationManager, private val supportFragManager: FragmentManager) {

    private var dialog: URLCheckDialog? = null

    @SuppressLint("CheckResult")
    @MainThread
    fun handleViewURLCheck(urlc: URLCheck) {
        Log.d("SAMB", this.javaClass.name + ", handleViewURLCheck() called for $urlc")
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        customTabsIntent.launchUrl(appContext, Uri.parse(urlc.getUrl()))
        URLCheckChangeNotifier.getNotifier().update(true)
        notificationService.cancel(urlc.id)

        GlobalScope.launch {
            urlc.hasBeenUpdated = false
            PWNDatabase.getInstance(appContext).urlCheckDao().update(urlc)
        }
    }

    @MainThread
    fun handleEditURLCheck(urlc: URLCheck?) {
        Log.i("SAMB", this.javaClass.name + ", handleEditURLCheck() called for '$urlc', dialog is '$dialog'")
        if (urlc == null) {
            dialog?.dismiss()
            return
        }
        if (dialog == null) {
            Log.i("SAMB", this.javaClass.name + ", new dialog created")
            dialog = URLCheckDialog()
        }

        val b = Bundle()
        b.putSerializable(URLCheck.CLASSNAME, urlc)
        dialog?.arguments = b
        Log.i("SAMB", this.javaClass.name + ", showing dialog '$dialog'")
        dialog?.show(supportFragManager, URLCheckDialog::class.java.name)
    }
}