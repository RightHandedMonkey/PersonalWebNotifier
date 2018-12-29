package com.rhm.pwn.debug

import com.rhm.pwn.model.PWNLog
import java.util.function.BiConsumer

data class DebugItem(val dateStr: String = "") {
    var debugString: String = ""

    fun getMessage(): String {
        return debugString
    }

    private fun addMessage(message: String) {
        if (debugString.isBlank()) {
            debugString = message
        } else {
            debugString += "\n$message"
        }
    }

    companion object {
        @JvmStatic
        fun parseLogs(logs: List<PWNLog>): List<DebugItem> {
            val list = ArrayList<com.rhm.pwn.debug.DebugItem>()
            for (entry in logs.groupBy { it.datetime.substring(0..15) }) {
                val debugItem = DebugItem(entry.key)
                for (log in entry.value) {
                    debugItem.addMessage(log.toString())
                }
                list.add(debugItem)
            }
            return list
        }
    }


}