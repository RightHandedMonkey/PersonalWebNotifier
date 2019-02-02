package com.rhm.pwn.model

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import android.text.TextUtils
import android.util.Log
import com.rhm.pwn.PWNApp
import com.rhm.pwn.R
import com.rhm.pwn.home.PWNHomeActivity
import com.rhm.pwn.network.PWNRetroFitConnector
import com.rhm.pwn.utils.PWNUtils
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import okhttp3.Headers
import org.jetbrains.annotations.NotNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Selector
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * Created by sambo on 9/3/2017.
 */

object URLCheckTask {
    @Volatile
    internal var COUNT = 0
    private var TOTAL = 0

    private const val MESSAGE_LEN = 768
    private const val LONG_MESSAGE_LEN = 768


    @JvmStatic
    fun doesURLCheckRequireUpdate(urlc: URLCheck, curElapsedRealTime: Long): Boolean {
        if (!TextUtils.isEmpty(urlc.baseUrl) && curElapsedRealTime - urlc.lastElapsedRealtime > urlc.checkInterval * 1000) {
            PWNLog.log(URLCheckTask::class.java.name, "doesURLCheckRequireUpdate is TRUE: curElapsedRealTime=" + curElapsedRealTime + " - urlc.getLastElapsedRealtime()=" + urlc.lastElapsedRealtime + " > urlc.getCheckInterval()=" + urlc.checkInterval)
            return true
        }
        PWNLog.log(URLCheckTask::class.java.name, "doesURLCheckRequireUpdate is FALSE")
        return false
    }

    @JvmStatic
    fun checkAll(urlChecks: List<URLCheck>, appContext: Context): Long {
        COUNT = 0
        TOTAL = 0
        PWNLog.log(URLCheckTask::class.java.name, "Start check task")
        val curElapsedTime = SystemClock.elapsedRealtime()
        var minIntervalRequested = java.lang.Long.MAX_VALUE
        for ((num, urlCheck) in urlChecks.withIndex()) {
            minIntervalRequested = Math.min(minIntervalRequested, urlCheck.checkInterval)
            PWNLog.log(URLCheckTask::class.java.name, "Update check for #" + num + ", " + urlCheck.displayTitle)
            if (URLCheckTask.doesURLCheckRequireUpdate(urlCheck, curElapsedTime)) {
                TOTAL++
                PWNLog.log(URLCheckTask::class.java.name, "Scheduling check for #$num")
                URLCheckTask.handleURLCheckAction(urlCheck, appContext)
            } else {
                PWNLog.log(URLCheckTask::class.java.name, "Skipping check for #$num")
            }
        }

        return if (urlChecks.isNotEmpty()) {
            minIntervalRequested
        } else {
            0
        }
    }

    private fun checkForNotification(appContext: Context) {
        PWNLog.log(URLCheckTask::class.java.name, "Checking if notifications are needed")

        Completable.fromAction {
            val list = PWNDatabase.getInstance(appContext).urlCheckDao().all()
            val updated = list.filter { it.hasBeenUpdated && !it.updateShown }
            updated.forEach { urlCheck ->
                PWNLog.log(URLCheckTask::class.java.name, "Found url updated at: " + urlCheck.displayTitle)
                urlCheck.updateShown = true
                //save that the tasks have been notified to the user
                PWNDatabase.getInstance(appContext).urlCheckDao().update(urlCheck)
            }
            if (updated.isNotEmpty()) {
                PWNLog.log(URLCheckTask::class.java.name, "Some urls updated, prepare to show notifications for #" + updated.size)
                //create notification
                if (PWNUtils.isAppIsInBackground(appContext)) {
                    PWNLog.log(URLCheckTask::class.java.name, "App in the background, showing notifications")
                    createNotifications(appContext, updated)
                } else {
                    PWNLog.log(URLCheckTask::class.java.name, "App in the foreground, suppressing notifications")
                }
            }
        }.subscribeOn(Schedulers.io())
                .subscribe()
    }

    private fun checkIfAllDone(appContext: Context) {
        PWNLog.log(URLCheckTask::class.java.name, "Checking if complete")

        if (COUNT >= TOTAL) {
            PWNLog.log(URLCheckTask::class.java.name, "Complete, broadcasting results update")
            URLCheckJobCompletedNotifier.notifier.update()
            URLCheckChangeNotifier.getNotifier().update(false)
            checkForNotification(appContext)
        } else {
            PWNLog.log(URLCheckTask::class.java.name, "Not yet complete")
        }
    }


    private fun handleURLCheckAction(urlc: URLCheck, appContext: Context) {
        PWNLog.log(URLCheckTask::class.java.name, "Beginning check for " + urlc.displayTitle)

        if (!urlc.urlValid) {
            PWNLog.log(URLCheckTask::class.java.name, "Skipping, URL not valid")
            return
        }
        val retrofit = PWNRetroFitConnector.getInstance(urlc.baseUrl)
        val getPageService = retrofit.create(PWNRetroFitConnector.GetPage::class.java)
        getPageService.GetPageAsString(urlc.getUrl()).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                PWNLog.log(URLCheckTask::class.java.name, "Got response")
                PWNLog.log(URLCheckTask::class.java.name, "Headers:")
                PWNLog.log(URLCheckTask::class.java.name, response.headers().toString())
                val doc = Jsoup.parse(response.body())
                PWNLog.log(URLCheckTask::class.java.name, "JSON Parsed")
                var tags: Element? = null
                try {
                    PWNLog.log(URLCheckTask::class.java.name, "Searching for CSS")
                    tags = doc.select(urlc.cssSelectorToInspect!!).first()
                    if (tags != null) {
                        PWNLog.log(URLCheckTask::class.java.name, "CSS item found")
                    } else {
                        PWNLog.log(URLCheckTask::class.java.name, "CSS item NOT found")
                    }
                    PWNLog.log(URLCheckTask::class.java.name, "Getting page title")
                    val title = doc.select("title").first().text()
                    if (!TextUtils.isEmpty(title)) {
                        urlc.title = title
                        PWNLog.log(URLCheckTask::class.java.name, "Found page title: $title")
                    } else {
                        PWNLog.log(URLCheckTask::class.java.name, "Page title NOT found")
                    }
                } catch (e: Selector.SelectorParseException) {
                    PWNLog.log(URLCheckTask::class.java.name, "Error parsing selector\r\n" + e.message, "E")
                    urlc.lastRunCode = URLCheck.CODE_RUN_FAILURE
                    urlc.lastRunMessage = "Could not parse CSS selector. Please correct and retry."
                } catch (e: IllegalArgumentException) {
                    PWNLog.log(URLCheckTask::class.java.name, "Empty or invalid CSS\r\n" + e.message, "E")
                    urlc.lastRunCode = URLCheck.CODE_RUN_FAILURE
                    urlc.lastRunMessage = "CSS selector must not be empty. Please correct and retry."
                }

                val curDate = Calendar.getInstance().time.toString()
                urlc.lastChecked = curDate
                if (tags != null) {
                    PWNLog.log(URLCheckTask::class.java.name, "CSS result tag was ok")
                    //the content has changed from the last time
                    if (urlc.lastValue != tags.text()) {
                        urlc.hasBeenUpdated = true
                        urlc.updateShown = false
                        urlc.lastUpdated = curDate
                    }
                    urlc.lastValue = tags.text()
                    urlc.lastRunCode = URLCheck.CODE_RUN_SUCCESSFUL
                    urlc.lastRunMessage = "Last run successful"
                    urlc.lastElapsedRealtime = SystemClock.elapsedRealtime()
                    Log.d("SAMB", URLCheckTask::class.java.name + " - Successfully completed retrieval URLCheck for: " + urlc.getUrl())
                } else {
                    //Try to use page headers for 'Last-Modified': 'Sat, 21 Apr 2018 16:35:49 GMT'
                    if (!TextUtils.isEmpty(getLastModifiedDate(response.headers()))) {
                        PWNLog.log(URLCheckTask::class.java.name, "CSS result not available, checking via Last-Modified header")
                        //the content has changed from the last time
                        if (urlc.lastValue == null || urlc.lastValue != getLastModifiedDate(response.headers())) {
                            urlc.hasBeenUpdated = true
                            urlc.updateShown = false
                            urlc.lastUpdated = curDate
                        }
                        urlc.lastValue = getLastModifiedDate(response.headers())
                        urlc.lastRunCode = URLCheck.CODE_RUN_SUCCESSFUL
                        urlc.lastRunMessage = "Last run successful"
                        urlc.lastElapsedRealtime = SystemClock.elapsedRealtime()
                        Log.d("SAMB", URLCheckTask::class.java.name + " - Successfully completed retrieval URLCheck for: " + urlc.getUrl())
                    } else {
                        PWNLog.log(URLCheckTask::class.java.name, "CSS result tag was NULL")
                        urlc.lastRunCode = URLCheck.CODE_RUN_FAILURE
                        urlc.lastRunMessage = "Could not find the section of the page to search for. Check that CSS selector still exists for the page\n" + urlc.cssSelectorToInspect!!
                        Log.e("SAMB", URLCheckTask::class.java.name + " - Failed css retrieval URLCheck for: " + urlc.getUrl())
                    }
                }
                //save urlc to db
                Completable.fromAction {
                    PWNDatabase.getInstance(appContext).urlCheckDao().update(urlc)
                    PWNLog.log(URLCheckTask::class.java.name, "Results saved to DB")
                    COUNT++
                    //Notify observers an update is ready
                    checkIfAllDone(appContext)
                }.subscribeOn(Schedulers.io())
                        .subscribe()
                if (tags != null) {
                    Log.d("SAMB", "Response was: " + tags.text())
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                PWNLog.log(URLCheckTask::class.java.name, "Failure for " + urlc.displayTitle + " of \r\n" + t.message)
                Log.e("SAMB", "Error Occurred", t)
                urlc.lastChecked = Calendar.getInstance().time.toString()
                urlc.lastRunCode = URLCheck.CODE_RUN_FAILURE
                urlc.lastRunMessage = t.message
                if (!urlc.isHTTPS) {
                    urlc.lastRunMessage = t.message + "\r\nTry setting URL to be https://"
                }
                //save urlc to db
                Completable.fromAction {
                    PWNDatabase.getInstance(appContext).urlCheckDao().update(urlc)
                    COUNT++
                    Log.e("SAMB", URLCheckTask::class.java.name + " - Failed retrieval URLCheck for: " + urlc.getUrl(), t)
                    checkIfAllDone(appContext)
                }.subscribeOn(Schedulers.io())
                        .subscribe()

            }
        })
    }

    private fun getLastModifiedDate(headers: Headers): String? {
        return headers.get("Last-Modified")
    }

    private fun createNotifications(appContext: Context, updatedItems: List<URLCheck>) {
        var index = 0
        while (index < updatedItems.size) {
            val urlc = updatedItems[index]
            val mBuilder = NotificationCompat.Builder(appContext, PWNApp.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(urlc.title)
                    .setContentText(urlc.displayBody)
                    .setAutoCancel(true)
            mBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(buildLongNotificationMessage(urlc)))
            mBuilder.setContentText(urlc.displayBody)
            // Creates an explicit intent for an Activity in your app
            val resultIntent = Intent(appContext, PWNHomeActivity::class.java)
            resultIntent.putExtra(URLCheck.CLASSNAME, urlc.id)
            resultIntent.putExtra(URLCheck.URL, urlc.getUrl())
            val stackBuilder = TaskStackBuilder.create(appContext)
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(PWNHomeActivity::class.java)
            stackBuilder.addNextIntent(resultIntent)
            val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            mBuilder.setContentIntent(resultPendingIntent)
            val mNotificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // mNotificationId is a unique integer your app uses to identify the
            // notification. For example, to cancel the notification, you can pass its ID
            // number to NotificationManager.cancel().
            mNotificationManager.notify(urlc.id, mBuilder.build())
            index++
            index++
        }

        Completable.fromAction {
            for (urlc in updatedItems) {
                urlc.updateShown = true
            }
            PWNLog.log(URLCheckTask::class.java.name, "Marking url checks as having been displayed to the user in DB")
            PWNDatabase.getInstance(appContext).urlCheckDao().update(updatedItems)
        }.subscribeOn(Schedulers.io())
                .subscribe()
    }

    @JvmStatic
    fun buildLongNotificationMessage(@NotNull item: URLCheck): String {
        var longText = ""
        longText += getShortUpdateText(item.displayTitle) + "\n" + getLongUpdateText(item.lastValue
                ?: "") + "\n"
        //subtract title from this message
        val message = subtractText(longText, item.title ?: "")
        return message.trim { it <= ' ' }
    }

    private fun subtractText(@NotNull longText: String, @NotNull shortText: String): String {
        return if (longText.startsWith(shortText)) {
            longText.substring(shortText.length)
        } else longText
    }

    private fun getShortUpdateText(@NotNull text: String): String {
        return if (text.length > MESSAGE_LEN) {
            text.substring(0, MESSAGE_LEN - 1) + "…\n"
        } else text
    }

    private fun getLongUpdateText(@NotNull text: String): String {
        return if (text.length > LONG_MESSAGE_LEN) {
            text.substring(0, LONG_MESSAGE_LEN - 1) + "…\n"
        } else text
    }
}
