package com.rhm.pwn.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import com.google.android.gms.tasks.Tasks.await
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
                PWNDatabase.getInstance(appContext).urlCheckDao().insertLog(logItem)
            }.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe()
        }

    }
}