package com.rhm.pwn.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by sambo on 10/7/17.
 */
@Entity
data class PWNTask(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,
        var minLatency: Long = 0,
        var overrideDeadline: Long = 0,
        var actualExecutionTime: String? = null,
        var createJobTime: String?,
        var scheduledExecutionMinTime: String?) {

    constructor(cjt: String, semt: String) : this(createJobTime = cjt, scheduledExecutionMinTime = semt)

    override fun toString(): String {
        return String.format("Id#%d \r\ncreated:%s \n" + "sched min:%s \r\nactual:%s", id, createJobTime, scheduledExecutionMinTime, actualExecutionTime)
    }
}
