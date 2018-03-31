package com.rhm.pwn.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class PWNLog constructor(val logLevel: Char, val classname: String, val message: String, val datetime: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}