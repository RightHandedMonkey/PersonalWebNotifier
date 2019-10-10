package com.rhm.pwn.model

/**
 * Created by sambo on 9/2/17.
 */

enum class URLCheckInterval(val interval: Long, private val description: String) {
    INT_1_MIN(60, "1 Minute"),
    INT_15_MIN(15 * 60, "15 Minutes"),
    INT_1_HOUR(60 * 60, "1 Hour"),
    INT_6_HOURS(6 * 60 * 60, "6 Hours"),
    INT_1_DAY(24 * 60 * 60, "1 Day"),
    INT_1_WEEK(7 * 24 * 60 * 60, "1 Week");

    companion object {

        const val DEFAULT_VAL = 3

        @JvmStatic
        fun getIndexFromInterval(intv: Long): Int {
            return listOf(values().indexOfFirst { it.interval == intv }, 0).max() ?: 0
        }

        @JvmStatic
        fun getIntervalFromDescription(desc: String): Long {
            return values().firstOrNull { it.description == desc }?.interval ?: INT_1_DAY.interval
        }

        @JvmStatic
        val intervalStrings: List<String> = values().map { it.description }
    }
}
