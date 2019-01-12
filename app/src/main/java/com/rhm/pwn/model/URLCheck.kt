package com.rhm.pwn.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import android.text.TextUtils
import android.util.Log
import java.io.Serializable
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by sambo on 8/28/2017.
 *
 * All of the Strings have been made nullable to maintain compatibility with the original java
 * version of the database which allowed null values
 */

@Entity
data class URLCheck(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    internal var url: String?,
    var title: String?,
    var lastUpdated: String?,
    var lastChecked: String?,
    var lastElapsedRealtime: Long,
    var lastValue: String?,
    var cssSelectorToInspect: String?,
    var checkInterval: Long,
    @get:JvmName("isEnableNotifications")
    var enableNotifications: Boolean,
    @get:JvmName("isHasBeenUpdated")
    var hasBeenUpdated: Boolean,
    @get:JvmName("isUpdateShown")
    var updateShown: Boolean,
    var lastRunCode: Int,
    var lastRunMessage: String?,
    @get:JvmName("isUrlValid")
    @Ignore var urlValid: Boolean
) : Serializable {
    constructor(): this(0, "", "", "", "", 0, "", "", URLCheckInterval.INT_1_DAY.interval, true, true, false, CODE_NOT_RUN, "", false)

    val isHTTPS: Boolean
        get() = if (urlValid) {
            url!!.toLowerCase().startsWith("https")
        } else false


    @Ignore
    var baseUrl = ""
        private set(_url) {
            try {
                val urlObj = URL(_url)
                field = urlObj.protocol + "://" + urlObj.host
                urlValid = true
            } catch (e: MalformedURLException) {
                field = "{Malformed URL}"
                urlValid = false
                Log.e("SAMB", "Couldn't convert url to URL Object$_url")
            }

        }

    val displayTitle: String
        get() = if (TextUtils.isEmpty(title)) {
            baseUrl
        } else {
            title!!
        }

    val displayBody: String
        get() = if (!TextUtils.isEmpty(lastValue)) {
            if (lastValue!!.length > MAX_BODY_LEN) {
                lastValue!!.substring(0, MAX_BODY_LEN - 1) + "…"
            } else {
                lastValue!!
            }
        } else {
            url!!
        }

    fun getUrl(): String {
        return url!!
    }

    fun setUrl(url: String) {
        this.url = url
        baseUrl = url
    }

    companion object {

        @Ignore @JvmStatic
        private val MAX_BODY_LEN = 768

        @Ignore @JvmField
        var URL = "url"

        @Ignore
        const val CODE_NOT_RUN = -1
        @Ignore
        const val CODE_RUN_SUCCESSFUL = 0
        @Ignore
        const val CODE_RUN_FAILURE = 1
    }

}
