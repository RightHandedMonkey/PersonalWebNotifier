package com.rhm.pwn.model;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.util.Log;

/**
 * Created by sambo on 8/28/2017.
 */

@Database(entities = {URLCheck.class, PWNTask.class}, version = 3)
public abstract class PWNDatabase extends RoomDatabase {
    public abstract URLCheckDao urlCheckDao();

    private static PWNDatabase ourInstance;

    public static PWNDatabase getInstance(Context appContext) {
        if (ourInstance == null) {
            ourInstance = Room.databaseBuilder(appContext,
                    PWNDatabase.class, "PWNDb")
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .build();
        }
        return ourInstance;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.d("SAMB", "Running database migration 1 -> 2");
            database.execSQL("ALTER TABLE pwntask ADD COLUMN scheduledExecutionMinTime TEXT");
        }
    };
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.d("SAMB", "Running database migration 2 -> 3");
            database.execSQL("CREATE TABLE if not exists \"pwnlog\"(\n" +
                    "\t\"id\" Integer NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                    "\t\"logLevel\" Text NOT NULL,\n" +
                    "\t\"classname\" Text NOT NULL,\n" +
                    "\t\"message\" Text NOT NULL,\n" +
                    "\t\"datetime\" Date NOT NULL );");
        }
    };
    /*
CREATE TABLE if not exists "pwnlog"(
	"id" Integer NOT NULL PRIMARY KEY AUTOINCREMENT,
	"logLevel" Text NOT NULL,
	"classname" Text NOT NULL,
	"message" Text NOT NULL,
	"datetime" Date NOT NULL );

	    data class PWNLog constructor(val logLevel: Char, val classname: String, val message: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
     */
}
