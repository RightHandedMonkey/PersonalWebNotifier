package com.rhm.pwn.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.content.Context
import android.util.Log
import com.rhm.pwn.utils.PWNUtils
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@Entity
data class PWNLog @JvmOverloads constructor(
        var classname: String = "",
        var message: String = "",
        var logLevel: String = "D",
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        var datetime: String = PWNUtils.getCurrentSystemFormattedDate()) {

    companion object {
        lateinit var appContext: Context
        @JvmStatic
        @JvmOverloads
        fun log(classname: String = "",
                message: String = "",
                logLevel: String = "D",
                id: Int = 0,
                datetime: String = PWNUtils.getCurrentSystemFormattedDate()) {
            val logItem = PWNLog(classname, message, logLevel[0].toString(), id, datetime)

            Completable.fromAction {
                when (logLevel.toUpperCase()) {
                    "E" -> Log.e("SAMB", "$classname - $message")
                    else -> Log.d("SAMB", "$classname - $message")
                }
                PWNDatabase.getInstance(appContext).urlCheckDao().insertLog(logItem)
            }.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe()
        }

    }
}