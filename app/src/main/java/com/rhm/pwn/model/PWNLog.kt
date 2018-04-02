package com.rhm.pwn.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import com.rhm.pwn.utils.PWNUtils

@Entity
data class PWNLog @JvmOverloads constructor(
        var classname: String = "",
        var message: String = "",
        var logLevel: Char = 'D',
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        var datetime: String = PWNUtils.getCurrentSystemFormattedDate()) {

    companion object {
        lateinit var appContext: Context
        @JvmStatic
        @JvmOverloads
        fun log(classname: String = "",
                message: String = "",
                logLevel: Char = 'D',
                id: Int = 0,
                datetime: String = PWNUtils.getCurrentSystemFormattedDate()) {
            val logItem = PWNLog(classname, message, logLevel, id, datetime)
            PWNDatabase.getInstance(appContext).urlCheckDao().insertLog(logItem)

        }

    }
}