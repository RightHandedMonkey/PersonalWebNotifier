package com.rhm.pwn.model

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import android.content.Context
import android.util.Log

/**
 * Created by sambo on 8/28/2017.
 */
@Database(entities = [URLCheck::class, PWNTask::class, PWNLog::class], version = 3, exportSchema = false)
abstract class PWNDatabase : RoomDatabase() {
    abstract fun urlCheckDao(): URLCheckDao

    companion object {

        private var ourInstance: PWNDatabase? = null

        @JvmStatic
        fun getInstance(appContext: Context): PWNDatabase {
            synchronized(PWNDatabase::class) {
                if (ourInstance == null) {
                    ourInstance = Room.databaseBuilder(appContext,
                            PWNDatabase::class.java, "PWNDb")
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .build()
                }
            }
            return ourInstance!!
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("SAMB", "Running database migration 1 -> 2")
                database.execSQL("ALTER TABLE pwntask ADD COLUMN scheduledExecutionMinTime TEXT")
            }
        }

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("SAMB", "Running database migration 2 -> 3")
                database.execSQL("CREATE TABLE if not exists \"pwnlog\"(\n" +
                        "\t\"id\" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "\t\"logLevel\" TEXT NOT NULL,\n" +
                        "\t\"classname\" TEXT NOT NULL,\n" +
                        "\t\"message\" TEXT NOT NULL,\n" +
                        "\t\"datetime\" TEXT NOT NULL );")
            }
        }
    }
}
